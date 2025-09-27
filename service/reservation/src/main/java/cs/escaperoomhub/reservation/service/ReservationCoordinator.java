package cs.escaperoomhub.reservation.service;

import cs.escaperoomhub.common.event.EventType;
import cs.escaperoomhub.common.exceptionstarter.BusinessException;
import cs.escaperoomhub.common.exceptionstarter.CommonErrorCode;
import cs.escaperoomhub.common.exceptionstarter.CommonErrors;
import cs.escaperoomhub.common.outboxmessagerelay.OutboxEventPublisher;
import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.reservation.client.PointApiClient;
import cs.escaperoomhub.reservation.client.TimeslotApiClient;
import cs.escaperoomhub.reservation.client.dto.*;
import cs.escaperoomhub.reservation.dto.request.PlaceReservationRequest;
import cs.escaperoomhub.reservation.entity.CompensationRegistry;
import cs.escaperoomhub.reservation.entity.Reservation;
import cs.escaperoomhub.reservation.repository.CompensationRegistryRepository;
import cs.escaperoomhub.common.event.payload.CompensationRequiredEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReservationCoordinator {
    private final Snowflake snowflake = new Snowflake();
    private final ReservationService reservationService;
    private final CompensationRegistryRepository compensationRegistryRepository;
    private final TimeslotApiClient timeslotApiClient;
    private final PointApiClient pointApiClient;
    private final RedisLockService redisLockService;
    private final OutboxEventPublisher outboxEventPublisher;
    private final CompensationEventProducer compensationEventProducer;

    @Transactional
    public void placeReservation(PlaceReservationRequest request) {
        String key = request.getReservationId().toString();

        if (!redisLockService.lock(key)) {
            throw CommonErrors.lockAcquisitionFailed(key);
        }

        try {
            reservationService.request(request.getReservationId());
            Reservation reservation = reservationService.getReservation(request.getReservationId());

            boolean booked = false;
            boolean pointsUsed = false;

            try {
                TimeslotBookingApiRequest timeslotBookingApiRequest = new TimeslotBookingApiRequest(
                        request.getReservationId().toString(),
                        reservation.getUserId(),
                        reservation.getTimeslotId(),
                        reservation.getPersonCount()
                );

                TimeslotBookingApiResponse bookingApiResponse = timeslotApiClient.booking(timeslotBookingApiRequest);
                booked = true;

                PointUseApiRequest pointUseApiRequest = new PointUseApiRequest(
                        request.getReservationId().toString(),
                        reservation.getUserId(),
                        bookingApiResponse.getTotalPrice()
                );

                pointApiClient.use(pointUseApiRequest);
                pointsUsed = true;

                reservationService.complete(reservation.getReservationId());

            } catch (BusinessException e) { // 로직 중 에러 시 보상 트랜잭션
                System.out.println("publish!!!");
                compensationEventProducer.publishCompensationRequired(
                        request.getReservationId(), e.getMessage()
                );

                throw e;
            } catch(Exception e) {
                log.warn("예약 중 예상하지 못한 에러, reservationId={}", request.getReservationId(), e);

                compensationEventProducer.publishCompensationRequired(
                        request.getReservationId(), e.getMessage()
                );

                throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR,"보상 트랜잭션 완료 - 예약 실패", e);
            }

        } finally {
            redisLockService.unlock(key);
        }
    }

}
