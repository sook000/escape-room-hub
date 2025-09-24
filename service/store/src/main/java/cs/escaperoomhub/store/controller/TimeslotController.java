package cs.escaperoomhub.store.controller;

import cs.escaperoomhub.store.dto.request.TimeslotBookingRequest;
import cs.escaperoomhub.store.dto.response.TimeslotBookingResponse;
import cs.escaperoomhub.store.service.RedisLockService;
import cs.escaperoomhub.store.service.TimeslotService;
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

    @PostMapping("/timeslot/booking")
    public ResponseEntity<TimeslotBookingResponse> booking(
            @Valid @RequestBody TimeslotBookingRequest request) {
        TimeslotBookingResponse response = timeslotService.booking(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
