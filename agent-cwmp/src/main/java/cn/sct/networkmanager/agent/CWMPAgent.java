package cn.sct.networkmanager.agent;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.config.CPEConfigProperties;
import cn.sct.networkmanager.agent.domain.enums.CWMPEventCode;
import cn.sct.networkmanager.agent.domain.enums.CWMPServeState;
import cn.sct.networkmanager.agent.element.CWMPElementManager;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPNettyClient;
import cn.sct.networkmanager.agent.transport.netty.CWMPNettyServe;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class CWMPAgent implements ApplicationListener<CWMPNettyServe.CWMPServeStateEvent>, CommandLineRunner {
    private static final Logger log = Loggers.getLogger(CWMPAgent.class);
    private final CWMPNettyServe serve;
    private final DefaultCwmpSession session;
    private final boolean isBootStart;
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    public CWMPAgent(DefaultCwmpSession session, CPEConfigProperties cpeConfigProperties, HttpMessageCreateFactory httpMessageCreateFactory, ApplicationEventPublisher eventPublisher) {
        this.serve =  new CWMPNettyServe(cpeConfigProperties,session,httpMessageCreateFactory,cpeConfigProperties.getReceiveUrl(),eventPublisher);;
        this.session = session;
        this.isBootStart = cpeConfigProperties.isBootstrap();
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }
    public void start (){
        //打开http 服务
        serve.start();
        //设备启动建立第一次连接
        if (isBootStart){
            session
                    .connect(CWMPEventCode.BOOTSTRAP,"")
                    .flatMap(aboolean -> {
                        if (aboolean){
                            return session.send(httpMessageCreateFactory.createNull(), new AtomicInteger(0));
                        }
                        return Mono.empty();
                    }).subscribe();
        }else{
            session
                    .connect(CWMPEventCode.BOOT,"")
                    .flatMap(aboolean -> {
                        if ( aboolean){
                            return session.send(httpMessageCreateFactory.createNull(), new AtomicInteger(0));
                        }
                        return Mono.empty();
                    }).subscribe();
        }
        //启动定时
        schedulePeriodicInform();
    }

    public void schedulePeriodicInform() {
        Item<Object> itemElement = CWMPElementManager.getItemElement("InternetGatewayDevice.ManagementServer.PeriodicInformInterval");
        if (itemElement == null){
            throw new RuntimeException("InternetGatewayDevice.ManagementServer.PeriodicInformInterval not found");
        }
        Flux.interval(Duration.ofSeconds(Long.parseLong(itemElement.getValue().toString()))
                        ,Duration.ofSeconds(Long.parseLong(itemElement.getValue().toString())))
                .flatMap(x-> session.connect(CWMPEventCode.PERIODIC,"").flatMap(aboolean -> {
                    if (aboolean){
                        return session.send(httpMessageCreateFactory.createNull(), new AtomicInteger(0));
                    }
                    return Mono.empty();
                })).onErrorContinue((e,x)-> log.error("schedulePeriodicInform error",e))
                .subscribeOn(Schedulers.boundedElastic()).subscribe();
    }
    @Override
    public void run(String... args) throws Exception {
        start();
    }

    @Override
    public void onApplicationEvent(CWMPNettyServe.CWMPServeStateEvent event) {
        //服务端关闭，重开服务端
        if (event.getCwmpServeState() == CWMPServeState.CLOSED){
            log.info("CWMPNettyServe closed, restarting...");
            serve.start();
            if(serve.getState() != CWMPServeState.started){
                log.error("CWMPNettyServe restart failed");
                Mono.delay(Duration.ofSeconds(5)).doOnNext(x-> serve.start())
                        .subscribeOn(Schedulers.boundedElastic()).subscribe();
            }
        }
    }
}
