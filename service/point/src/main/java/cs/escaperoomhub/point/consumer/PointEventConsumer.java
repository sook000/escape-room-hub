package cs.escaperoomhub.point.consumer;

import cs.escaperoomhub.common.event.Event;
import cs.escaperoomhub.common.event.EventPayload;
import cs.escaperoomhub.common.event.EventType;
import cs.escaperoomhub.point.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointEventConsumer {
    private final PointService pointService;

    @KafkaListener(topics = {
            EventType.Topic.ADVANCED_BOARD_ARTICLE,
    })
    public void listen(String message, Acknowledgment ack) {
        log.info("[PointEventConsumer.listen] received message={}", message);
        Event<EventPayload> event = Event.fromJson(message);
        if (event != null) {
            pointService.handleEvent(event);
        }
        ack.acknowledge();
    }
}
