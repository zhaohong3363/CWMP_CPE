package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.domain.CWMPElement;
import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.Reboot;
import cn.sct.networkmanager.agent.domain.model.acs.SetParameterAttributes;
import cn.sct.networkmanager.agent.element.CWMPElementManager;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.IllegalFormatCodePointException;
import java.util.List;

public class SetParameterAttributesHandler implements Handler {
    private final HttpMessageCreateFactory httpMessageCreateFactory;

    public SetParameterAttributesHandler(HttpMessageCreateFactory httpMessageCreateFactory) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
    }

    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) throws IOException, InterruptedException {
        SetParameterAttributes methodRequest = (SetParameterAttributes) envelope.getBody().getMethodRequest();
        SetParameterAttributes.ParameterList parameterList = methodRequest.getParameterList();
        List<SetParameterAttributes.SetParameterAttributesStruct> setParameterAttributesStructs = parameterList.getSetParameterAttributesStructs();
        for (SetParameterAttributes.SetParameterAttributesStruct setParameterAttributesStruct : setParameterAttributesStructs){
            String name = setParameterAttributesStruct.getName();
            Item<?> itemElement = CWMPElementManager.getItemElement(name);
            if (itemElement instanceof CWMPElement){
                CWMPElement<Object> element = (CWMPElement<Object>) itemElement;
                if (setParameterAttributesStruct.getAccessListChange()==1){//权限列表被修改

                }
                if (setParameterAttributesStruct.getNotificationChange()==1){//通知方式被修改
                    element.setNotification(CWMPElement.Notification.fromCode(setParameterAttributesStruct.getNotification()));
                }
            }else{
                return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(), FaultCode.PARAMETER_TYPE_ERROR.getCode(), "Parameter values are not allowed to be modified"),session);
            }
        }
        return responseMessage(SoapCreateFactory.createSetParameterAttributesResponse(envelope.getId()),session);
    }

    @Override
    public String getFunctionName() {
        return "SetParameterAttributes";
    }

    @Override
    public <T> boolean match(Class<T> tClass) {
        return tClass.equals(SetParameterAttributes.class);
    }

    @Override
    public HttpMessageCreateFactory getHttpMessageCreateFactory() {
        return httpMessageCreateFactory;
    }
}
