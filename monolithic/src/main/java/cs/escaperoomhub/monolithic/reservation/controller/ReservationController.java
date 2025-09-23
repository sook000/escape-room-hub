package cs.escaperoomhub.monolithic.reservation.controller;

import cs.escaperoomhub.monolithic.reservation.service.ReservationService;
import cs.escaperoomhub.monolithic.reservation.dto.PlaceReservationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping("/reservation/place")
    public ResponseEntity<?> reservationOrder(@Valid @RequestBody PlaceReservationRequest request) {
        reservationService.placeReservation(request);
        return ResponseEntity.ok().build();
    }
}
