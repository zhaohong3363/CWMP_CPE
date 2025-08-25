package cn.sct.networkmanager.agent.protocol.cwmp.handler.annotation;

import reactor.core.publisher.Mono;

public interface RebootHandler {
    Mono<Void> handle();
}
