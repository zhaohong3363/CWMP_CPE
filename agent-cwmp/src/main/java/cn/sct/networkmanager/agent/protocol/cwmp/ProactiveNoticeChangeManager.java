package cn.sct.networkmanager.agent.protocol.cwmp;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

public class ProactiveNoticeChangeManager {
    private static final Sinks.Many<NoticeChange> processor = Sinks.many().multicast().onBackpressureBuffer();
    private static final Logger log = LoggerFactory.getLogger(ProactiveNoticeChangeManager.class);

    public static void notice(String name, Object value) {
        processor.tryEmitNext(new NoticeChange(name, value));
    }

    public static Flux<NoticeChange> subscribe() {
        return processor.asFlux();
    }

    @Getter
    @Setter
    public static class NoticeChange{
        private final String name;
        private final Object value;

        NoticeChange(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }
}
