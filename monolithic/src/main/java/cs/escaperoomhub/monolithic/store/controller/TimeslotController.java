package cs.escaperoomhub.monolithic.store.controller;

import cs.escaperoomhub.monolithic.reservation.dto.request.CreateReservationRequest;
import cs.escaperoomhub.monolithic.reservation.dto.response.CreateReservationResponse;
import cs.escaperoomhub.monolithic.store.service.TimeslotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimeslotController {
    private final TimeslotService timeslotService;

    // 동시성 테스트를 위한 컨트롤러
    @PostMapping("/timeslot/test")
    public void changeTimeslotStatus() {
        timeslotService.reserve(1L, 1L);
    }

}
