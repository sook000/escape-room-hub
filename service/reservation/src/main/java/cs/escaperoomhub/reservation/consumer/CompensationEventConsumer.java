package cs.escaperoomhub.reservation.consumer;

import cs.escaperoomhub.common.event.Event;
import cs.escaperoomhub.common.event.EventPayload;
import cs.escaperoomhub.common.event.EventType;
import cs.escaperoomhub.common.event.payload.CompensationRequiredEventPayload;
import cs.escaperoomhub.reservation.service.CompensationTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CompensationEventConsumer {
    private final CompensationTransactionService compensationTransactionService;

    @KafkaListener(topics = {EventType.Topic.RESERVATION_COMPENSATION})
    public void listen(String message, Acknowledgment ack) {
        log.info("[CompensationEventConsumer.listen] received message={}", message);

        try {
            Event<EventPayload> event = Event.fromJson(message);

            if (event != null && event.getType() == EventType.COMPENSATION_REQUIRED) {
                CompensationRequiredEventPayload payload = (CompensationRequiredEventPayload) event.getPayload();
                compensationTransactionService.executeCompensation(
                        payload.getReservationId(),
                        payload.getReason()
                );
            }
            ack.acknowledge();

        } catch (Exception e) {
            log.error("보상 트랜잭션 이벤트 처리 실패", e);
            // 재처리하거나 DLQ에 넣기
//            throw e;
        }
    }
}
