package cn.sct.networkmanager.agent.annotation;

import cn.sct.networkmanager.agent.config.CWMPAutoConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({CWMPAutoConfiguration.class})
public @interface EnableCWMPAgent {
}
