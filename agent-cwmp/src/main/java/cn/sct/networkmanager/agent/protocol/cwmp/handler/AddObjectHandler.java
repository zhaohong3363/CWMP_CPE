package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.AddObject;
import cn.sct.networkmanager.agent.domain.model.acs.Download;
import cn.sct.networkmanager.agent.domain.model.acs.Reboot;
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

public abstract class AddObjectHandler implements Handler {

    private static final Logger log = LoggerFactory.getLogger(AddObjectHandler.class);
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    public AddObjectHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) throws IOException, InterruptedException {
        AddObject methodRequest = (AddObject) envelope.getBody().getMethodRequest();
        return add(methodRequest).flatMap(result -> {
            if (result.flag){//发送报文
                return responseMessage(SoapCreateFactory.createAddObjectResponse(envelope.getId(),
                        0, result.instanceNumber),session);
            }
            //发送
            return responseMessage(SoapCreateFactory.createAddObjectResponse(envelope.getId(),
                    1, result.instanceNumber),session);
        }).onErrorResume(throwable -> {
            log.warn("add object error", throwable);
           return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(),
                   FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(),
                   "add object error"),session);
        });
    }

    @Override
    public String getFunctionName() {
        return "AddObject";
    }

    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(AddObject.class);
    }

    @Override
    public HttpMessageCreateFactory getHttpMessageCreateFactory() {
        return httpMessageCreateFactory;
    }

    /**
     * 向设备中添加对象
     * **/
    public abstract Mono<Result> add(AddObject addObject);

    @Getter
    @Setter
    @AllArgsConstructor
    public class Result {
        private boolean flag;//添加成功并应用返回true，添加成功为应用返回false
        private int instanceNumber;//实例编号
    }

}
