package cs.escaperoomhub.store.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TimeslotBookingCancelRequest {
    @NotNull
    String requestId;
}
