package cs.escaperoomhub.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class PlaceReservationRequest {
    @NotNull
    Long reservationId;
}
