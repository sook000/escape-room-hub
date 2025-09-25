package cs.escaperoomhub.reservation.controller;

import cs.escaperoomhub.reservation.dto.request.CreateReservationRequest;
import cs.escaperoomhub.reservation.dto.request.PlaceReservationRequest;
import cs.escaperoomhub.reservation.dto.response.CreateReservationResponse;
import cs.escaperoomhub.reservation.service.ReservationCoordinator;
import cs.escaperoomhub.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationCoordinator reservationCoordinator;

    @PostMapping("/reservation")
    public ResponseEntity<?> createReservation(@Valid @RequestBody CreateReservationRequest request) {
        CreateReservationResponse reservation = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservation);
    }

    @PostMapping("/reservation/place")
    public ResponseEntity<?> placeReservation(@Valid @RequestBody PlaceReservationRequest request) {
        reservationCoordinator.placeReservation(request);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
