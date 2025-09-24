package cs.escaperoomhub.point.controller;

import cs.escaperoomhub.point.dto.request.PointUseCancelRequest;
import cs.escaperoomhub.point.dto.request.PointUseRequest;
import cs.escaperoomhub.point.service.PointService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;

    @PostMapping("/point/use")
    public ResponseEntity<?> use(@Valid @RequestBody PointUseRequest request) {
        pointService.use(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/point/use/cancel")
    public ResponseEntity<?> cancel(@Valid @RequestBody PointUseCancelRequest request) {
        pointService.cancel(request);
        return ResponseEntity.ok().build();
    }
}
