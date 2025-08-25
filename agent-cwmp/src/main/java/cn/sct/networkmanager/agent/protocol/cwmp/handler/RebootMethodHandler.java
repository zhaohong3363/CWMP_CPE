package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.CWMPEventCode;
import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.Reboot;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.RebootHandler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import org.springframework.util.StringUtils;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RebootMethodHandler implements Handler {
    private static final Logger log = Loggers.getLogger(RebootMethodHandler.class);
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    public RebootMethodHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) {
        try {
            Reboot methodRequest = (Reboot) envelope.getBody().getMethodRequest();
            String commandKey = methodRequest.getCommandKey();
            //延迟调用重启方法
            Mono.delay(Duration.ofSeconds(30)).flatMap(ig -> {
                        Disposable timeOutTask = Mono.delay(Duration.ofSeconds(30)).doOnNext(x -> {
                            log.info("{}:重启设备超时", System.currentTimeMillis());
                            session.disconnect();
                        }).subscribeOn(Schedulers.boundedElastic()).subscribe();
                        while (session.isAlive() ) {}
                        timeOutTask.dispose();
                        log.info("{}:开始重启设备", System.currentTimeMillis());
                        return this.reboot();
                    }).flatMap(flag->{
                        if (flag){//重启成功
                            return session.connect(CWMPEventCode.MReboot,commandKey).flatMap(aBoolean -> {
                                    if (aBoolean){
                                        return session.send(httpMessageCreateFactory.createNull(),
                                                new AtomicInteger(0));
                                    }
                                    return Mono.empty().then();
                            });
                        }
                        return Mono.empty().then();
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            return responseMessage(SoapCreateFactory.createRebootResponse(envelope.getId()),session);
        } catch (Exception e) {
            log.warn("RebootMethodHandler error:{}", e.getMessage());
            return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), e.getMessage()),session);
        }
    }
    @Override
    public String getFunctionName() {
        return "Reboot";
    }
    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(Reboot.class);
    }
    /**
     * 应该等待设备重启成功后返回 true,否则返回false，重启后会想ACS发起重启完成的infrom
     *
     * @return
     */
    public abstract Mono<Boolean> reboot();

}
