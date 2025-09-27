package cs.escaperoomhub.common.event.payload;

import cs.escaperoomhub.common.event.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensationRequiredEventPayload implements EventPayload {
    private Long reservationId;
    private String reason;
    private LocalDateTime occurredAt;
}
