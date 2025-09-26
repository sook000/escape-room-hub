package cs.escaperoomhub.articleread.consumer;

import cs.escaperoomhub.articleread.service.ArticleReadService;
import cs.escaperoomhub.common.event.Event;
import cs.escaperoomhub.common.event.EventPayload;
import cs.escaperoomhub.common.event.EventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleReadEventConsumer {
    private final ArticleReadService articleReadService;

    @KafkaListener(topics = {
            EventType.Topic.ADVANCED_BOARD_ARTICLE,
            EventType.Topic.ADVANCED_BOARD_COMMENT,
            EventType.Topic.ADVANCED_BOARD_LIKE
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[ArticleReadEventConsumer.listen] message={}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            articleReadService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
