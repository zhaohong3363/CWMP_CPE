package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.GetParameterValues;
import cn.sct.networkmanager.agent.element.CWMPElementManager;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GetParameterValuesMethodHandler implements Handler {
    private final HttpMessageCreateFactory httpMessageCreateFactory;
    private static final Logger log = Loggers.getLogger(GetParameterValuesMethodHandler.class);

    public GetParameterValuesMethodHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) {
        try{
            GetParameterValues methodRequest = (GetParameterValues) envelope.getBody().getMethodRequest();
            GetParameterValues.ParameterNames parameterNames = methodRequest.getParameterNames();
            Map<String,Object> parameterValues = new HashMap<>();
            for (String name : parameterNames.getNames()) {
                Item<?> itemElement = CWMPElementManager.getItemElement(name);
                if (itemElement == null){
                    return responseMessage( SoapCreateFactory.createFaultResponse(envelope.getId(),
                            FaultCode.PARAMETER_NOT_EXISTS.getCode(), name+" parameter does not exist "), session);
                }
                parameterValues.put(name, itemElement.getValue());
            }
            Envelope getParameterValuesResponse = SoapCreateFactory.createGetParameterValuesResponse(envelope.getId(), parameterValues);
            CWMPDefaultFullHttpRequest httpRequest = httpMessageCreateFactory.createHttpRequest(getParameterValuesResponse);
            return session.send(httpRequest,new AtomicInteger(0));
        }catch (Exception e){
            log.warn("GetParameterValuesMethodHandler error:{}", e.getMessage());
            return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), e.getMessage()),session);
        }
    }

    @Override
    public String getFunctionName() {
        return "GetParameterValues";
    }

    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(GetParameterValues.class);
    }

    @Override
    public HttpMessageCreateFactory getHttpMessageCreateFactory() {
        return httpMessageCreateFactory;
    }
}
