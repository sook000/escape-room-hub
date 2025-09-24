package cs.escaperoomhub.monolithic.reservation.service;

import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.monolithic.exception.CommonErrors;
import cs.escaperoomhub.monolithic.point.service.PointService;
import cs.escaperoomhub.monolithic.reservation.dto.request.CreateReservationRequest;
import cs.escaperoomhub.monolithic.reservation.dto.response.CreateReservationResponse;
import cs.escaperoomhub.monolithic.reservation.entity.Reservation;
import cs.escaperoomhub.monolithic.reservation.repository.ReservationRepository;
import cs.escaperoomhub.monolithic.reservation.dto.request.PlaceReservationRequest;
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
    private final RedisLockService redisLockService;

    @Transactional
    public CreateReservationResponse createReservation(CreateReservationRequest request) {
        Reservation reservation = reservationRepository.save(new Reservation(snowflake.nextId(),
                request.getUserId(), request.getTimeslotId(), request.getPersonCount()));
        return new CreateReservationResponse(reservation.getReservationId());
    }

    @Transactional
    public void placeReservation(PlaceReservationRequest request) {
        String key = request.getReservationId().toString();
        if (!redisLockService.lock(key)) {
            throw CommonErrors.lockAcquisitionFailed(key);
        }
//        Thread.sleep(100000);

        try {
            Reservation reservation = reservationRepository.findById(request.getReservationId())
                    .orElseThrow(() -> CommonErrors.notFound("예약이 존재하지 않습니다."));

            if (reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
                return;
            }

            Long price = timeslotService.reserve(reservation.getTimeslotId(), reservation.getPersonCount());

            pointService.use(reservation.getUserId(), price);

            reservation.complete();
            reservationRepository.save(reservation);
        } finally {
            redisLockService.unlock(key);
        }
    }

}
