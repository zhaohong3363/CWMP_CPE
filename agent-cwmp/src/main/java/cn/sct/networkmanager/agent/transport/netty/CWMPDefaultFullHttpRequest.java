package cn.sct.networkmanager.agent.transport.netty;

import cn.sct.networkmanager.agent.domain.model.Envelope;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import lombok.Getter;

@Getter
public class CWMPDefaultFullHttpRequest   {
    private final String id;
    private final Envelope envelope;
    private final DefaultFullHttpRequest defaultFullHttpRequest;


    public CWMPDefaultFullHttpRequest(String id,   Envelope envelope, DefaultFullHttpRequest defaultFullHttpRequest) {
        this.id = id;
        this.envelope = envelope;
        this.defaultFullHttpRequest = defaultFullHttpRequest;
    }
}
