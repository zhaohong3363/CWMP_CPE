package cn.sct.networkmanager.agent.protocol.cwmp;

import cn.sct.networkmanager.agent.config.CPEConfigProperties;
import cn.sct.networkmanager.agent.domain.enums.CWMPClientState;
import cn.sct.networkmanager.agent.domain.enums.CWMPEventCode;
import cn.sct.networkmanager.agent.domain.model.acs.ACSInformResponseEnvelope;
import cn.sct.networkmanager.agent.domain.model.JAXBUtils;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.DispatchMessageMethodHandler;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFulHttpResponse;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import cn.sct.networkmanager.agent.transport.netty.CWMPNettyClient;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import reactor.util.function.Tuples;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;


public class DefaultCwmpSession implements CommandLineRunner {
    private static final Logger log = Loggers.getLogger(DefaultCwmpSession.class);
    private final CWMPNettyClient client;
    private final CPEConfigProperties config;
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    private final DispatchMessageMethodHandler dispatchMessageMethodHandler;
    private final AtomicLong lastResponseTime = new AtomicLong(System.currentTimeMillis());
    private final AtomicInteger maxEnvelopes = new AtomicInteger(0);//服务端该次请求理论上发送报文数
    private Disposable task;
    private final ApplicationContext context;
    private final AtomicReference<AuthManager> authManager = new AtomicReference<>(null);
    private final AnonymousAuthManager anonymousAuthManager = new AnonymousAuthManager(null);

    public DefaultCwmpSession(CPEConfigProperties config,
                              HttpMessageCreateFactory httpMessageCreateFactory,
                              ApplicationEventPublisher eventPublisher,
                              ApplicationContext context) {

        this.config = config;
        this.httpMessageCreateFactory = httpMessageCreateFactory;
        this.context = context;
        this.client=new CWMPNettyClient(eventPublisher,config);
        this.dispatchMessageMethodHandler = new DispatchMessageMethodHandler(this,httpMessageCreateFactory);
        ProactiveNoticeChangeManager.subscribe()
                .buffer(Duration.ofSeconds(5))
                .filter(list->!list.isEmpty())
                .flatMap(list->{
                    //发送值主动改变事件
                    return this.connect(CWMPEventCode.VALUE_CHANGE,"").flatMap(aBoolean -> {
                        if (aBoolean){
                            return send(httpMessageCreateFactory.createNull(),
                                    new AtomicInteger(0));
                        }
                        return Mono.empty().then();
                    });
                }).subscribeOn(Schedulers.boundedElastic()).subscribe();


    }
    public Mono<Boolean> connect(CWMPEventCode code,String commandKey){
        AtomicBoolean timeOut = new AtomicBoolean(false);
        Disposable disposable = null;
        while (!(authManager.get() ==null && authManager.compareAndSet(null,anonymousAuthManager))
            && !timeOut.get()
        ){
            disposable=Mono.delay(Duration.ofSeconds(30))
                    .doOnNext(x-> timeOut.set(true))
                    .subscribeOn(Schedulers.boundedElastic()).subscribe();
        }
        if (timeOut.get()){
            assert disposable != null;
            disposable.dispose();
            return Mono.just(false);
        }
            return Mono.fromCallable(()->{
                        if (client.connect(config.getAcsHost(), config.getAcsPort())){
                            return true;
                        }
                        throw new RuntimeException();
                    })
                    .retryWhen(
                            Retry.backoff(config.getMaxRetryAttempts(), Duration.ofMillis(3000))
                                    .maxBackoff(Duration.ofMillis(config.getMaxDelayMillis()))
                                    .jitter(0.1)
                                    .doBeforeRetry(retrySignal -> log.info("连接失败，进行第 {} 次重试", retrySignal.totalRetries() + 1))
                    ).flatMap(aBoolean -> {
                        if (aBoolean){//发起cwmp连接建立
                            AtomicInteger retryCount = new AtomicInteger(0);
                            return connectSoap(retryCount,code,commandKey)
                                    .map(x->{
                                        if (authManager.compareAndSet(anonymousAuthManager,x)){
                                            //连接建立成功，定时任务监听session是否存活
                                            task = Flux.interval(Duration.ofSeconds(30)).flatMap(y -> {
                                                if (System.currentTimeMillis() -
                                                        lastResponseTime.get() >
                                                        config.getRequestTimeoutSeconds() * 1000 * (config.getRetrySendMessage()+2)) {
                                                    if (maxEnvelopes.get() <= 0) {
                                                        disconnect();
                                                        return Mono.empty();
                                                    }
                                                    CWMPDefaultFullHttpRequest aNull = httpMessageCreateFactory.createNull();
                                                    return this.send(aNull, new AtomicInteger(config.getRetrySendMessage() - 1));
                                                }
                                                return Mono.empty();
                                            }).onErrorContinue((throwable, o) -> log.warn("定时任务异常，异常原因：{}", throwable.getMessage())).subscribeOn(Schedulers.boundedElastic()).subscribe();
                                            return true;
                                        }
                                        log.warn("连接建立失败");
                                        disconnect();
                                        return false;
                                    });
                        }
                        disconnect();
                        return Mono.just(false);
                    }).onErrorResume(throwable -> {
                        log.warn("连接失败，失败原因：{}", throwable.getMessage());
                        disconnect();
                        return Mono.just(false);
                    });


    }
    private Mono<AuthManager> connectSoap(AtomicInteger retryCount, CWMPEventCode code,String commandKey) {
        if (client.getState()!=CWMPClientState.CONNECTED){
            log.warn("TCP层连接为建立，不能发送消息");
            throw new RuntimeException("TCP层连接为建立，不能发送消息");
        }
        CWMPDefaultFullHttpRequest inform = httpMessageCreateFactory.createHttpRequest(SoapCreateFactory
                .createBootstrapInform(config.getDeviceInfo(),
                        code, retryCount.getAndIncrement(),commandKey));
        return client
                .send(inform)
                .flatMap(response->{
                    String cookie = response.getDefaultHttpResponse().headers().get("Set-Cookie");
                    if (response.getDefaultHttpResponse().status().code() == 401){//需要授权
                        String head = response.getDefaultHttpResponse().headers().get("WWW-Authenticate");
                        Map<String, String> paramsMap = parseAuthHeader(head);
                        AuthManager digestAuthManager = new DigestAuthManager(cookie,
                                config.getUserName(),
                                config.getPassword(),paramsMap.get("realm"),paramsMap.get("nonce"),config.getRequestUrl(),"POST",paramsMap.get("qop"));

                        CWMPDefaultFullHttpRequest inform2 = httpMessageCreateFactory.createHttpRequest(SoapCreateFactory
                                .createBootstrapInform(config.getDeviceInfo(),
                                        code, retryCount.getAndIncrement(),commandKey));

                        digestAuthManager.authenticate(inform2.getDefaultFullHttpRequest());
                        return client
                                .send(inform2).flatMap(response1-> Mono.just(Tuples.of(response1,digestAuthManager)));
                    }
                    return Mono.just(Tuples.of(response,new AnonymousAuthManager(cookie)));
                }) .<AuthManager>handle((tuple2, sink) -> {
                    CWMPDefaultFulHttpResponse response = tuple2.getT1();
                    try{
                            String contentXml = response.getContentXml();
                            ACSInformResponseEnvelope unmarshal = JAXBUtils.unmarshal(contentXml, ACSInformResponseEnvelope.class);
                            if (!maxEnvelopes.compareAndSet(0,unmarshal.getBody().getInformResponse().getMaxEnvelopes())){
                                //上次连接未关闭
                                sink.error(new RuntimeException("上次连接未关闭"));
                                return;
                            }
                    }catch (Exception e) {
                            sink.error(new RuntimeException("解析消息失败: " + response.getContentXml(), e));
                            return;
                    }
                    sink.next(tuple2.getT2());
                    sink.complete();
                })
                .onErrorResume(throwable -> {
                    log.warn("发送建立连接消息失败，失败原因：{}", throwable.getMessage());
                    if (retryCount.get() < config.getRetrySendMessage()){
                        log.info("开始重发建立连接消息");
                        return Mono.delay(Duration.ofMillis(config.getRetrySendMessageDelayMillis()))
                                .flatMap(ignore -> connectSoap(retryCount,code,""));
                    }
                    throw new RuntimeException(throwable);
                });

    }

