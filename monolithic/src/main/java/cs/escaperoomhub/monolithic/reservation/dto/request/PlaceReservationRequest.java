package cs.escaperoomhub.monolithic.reservation.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceReservationRequest {
    @NotNull
    private Long reservationId;
}
