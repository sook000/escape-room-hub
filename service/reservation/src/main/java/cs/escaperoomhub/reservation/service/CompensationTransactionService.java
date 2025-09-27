package cs.escaperoomhub.reservation.service;

import cs.escaperoomhub.common.exceptionstarter.BusinessException;
import cs.escaperoomhub.common.exceptionstarter.CommonErrorCode;
import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.reservation.client.PointApiClient;
import cs.escaperoomhub.reservation.client.TimeslotApiClient;
import cs.escaperoomhub.reservation.client.dto.PointUseCancelApiRequest;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingCancelApiRequest;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingCancelApiResponse;
import cs.escaperoomhub.reservation.entity.CompensationRegistry;
import cs.escaperoomhub.reservation.repository.CompensationRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompensationTransactionService {
    private final Snowflake snowflake = new Snowflake();
    private final ReservationService reservationService;
    private final CompensationRegistryRepository compensationRegistryRepository;
    private final TimeslotApiClient timeslotApiClient;
    private final PointApiClient pointApiClient;
    private final RedisLockService redisLockService;

    public void executeCompensation(Long reservationId, String reason) {
        String key = "compensation::" + reservationId.toString();

        if (!redisLockService.lock(key)) {
            log.info("보상 트랜잭션 락 획득 실패: reservationId={}", reservationId);
            return;
        }

        try {
            CompensationRegistry registry = createOrGetRegistry(reservationId);
            if (registry == null) {
                return;
            }

            boolean compensationSuccess = performExternalCompensation(reservationId, registry);

            updateFinalStatus(reservationId, registry, compensationSuccess, reason);

        } finally {
            redisLockService.unlock(key);
        }
    }

    @Transactional
    private CompensationRegistry createOrGetRegistry(Long reservationId) {
        try {
            CompensationRegistry registry = new CompensationRegistry(snowflake.nextId(), reservationId);
            return compensationRegistryRepository.save(registry);

        } catch (DataIntegrityViolationException e) { // reservationId는 유니크 키
            CompensationRegistry existing = compensationRegistryRepository.findByReservationId(reservationId)
                    .orElse(null);

            if (existing != null && existing.getStatus() == CompensationRegistry.CompensationRegistryStatus.COMPLETE) {
                log.info("이미 보상 처리 완료된 예약: reservationId={}", reservationId);
                return null;
            }

            return existing;
        }
    }

    private boolean performExternalCompensation(Long reservationId, CompensationRegistry registry) {
        try {
            TimeslotBookingCancelApiRequest timeslotCancelRequest =
                    new TimeslotBookingCancelApiRequest(reservationId.toString());
            TimeslotBookingCancelApiResponse cancelResponse = timeslotApiClient.cancel(timeslotCancelRequest);

            if (cancelResponse.getTotalPrice() > 0) {
                PointUseCancelApiRequest pointCancelRequest =
                        new PointUseCancelApiRequest(reservationId.toString());
                pointApiClient.cancel(pointCancelRequest);
            }

            return true;

        } catch (Exception e) {
            log.error("외부 시스템 보상 처리 실패: reservationId={}", reservationId, e);
            updateRegistryToFailed(registry, e.getMessage());
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void updateRegistryToFailed(CompensationRegistry registry, String errorMessage) {
        try {
            registry.fail(errorMessage);
            compensationRegistryRepository.save(registry);
        } catch (Exception e) {
            log.error("보상 레지스트리 실패 상태 업데이트 실패: registryId={}", registry.getId(), e);
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR, "Compensation Registry update failed", e);
        }
    }

    @Transactional
    private void updateFinalStatus(Long reservationId, CompensationRegistry registry,
                                   boolean compensationSuccess, String reason) {
        if (!compensationSuccess) {
            log.error("보상 트랜잭션 실패로 완료: reservationId={}, reason={}", reservationId, reason);
            return;
        }

        try {
            reservationService.fail(reservationId);

            registry.complete();
            compensationRegistryRepository.save(registry);

            log.info("보상 트랜잭션 완료: reservationId={}, reason={}", reservationId, reason);
        } catch (Exception e) {
            log.error("최종 상태 업데이트 실패: reservationId={}", reservationId, e);
            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR,"Final status update failed", e);
        }
    }
}
