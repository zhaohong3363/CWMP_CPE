package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.AddObject;
import cn.sct.networkmanager.agent.domain.model.acs.DeleteObject;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;

public abstract class DeleteObjectHandler implements Handler {

    private static final Logger log = LoggerFactory.getLogger(DeleteObjectHandler.class);
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    public DeleteObjectHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) throws IOException, InterruptedException {
        DeleteObject methodRequest = (DeleteObject) envelope.getBody().getMethodRequest();
        return delete(methodRequest).flatMap(aBoolean -> {
            if (aBoolean){//发送报文
                return responseMessage(SoapCreateFactory.createDeleteObjectResponse(envelope.getId(),
                        0),session);
            }
            //发送
            return responseMessage(SoapCreateFactory.createDeleteObjectResponse(envelope.getId(),
                    1),session);
        }).onErrorResume(throwable -> {
            log.warn("delete object error", throwable);
           return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                   FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(),
                   "delete object error"),session);
        });
    }

    @Override
    public String getFunctionName() {
        return "DeleteObject";
    }

    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(DeleteObject.class);
    }

    @Override
    public HttpMessageCreateFactory getHttpMessageCreateFactory() {
        return httpMessageCreateFactory;
    }

    /**
     * 向设备中添加对象
     * **/
    public abstract Mono<Boolean> delete(DeleteObject deleteObject);



}
