package cs.escaperoomhub.monolithic.reservation.controller;

import cs.escaperoomhub.monolithic.reservation.dto.request.CreateReservationRequest;
import cs.escaperoomhub.monolithic.reservation.dto.response.CreateReservationResponse;
import cs.escaperoomhub.monolithic.reservation.service.ReservationService;
import cs.escaperoomhub.monolithic.reservation.dto.request.PlaceReservationRequest;
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

    @PostMapping("/reservation")
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        CreateReservationResponse reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @PostMapping("/reservation/place")
    public ResponseEntity<?> reservationOrder(@Valid @RequestBody PlaceReservationRequest request) throws InterruptedException {
        reservationService.placeReservation(request);
        return ResponseEntity.ok().build();
    }
}
