package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.JAXBUtils;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFulHttpResponse;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import jakarta.xml.bind.JAXBException;
import reactor.util.Logger;
import reactor.util.Loggers;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DispatchMessageMethodHandler {
    private static final Logger log = Loggers.getLogger(DispatchMessageMethodHandler.class);
    private final DefaultCwmpSession session;
    private final List<Handler> handlers=new ArrayList<>();
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    public DispatchMessageMethodHandler(DefaultCwmpSession session, HttpMessageCreateFactory httpMessageCreateFactory) {
        this.session = session;
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }
    public Mono<Void> dispatch(CWMPDefaultFulHttpResponse response) {
        if (!StringUtils.hasText( response.getContentXml())){
            return Mono.empty();
        }
        try{
            AcsMethodRequestEnvelope envelope = JAXBUtils.unmarshal(response.getContentXml(), AcsMethodRequestEnvelope.class);
            for (Handler handler : handlers) {
                if (handler.match(envelope.getBody().getMethodRequest().getClass())){
                    return handler.handle(envelope, session);
                }
            }
            log.warn("方法不被支持：{}", response.getContentXml());
            Envelope faultResponse = SoapCreateFactory.createFaultResponse(envelope.getId(),
                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), "The method is not supported ");
            CWMPDefaultFullHttpRequest httpRequest = httpMessageCreateFactory.createHttpRequest(faultResponse);
            return session.send(httpRequest,new AtomicInteger(0));
        }catch (JAXBException e){
            log.warn("XML转化失败：{},失败原因:{}", response.getContentXml(),e.getMessage());
            Envelope faultResponse = SoapCreateFactory.createFaultResponse("",
                    FaultCode.REQUEST_FORMAT_ERROR.getCode(), e.getMessage());
            CWMPDefaultFullHttpRequest httpRequest = httpMessageCreateFactory.createHttpRequest(faultResponse);
            return session.send(httpRequest,new AtomicInteger(0));
        }catch (Exception e){
            log.warn("DispatchMessageMethodHandler error:{}", e.getMessage());
            Envelope faultResponse = SoapCreateFactory.createFaultResponse("",
                    FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(), e.getMessage());
            CWMPDefaultFullHttpRequest httpRequest = httpMessageCreateFactory.createHttpRequest(faultResponse);
            return session.send(httpRequest,new AtomicInteger(0));
        }
    }

    public void registerHandler(Handler handler) {
        handlers.add(handler);
    }
}
