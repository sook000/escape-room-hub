package cs.escaperoomhub.reservation.service;

import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.reservation.dto.request.CreateReservationRequest;
import cs.escaperoomhub.reservation.dto.response.CreateReservationResponse;
import cs.escaperoomhub.reservation.entity.Reservation;
import cs.escaperoomhub.reservation.repository.ReservationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class ReservationService {
    private final Snowflake snowflake = new Snowflake();
    private final ReservationRepository reservationRepository;

    @Transactional
    public CreateReservationResponse createReservation(CreateReservationRequest request) {
        Reservation reservation = reservationRepository.save(new Reservation(snowflake.nextId(),
                request.getUserId(), request.getTimeslotId(), request.getPersonCount()));

        return new CreateReservationResponse(reservation.getReservationId());
    }
}
