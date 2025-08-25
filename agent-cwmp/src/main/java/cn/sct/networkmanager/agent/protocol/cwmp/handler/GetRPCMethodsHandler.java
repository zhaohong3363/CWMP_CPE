package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.GetRPCMethods;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.concurrent.atomic.AtomicInteger;

public class GetRPCMethodsHandler implements Handler {
    private final HttpMessageCreateFactory httpMessageCreateFactory;
    private static final Logger log = Loggers.getLogger(GetRPCMethodsHandler.class);

    public GetRPCMethodsHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) {
        try{
            return responseMessage( SoapCreateFactory.createGetRpcMethodResponse(envelope.getId()), session);
        }catch (Exception e){
            log.warn("GetRPCMethodsHandler error:{}", e.getMessage());
            return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), e.getMessage()), session);
        }
    }

    @Override
    public String getFunctionName() {
        return "GetRPCMethods";
    }

    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(GetRPCMethods.class);
    }

    @Override
    public HttpMessageCreateFactory getHttpMessageCreateFactory() {
        return httpMessageCreateFactory;
    }
}
