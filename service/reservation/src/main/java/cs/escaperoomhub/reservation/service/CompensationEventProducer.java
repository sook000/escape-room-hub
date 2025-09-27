package cs.escaperoomhub.reservation.service;

import cs.escaperoomhub.common.event.EventType;
import cs.escaperoomhub.common.event.payload.CompensationRequiredEventPayload;
import cs.escaperoomhub.common.outboxmessagerelay.OutboxEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CompensationEventProducer {
    private final OutboxEventPublisher outboxEventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishCompensationRequired(Long reservationId, String reason) {
        outboxEventPublisher.publish(
                EventType.COMPENSATION_REQUIRED,
                CompensationRequiredEventPayload.builder()
                        .reservationId(reservationId)
                        .reason(reason)
                        .occurredAt(LocalDateTime.now())
                        .build(),
                reservationId
        );
    }
}