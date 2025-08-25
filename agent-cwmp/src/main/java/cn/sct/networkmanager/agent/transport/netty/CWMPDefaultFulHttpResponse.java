package cn.sct.networkmanager.agent.transport.netty;

import io.netty.handler.codec.http.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CWMPDefaultFulHttpResponse   {
    private  final String contentXml;
    private  final String responseId;
    private final FullHttpResponse defaultHttpResponse;


    public CWMPDefaultFulHttpResponse(String contentXml, String responseId, FullHttpResponse defaultHttpResponse) {
        this.contentXml = contentXml;
        this.responseId = responseId;
        this.defaultHttpResponse = defaultHttpResponse;
    }
}
