package cs.escaperoomhub.point.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointUseRequest {

    @NotNull
    String requestId;

    @NotNull
    Long userId;

    @NotNull
    Long amount;
}
