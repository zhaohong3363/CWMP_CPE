package cn.sct.networkmanager.agent.protocol.cwmp;

import io.netty.handler.codec.http.DefaultFullHttpRequest;

public interface AuthManager {
    void authenticate(DefaultFullHttpRequest request);
}
