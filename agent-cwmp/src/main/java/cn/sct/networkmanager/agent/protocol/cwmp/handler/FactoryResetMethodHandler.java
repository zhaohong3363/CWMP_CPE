package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.FactoryReset;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FactoryResetMethodHandler implements Handler {

    private static final Logger log = Loggers.getLogger(FactoryResetMethodHandler.class);
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    public FactoryResetMethodHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public HttpMessageCreateFactory getHttpMessageCreateFactory() {
        return httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) {
        try{
            Mono.delay(Duration.ofSeconds(30))
                    .doOnNext(x-> this.reset())
                    .onErrorResume(e-> {
                        log.info("FactoryResetMethodHandler error:{}", e.getMessage());
                        return Mono.empty();
                    })
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            return responseMessage(SoapCreateFactory.createFactoryResetResponse(envelope.getId()), session);
        }catch (Exception e){
            log.warn("RebootMethodHandler error:{}", e.getMessage());
            return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), e.getMessage()), session);
        }
    }

    @Override
    public String getFunctionName() {
        return "FactoryReset";
    }

    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(FactoryReset.class);
    }
    /*
    *
    * */
    public abstract void reset();
}
