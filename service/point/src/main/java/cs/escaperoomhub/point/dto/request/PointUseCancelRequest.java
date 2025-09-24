package cs.escaperoomhub.point.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PointUseCancelRequest {
    @NotNull
    private String requestId;
}
