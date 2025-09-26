package cs.escaperoomhub.articleread.service.event.handler;

import cs.escaperoomhub.common.event.Event;
import cs.escaperoomhub.common.event.EventPayload;

public interface EventHandler<T extends EventPayload> {
    void handle(Event<T> event);
    boolean supports(Event<T> event);
}
