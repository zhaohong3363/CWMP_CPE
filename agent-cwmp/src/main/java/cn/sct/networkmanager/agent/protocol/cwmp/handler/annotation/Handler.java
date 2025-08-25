package cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation;

import cn.sct.networkmanager.agent.domain.model.Envelope;
import cn.sct.networkmanager.agent.domain.model.acs.AcsMethodRequestEnvelope;
import cn.sct.networkmanager.agent.protocol.cwmp.DefaultCwmpSession;
import cn.sct.networkmanager.agent.protocol.cwmp.http.HttpMessageCreateFactory;
import cn.sct.networkmanager.agent.transport.netty.CWMPDefaultFullHttpRequest;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public interface Handler {

    Mono<Void> handle(AcsMethodRequestEnvelope envelope, DefaultCwmpSession  session) throws IOException, InterruptedException;

    String getFunctionName();

    <T> boolean match(Class<T> tClass);

    HttpMessageCreateFactory getHttpMessageCreateFactory();
    default Mono<Void> responseMessage(Envelope envelope, DefaultCwmpSession session) {
        CWMPDefaultFullHttpRequest httpRequest = getHttpMessageCreateFactory().createHttpRequest(envelope);
        return session.send(httpRequest, new AtomicInteger(0));
    }
}
