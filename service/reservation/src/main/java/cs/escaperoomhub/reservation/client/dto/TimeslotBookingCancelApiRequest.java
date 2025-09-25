package cs.escaperoomhub.reservation.client.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeslotBookingCancelApiRequest {
    String requestId;
}
