package cn.sct.networkmanager.agent.protocol.cwmp.http;

import cn.sct.networkmanager.agent.config.CPEConfigProperties;
import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.springframework.util.StringUtils;

public class HttpMessageCreateFactory {
	private final CPEConfigProperties cpeConfigProperties;

    public HttpMessageCreateFactory(CPEConfigProperties cpeConfigProperties) {
        this.cpeConfigProperties = cpeConfigProperties;
    }

    public  CWMPDefaultFullHttpRequest createHttpRequest(Envelope envelope)
    {
		ByteBuf content = envelope.content();
		DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1,
				HttpMethod.POST,
				cpeConfigProperties.getRequestUrl(),
				content
				);
		CWMPDefaultFullHttpRequest cwmpDefaultFullHttpRequest = new CWMPDefaultFullHttpRequest(
				envelope.getId(),
				envelope,
				defaultFullHttpRequest);
		defaultFullHttpRequest.headers().set("Content-Type", "text/xml; charset=UTF-8");
		defaultFullHttpRequest.headers().set("Content-Length", content.readableBytes());
		defaultFullHttpRequest.headers().set("host", "192.168.17.213:80");
		defaultFullHttpRequest.headers().set("Accept", "*/*");
		return cwmpDefaultFullHttpRequest;

    }


	public  CWMPDefaultFullHttpRequest createNull()
	{
		DefaultFullHttpRequest defaultFullHttpRequest = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1,
				HttpMethod.POST,
				cpeConfigProperties.getRequestUrl()
		);
		CWMPDefaultFullHttpRequest cwmpDefaultFullHttpRequest = new CWMPDefaultFullHttpRequest(
				"",
				null,
				defaultFullHttpRequest);
		defaultFullHttpRequest.headers().set("Content-Length", 0);
		defaultFullHttpRequest.headers().set("host", "192.168.17.213:80");
		return cwmpDefaultFullHttpRequest;

	}

}
