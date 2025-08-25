package cn.sct.networkmanager.agent.element;

import cn.sct.agent.item.Item;
import cn.sct.networkmanager.agent.annotation.CWMPItem;
import cn.sct.networkmanager.agent.config.CPEConfigProperties;
import cn.sct.networkmanager.agent.domain.CWMPElement;
import cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation.Handler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.CommandLineRunner;

public class RegisterCWMPElement implements BeanPostProcessor, CommandLineRunner {
    private final CPEConfigProperties cpeConfigProperties;

    public RegisterCWMPElement(CPEConfigProperties cpeConfigProperties) {
        this.cpeConfigProperties = cpeConfigProperties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        CWMPItem annotation = bean.getClass().getAnnotation(CWMPItem.class);
        if (annotation !=null){
            if (bean instanceof CWMPElement){
                CWMPElementManager.registerItemElement(annotation.name(),(CWMPElement) bean);
            }
            if (bean instanceof Item<?>){
                CWMPElementManager.registerItemElement(annotation.name(),(Item<Object>) bean);
            }

        }
        if (bean instanceof Handler){
            CWMPElementManager.registerFunction(((Handler) bean).getFunctionName());
        }
        return BeanPostProcessor.super.postProcessAfterInitialization(bean, beanName);
    }

    @Override
    public void run(String... args) throws Exception {
        cpeConfigProperties
                .getDeviceInfo()
                .getParameter()
                .forEach((k,v)-> CWMPElementManager.registerItemElement(k, new Item<Object>() {
                    private Object value=v;
                    @Override
                    public Object setValue(Object value) {
                        //只更新内存的值，设备重启会丢失配置key 如Device.ManagementServer.ParameterKey
                        this.value=value;
                        return Item.super.setValue(value);
                    }

                    @Override
                    public Object getValue() {
                        return value;
                    }
                }));
    }
}