    private Map<String, String> parseAuthHeader(String authHeader) {
        Map<String, String> params = new HashMap<>();
        // 移除 "Digest " 前缀
        String digestPart = authHeader.substring(7);

        // 分割参数
        String[] pairs = digestPart.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim();
                String value = keyValue[1].trim();
                // 移除引号
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                params.put(key, value);
            }
        }

        return params;
    }
    public void disconnect(){
       authManager.set(null);
        maxEnvelopes.set(0);
        if (task != null){
            task.dispose();
            task = null;
        }
        client.disconnect();
    }
    public boolean isAlive(){
        return authManager.get()!=null;
    }

    public Mono<Void> send(CWMPDefaultFullHttpRequest request
            , AtomicInteger retryCount){
        AuthManager authManager1 = authManager.get();
        if (authManager1 !=null && !authManager1.equals(anonymousAuthManager)){
            DefaultFullHttpRequest defaultFullHttpRequest = request.getDefaultFullHttpRequest();
            authManager1.authenticate(defaultFullHttpRequest);
            return   client
                    .send(request)
                    .onErrorResume(throwable -> {
                        log.warn("发送消息失败，失败原因：{}", throwable.getMessage());
                        if (throwable instanceof CWMPNettyClient.InvalidClientStateException){//如果是TCP连接状态异常，不在重发
                            return Mono.empty();
                        }
                        if (retryCount.incrementAndGet() < config.getRetrySendMessage()){
                            log.info("开始重发消息");
                            return   Mono.delay(Duration.ofMillis(config.getRetrySendMessageDelayMillis()))
                                    .flatMap(ignore -> client.send(request))
                                    ;
                        }
                        log.error("消息重发{}次失败", config.getRetrySendMessage());
                        this.disconnect();
                        return Mono.empty();
                    })
                    .doOnNext(response->{
                        lastResponseTime.set(System.currentTimeMillis());
                        if (!StringUtils.hasText( response.getContentXml())){
                            log.info("该次请求结束");
                            this.disconnect();
                            return;
                        }
                        //使用多线程下发请求，避免调用链过长，栈溢出
                        if (maxEnvelopes.get() > 0){
                            maxEnvelopes.decrementAndGet();
                        }
                        dispatchMessageMethodHandler
                                .dispatch(response)
                                .subscribeOn(Schedulers.boundedElastic())
                                .subscribe();
                    })
                    .then();
        }

        return Mono.error(new RuntimeException("当前会话未建立或正在建立中"));
    }


    @Override
    public void run(String... args) throws Exception {
        //注册handler
      context.getBeansOfType(Handler.class).values().forEach(dispatchMessageMethodHandler::registerHandler);
    }

}
