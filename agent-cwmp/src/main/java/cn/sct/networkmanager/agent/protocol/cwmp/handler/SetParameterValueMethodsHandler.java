package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import cn.sct.agent.item.Item;
import cn.sct.agent.item.Permission;
import cn.sct.networkmanager.agent.domain.enums.FaultCode;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.domain.model.acs.SetParameterValues;
import cn.sct.networkmanager.agent.element.CWMPElementManager;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.protocol.cwmp.soap.SoapCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SetParameterValueMethodsHandler implements Handler {
    private final HttpMessageCreateFactory httpMessageCreateFactory;
    private final ApplicationEventPublisher publisher;
    private static final Logger log = Loggers.getLogger(SetParameterValueMethodsHandler.class);

    public SetParameterValueMethodsHandler(HttpMessageCreateFactory httpMessageCreateFactory, ApplicationEventPublisher  publisher) {
        this.httpMessageCreateFactory = httpMessageCreateFactory;
        this.publisher = publisher;
    }
    @Override
    public Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession session) {
        try{
            SetParameterValues methodRequest = (SetParameterValues) envelope.getBody().getMethodRequest();
            SetParameterValues.ParameterList parameterList = methodRequest.getParameterList();
            List<SetParameterValues.ParameterValueStruct> parameters = parameterList.getParameters();
            ParameterTransactionContext transactionContext = new ParameterTransactionContext();
            for (SetParameterValues.ParameterValueStruct parameterValueStruct : parameters){
                String name = parameterValueStruct.getName();
                Item<?> itemElement = CWMPElementManager.getItemElement(name);
                if (itemElement == null) {
                    return handleParameterError(session, envelope, FaultCode.INVALID_PARAMETER_9003.getCode(),
                            name + " parameter does not exist ");
                }
                if (itemElement.getPermission().getCode() <= Permission.READ.getCode()) {
                    return handleParameterError(session, envelope, FaultCode.PARAMETER_READ_ONLY.getCode(),
                            name + " parameter read only ");
                }
                transactionContext.addOriginalValue(name, itemElement.getValue());
                SetParameterValues.TypedValue value = parameterValueStruct.getValue();
                try {
                    SoapCreateFactory.determineValue(value.getValue(), value.getType());
                } catch (Exception e) {
                    return handleParameterError(session, envelope, FaultCode.INVALID_PARAMETER_VALUE.getCode(),
                            name + " parameter value invalid: " + e.getMessage());
                }
            }
            try {
                for (SetParameterValues.ParameterValueStruct parameterValueStruct : parameters) {
                    String name = parameterValueStruct.getName();
                    Item<Object> itemElement = CWMPElementManager.getItemElement(name);
                    SetParameterValues.TypedValue value = parameterValueStruct.getValue();
                    Object convertedValue = SoapCreateFactory.determineValue(value.getValue(), value.getType());
                    itemElement.setValue(convertedValue);
                    transactionContext.markAsApplied(name);
                }
                String parameterKey = methodRequest.getParameterKey();
                Item<Object> parameterKeyItem =  CWMPElementManager.getItemElement("Device.ManagementServer.ParameterKey");
                if (parameterKeyItem != null) {
                    transactionContext.addOriginalValue("Device.ManagementServer.ParameterKey", parameterKeyItem.getValue());
                    parameterKeyItem.setValue(parameterKey);
                    transactionContext.markAsApplied("Device.ManagementServer.ParameterKey");
                }
                return responseMessage(SoapCreateFactory.createSetParameterValuesResponse(envelope.getId(), 0), session);
            } catch (Exception e) {
                transactionContext.rollback();
                log.warn("SetParameterValueMethodsHandler execution error:{}", e.getMessage());
                return handleParameterError(session, envelope, FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(),
                        "Execution failed: " + e.getMessage());
            }
        } catch (Exception e) {
            log.warn("SetParameterValueMethodsHandler error:{}", e.getMessage());
            return handleParameterError(session, envelope, FaultCode.METHOD_NOT_SUPPORTED_9000.getCode(),
                    "General error: " + e.getMessage());
        }
    }
    @Override
    public String getFunctionName() {
        return "SetParameterValues";
    }
    @Override
    public <T> boolean match(Class<T> tClass) {
          return tClass.equals(SetParameterValues.class);
    }

    @Override
    public HttpMessageCreateFactory getHttpMessageCreateFactory() {
        return httpMessageCreateFactory;
    }

    /**
     * 处理参数错误的统一方法
     */
    private Mono<Void> handleParameterError(DefaultCwmpSession session, AcsMethodRequestEnvelope envelope,
                                            int errorCode, String errorMessage) {
        return responseMessage(SoapCreateFactory.createFaultResponse(envelope.getId(), errorCode, errorMessage),session);
    }

    /**
     * 参数设置事务上下文
     */
    private static class ParameterTransactionContext {
        private final Map<String, Object> originalValues = new HashMap<>();
        private final Set<String> appliedChanges = new HashSet<>();
        public void addOriginalValue(String paramName, Object originalValue) {
            originalValues.put(paramName, originalValue);
        }
        public void markAsApplied(String paramName) {
            appliedChanges.add(paramName);
        }
        public void rollback() {
            for (String paramName : appliedChanges) {
                Item<Object> itemElement =  CWMPElementManager.getItemElement(paramName);
                if (itemElement != null && originalValues.containsKey(paramName)) {
                    itemElement.setValue(originalValues.get(paramName));
                }
            }
        }
    }
}
