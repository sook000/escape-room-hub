package cs.escaperoomhub.reservation.client.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeslotBookingApiRequest {
    private String requestId;
    private Long userId;
    private Long timeslotId;
    private Long personCount;

}
