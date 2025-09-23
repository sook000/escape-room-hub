package cs.escaperoomhub.monolithic.reservation.service;

import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.monolithic.point.service.PointService;
import cs.escaperoomhub.monolithic.reservation.entity.Reservation;
import cs.escaperoomhub.monolithic.reservation.repository.ReservationRepository;
import cs.escaperoomhub.monolithic.reservation.dto.PlaceReservationRequest;
import cs.escaperoomhub.monolithic.store.service.TimeslotService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ReservationService {
    private final Snowflake snowflake = new Snowflake();
    private final ReservationRepository reservationRepository;
    private final PointService pointService;
    private final TimeslotService timeslotService;

    @Transactional
    public void placeReservation(PlaceReservationRequest request) {
        Reservation reservation = reservationRepository.save(new Reservation(snowflake.nextId(),
                request.getUserId(), request.getTimeslotId(), request.getPersonCount()));

        Long price = timeslotService.reserve(reservation.getTimeslotId(), reservation.getPersonCount());

        pointService.use(reservation.getUserId(), price);
    }

}
