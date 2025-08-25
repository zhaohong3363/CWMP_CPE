package cn.sct.networkmanager.agent.protocol.cwmp;

import io.netty.handler.codec.http.DefaultFullHttpRequest;

public class AnonymousAuthManager implements AuthManager{
    private final String cookie;

    public AnonymousAuthManager(String cookie) {
        this.cookie = cookie;
    }

    @Override
    public void authenticate(DefaultFullHttpRequest request) {
        request.headers().set("Cookie", cookie);
    }
}
