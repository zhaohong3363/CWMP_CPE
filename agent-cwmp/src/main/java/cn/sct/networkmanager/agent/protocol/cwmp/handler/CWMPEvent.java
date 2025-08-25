package cn.sct.networkmanager.agent.protocol.cwmp.handler;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CWMPEvent extends ApplicationEvent {


    public CWMPEvent(String source) {
        super(source);
    }
}
