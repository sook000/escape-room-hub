package cs.escaperoomhub.reservation.service;

import cs.escaperoomhub.common.exceptionstarter.BusinessException;
import cs.escaperoomhub.common.exceptionstarter.CommonErrorCode;
import cs.escaperoomhub.common.exceptionstarter.CommonErrors;
import cs.escaperoomhub.reservation.client.PointApiClient;
import cs.escaperoomhub.reservation.client.TimeslotApiClient;
import cs.escaperoomhub.reservation.client.dto.*;
import cs.escaperoomhub.reservation.dto.request.PlaceReservationRequest;
import cs.escaperoomhub.reservation.entity.Reservation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCoordinator {
    private final ReservationService reservationService;
    private final TimeslotApiClient timeslotApiClient;
    private final PointApiClient pointApiClient;
    private final RedisLockService redisLockService;

    public void placeReservation(PlaceReservationRequest request) {
        String key = request.getReservationId().toString();

        if (!redisLockService.lock(key)) {
            throw CommonErrors.lockAcquisitionFailed(key);
        }

        try {
            reservationService.request(request.getReservationId());
            Reservation reservation = reservationService.getReservation(request.getReservationId());

            try {
                TimeslotBookingApiRequest timeslotBookingApiRequest = new TimeslotBookingApiRequest(
                        request.getReservationId().toString(),
                        reservation.getUserId(),
                        reservation.getTimeslotId(),
                        reservation.getPersonCount()
                );

                TimeslotBookingApiResponse bookingApiResponse = timeslotApiClient.booking(timeslotBookingApiRequest);

                PointUseApiRequest pointUseApiRequest = new PointUseApiRequest(
                        request.getReservationId().toString(),
                        reservation.getUserId(),
                        bookingApiResponse.getTotalPrice()
                );

                pointApiClient.use(pointUseApiRequest);

                reservationService.complete(reservation.getReservationId());

            } catch (Exception e) { // 로직 중 에러 시 보상 트랜잭션
                log.info(e.getMessage());

                TimeslotBookingCancelApiRequest timeslotBookingCancelApiRequest = new TimeslotBookingCancelApiRequest(request.getReservationId().toString());

                TimeslotBookingCancelApiResponse bookingCancelApiResponse = timeslotApiClient.cancel(timeslotBookingCancelApiRequest);

                if (bookingCancelApiResponse.getTotalPrice() > 0) {
                    PointUseCancelApiRequest pointUseCancelApiRequest = new PointUseCancelApiRequest(request.getReservationId().toString());

                    pointApiClient.cancel(pointUseCancelApiRequest);
                }

                reservationService.fail(request.getReservationId());


            }

        } finally {
            redisLockService.unlock(key);
        }
    }
}
