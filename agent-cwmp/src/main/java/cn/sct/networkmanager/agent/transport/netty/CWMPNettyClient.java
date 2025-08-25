package cn.sct.networkmanager.agent.transport.netty;

import cn.sct.networkmanager.agent.config.CPEConfigProperties;
import cn.sct.networkmanager.agent.domain.enums.CWMPClientState;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;


public class CWMPNettyClient {
    private static final Logger log = Loggers.getLogger(CWMPNettyClient.class);
    private final EventLoopGroup group = new NioEventLoopGroup(1);
    private final AtomicReference<CWMPClientState> state=new AtomicReference<>(CWMPClientState.INITIAL);
    private final ApplicationEventPublisher eventPublisher;
    private final CPEConfigProperties config;
    private Channel channel;
    private final Queue<RequestPromise> pendingRequests = new LinkedBlockingDeque<>(1);


    public CWMPNettyClient(ApplicationEventPublisher eventPublisher, CPEConfigProperties config) {
        this.eventPublisher = eventPublisher;
        this.config=config;
    }
    public boolean connect(String host,int port)  {
        if (state.get()== CWMPClientState.CLOSED){
            log.warn("{}:{}已关闭",host,port);
            return false;
        }
        if (state.compareAndSet(CWMPClientState.INITIAL, CWMPClientState.CONNECTING)
            || state.compareAndSet(CWMPClientState.DISCONNECTED, CWMPClientState.CONNECTING)
                || state.compareAndSet(CWMPClientState.ERROR, CWMPClientState.CONNECTING)
               ){
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, config.getConnectTimeoutSeconds() * 1000);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel channel) throws Exception {
                    if (config.isUseSSL()) {
                        log.info("{}:{}使用SSL", host, port);
                        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient();
                        if (config.isUseClientAuth() && config.getClientCertFile() != null && config.getClientKeyFile() != null) {
                            sslContextBuilder.keyManager(config.getClientKeyFile(), config.getClientCertFile());
                        }
                        if (config.getTrustCertFile() != null) {
                            sslContextBuilder.trustManager(config.getTrustCertFile());
                        }
                        channel.pipeline().addLast(sslContextBuilder.build().newHandler(channel.alloc()));
                    }
                    channel.pipeline().addLast(new HttpClientCodec());
                    channel.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                    channel.pipeline().addLast(new SimpleChannelInboundHandler<FullHttpResponse>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext channelHandlerContext,
                                                    FullHttpResponse fullHttpResponse) throws Exception {
                            if (fullHttpResponse.content() !=null ){
                                log.debug("收到响应:{}",fullHttpResponse.content().toString(StandardCharsets.UTF_8));
                            }
                            if (fullHttpResponse.status().code() == 200) {
                                if (fullHttpResponse.content() !=null && fullHttpResponse.content().readableBytes()>0){
                                    String contentXml =fullHttpResponse.content().toString(StandardCharsets.UTF_8);
//                                    log.info("收到响应:{}",contentXml);
                                    String responseId = Envelope.getId(contentXml);
                                    CWMPDefaultFulHttpResponse response =new CWMPDefaultFulHttpResponse(contentXml, responseId, fullHttpResponse);
                                    RequestPromise promise = pendingRequests.peek();
                                    if (promise!=null){
                                            pendingRequests.poll();
                                            promise.complete(response);
                                    }
                                    return;
                                }
                                RequestPromise promise = pendingRequests.peek();
                                if (promise!=null){//response 为空 后续断开连接
                                    pendingRequests.poll();
                                    promise.complete( new CWMPDefaultFulHttpResponse(null, null, fullHttpResponse));
                                }
                            }
                            else if (fullHttpResponse.status().code() == 204){
                                RequestPromise promise = pendingRequests.peek();
                                if (promise!=null){//response 为空 后续断开连接
                                    pendingRequests.poll();
                                    promise.complete( new CWMPDefaultFulHttpResponse(null, null, fullHttpResponse));
                                }
                            }
                            else if (fullHttpResponse.status().code() == 401){//ACS端需要账户密码验证
                                fullHttpResponse.retain();
                                RequestPromise promise = pendingRequests.peek();
                                if (promise!=null){//response 为空 后续断开连接
                                    pendingRequests.poll();
                                    promise.complete( new CWMPDefaultFulHttpResponse(null, null, fullHttpResponse));
                                }
                            }
                            else{
                                log.warn("{}:{}响应失败:{}",host,port,
                                        fullHttpResponse.status().code());
                                RequestPromise promise = pendingRequests.peek();
                                if (promise!=null){//response 为空 后续断开连接
                                    pendingRequests.poll();
                                    promise.complete( new CWMPDefaultFulHttpResponse(null, null, fullHttpResponse));
                                }
                            }
                        }
                    });
                }
            });
            try{
                ChannelFuture future = bootstrap.connect(host, port).sync();
                this.channel=future.channel();
                channel.closeFuture().addListener(ignore -> {
                    log.info("{}:{}连接断开",host,port);
                    state.set(CWMPClientState.DISCONNECTED);
                    eventPublisher.publishEvent(new CWMPClientStateEvent(this, CWMPClientState.DISCONNECTED));
                });
                state.set(CWMPClientState.CONNECTED);
                eventPublisher.publishEvent(new CWMPClientStateEvent(this, CWMPClientState.CONNECTED));
                return true;
            }catch (Exception e){
                log.warn("{}:{}连接失败:{}",host,port,e.getMessage());
                state.set(CWMPClientState.ERROR);
                eventPublisher.publishEvent(new CWMPClientStateEvent(this, CWMPClientState.ERROR));
                return false;
            }
        }
        return false;
    }
    public boolean disconnect(){
        if (state.compareAndSet(CWMPClientState.CONNECTED,CWMPClientState.DISCONNECTED)){
            pendingRequests.clear();
            if(channel!=null && channel.isOpen()){
                channel.close();
            }
            eventPublisher.publishEvent(new CWMPClientStateEvent(this, CWMPClientState.DISCONNECTED));
            return true;
        }
       return false;
    }

    public void close(){
        CWMPClientState previousState = state.getAndUpdate(current -> {
            if (current != CWMPClientState.CLOSING && current != CWMPClientState.CLOSED) {
                return CWMPClientState.CLOSING;
            }
            return current;
        });

        if (previousState != CWMPClientState.CLOSING && previousState != CWMPClientState.CLOSED) {
            if (channel != null) {
                channel.close();
            }
            group.shutdownGracefully();
            state.set(CWMPClientState.CLOSED);
            eventPublisher.publishEvent(new CWMPClientStateEvent(this, CWMPClientState.CLOSED));
        }
    }
    public CWMPClientState getState(){
        return state.get();
    }

    public Mono<CWMPDefaultFulHttpResponse> send(CWMPDefaultFullHttpRequest request){
        if (request.getEnvelope()!=null){
            log.info("发送请求:{}",request.getEnvelope().contentAsString());
        }

        if (state.get() != CWMPClientState.CONNECTED){
            return Mono.error(new InvalidClientStateException("CWMPClient is not connected"));
        }
        return Mono.create(sink -> {
            Disposable timeoutDisposable = Mono.delay(Duration.ofSeconds(config.getRequestTimeoutSeconds()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(ignore -> {
                        RequestPromise poll = pendingRequests.poll();
                        assert poll != null;
                        sink.error(new TimeoutException("Request timeout ID:" + poll.getRequestId() +
                                " 超时时间:" + config.getRequestTimeoutSeconds() + " 秒" + " 详情:\n" + (request.getEnvelope()!=null ? request.getEnvelope().contentAsString() : "")));
                    });
            String  requestId= request.getId();
            if (pendingRequests.offer(new RequestPromise(sink, timeoutDisposable, requestId))){
                channel.writeAndFlush(request.getDefaultFullHttpRequest());
            }else{
                sink.error(new RuntimeException("超过最大并发数"));
            }
        });
    }

    @Getter
    public static class CWMPClientStateEvent extends ApplicationEvent {
        private final CWMPClientState cwmpClientState;
        public CWMPClientStateEvent(CWMPNettyClient client, CWMPClientState cwmpClientState) {
            super(client);
            this.cwmpClientState = cwmpClientState;
        }
     }
     @Getter
    private static class RequestPromise {
        private final MonoSink<CWMPDefaultFulHttpResponse> sink;
        private final Disposable timeoutDisposable;
        private final String requestId;

        public RequestPromise(MonoSink<CWMPDefaultFulHttpResponse> sink, Disposable timeoutDisposable, String requestId) {
            this.sink = sink;
            this.timeoutDisposable = timeoutDisposable;
            this.requestId = requestId;
        }

        public void complete(CWMPDefaultFulHttpResponse response) {
            timeoutDisposable.dispose();
            sink.success(response);
        }

        public void error(Throwable error) {
            timeoutDisposable.dispose();
            sink.error(error);
        }
    }

    public static class InvalidClientStateException extends RuntimeException{
        public InvalidClientStateException(String message) {
            super(message);
        }
    }

}
