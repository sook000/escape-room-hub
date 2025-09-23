package cs.escaperoomhub.monolithic.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlaceReservationRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long timeslotId;

    @NotNull @Min(value = 1)
    private Long personCount;
}
