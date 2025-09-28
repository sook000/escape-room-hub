package cs.escaperoomhub.store.service;

import cs.escaperoomhub.common.exceptionstarter.CommonErrors;
import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.store.dto.request.TimeslotBookingCancelRequest;
import cs.escaperoomhub.store.dto.request.TimeslotBookingRequest;
import cs.escaperoomhub.store.dto.response.TimeslotBookingCancelResponse;
import cs.escaperoomhub.store.dto.response.TimeslotBookingResponse;
import cs.escaperoomhub.store.entity.Timeslot;
import cs.escaperoomhub.store.entity.TimeslotTransactionHistory;
import cs.escaperoomhub.store.repository.TimeslotTransactionHistoryRepository;
import cs.escaperoomhub.store.repository.TimeslotRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;

@Service
@Slf4j
@AllArgsConstructor
public class TimeslotService {
    private final Snowflake snowflake = new Snowflake();
    private final TimeslotRepository timeslotRepository;
    private final TimeslotTransactionHistoryRepository timeslotTransactionHistoryRepository;
    private final RedisLockService redisLockService;
    private final RedisAvailabilityService redisAvailabilityService;

    private static final Duration AVAIL_TTL = Duration.ofMinutes(3);

    @Transactional
    public TimeslotBookingResponse booking2(TimeslotBookingRequest request) {
        String lockKey = request.getRequestId().toString();
        Long timeslotId = request.getTimeslotId();

        if (!redisLockService.lock(lockKey)) {
            throw CommonErrors.lockAcquisitionFailed(lockKey);
        }

        try {
            TimeslotTransactionHistory existing = timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(), TimeslotTransactionHistory.TransactionType.BOOKING);

            if (existing != null) {
                log.info("이미 타임슬롯을 booking한 이력이 있습니다");
                return new TimeslotBookingResponse(existing.getPrice());
            }

            Boolean cached = redisAvailabilityService.isAvailable(timeslotId);
            if (Boolean.FALSE.equals(cached)) {
                throw CommonErrors.notFound("이미 예약된 타임슬롯입니다.");
            }

            Timeslot timeslot = timeslotRepository.findById(timeslotId)
                    .orElseThrow(() -> CommonErrors.notFound("타임슬롯이 존재하지 않습니다."));
            timeslot.checkOpenTime();

            int affected = timeslotRepository.reserveIfAvailable(timeslotId);
            if (affected == 0) {
                redisAvailabilityService.setAvailable(timeslotId, false, AVAIL_TTL);
                throw CommonErrors.notFound("이미 예약된 타임슬롯입니다.");
            }

            cacheAfterCommit(timeslotId, false, AVAIL_TTL);

            Long price = timeslot.calculatePrice(request.getPersonCount());
            timeslotTransactionHistoryRepository.save(new TimeslotTransactionHistory(
                    snowflake.nextId(), request.getRequestId(), request.getUserId(),
                    timeslotId, request.getPersonCount(), price, TimeslotTransactionHistory.TransactionType.BOOKING
            ));

            return new TimeslotBookingResponse(price);
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    // 커밋 이후에만 캐시 갱신/삭제
    @Transactional
    public TimeslotBookingCancelResponse cancel2(TimeslotBookingCancelRequest request) {
        String lockKey = request.getRequestId().toString();
        Long timeslotId = null;

        if (!redisLockService.lock(lockKey)) {
            throw CommonErrors.lockAcquisitionFailed(lockKey);
        }

        try {
            TimeslotTransactionHistory bookingHistory = timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(), TimeslotTransactionHistory.TransactionType.BOOKING);

            if (bookingHistory == null) {
                log.info("타임슬롯 booking 내역이 존재하지 않습니다");
                return new TimeslotBookingCancelResponse(0L);
            }

            TimeslotTransactionHistory cancelHistory = timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(), TimeslotTransactionHistory.TransactionType.CANCEL);

            if (cancelHistory != null) {
                log.info("이미 booking 취소된 타임슬롯 내역입니다.");
                return new TimeslotBookingCancelResponse(cancelHistory.getPrice());
            }

            timeslotId = bookingHistory.getTimeslotId();
            Timeslot timeslot = timeslotRepository.findById(timeslotId)
                    .orElseThrow(() -> CommonErrors.notFound("타임슬롯이 존재하지 않습니다."));
            int affected = timeslotRepository.cancelIfReserved(timeslotId);

            if (affected == 1) {
                cacheAfterCommit(timeslotId, true, AVAIL_TTL);
            } else {
                cacheAfterCommit(timeslotId, null, AVAIL_TTL); //캐시 삭제
            }

            timeslotTransactionHistoryRepository.save(new TimeslotTransactionHistory(
                    snowflake.nextId(), request.getRequestId(), bookingHistory.getUserId(),
                    bookingHistory.getTimeslotId(), bookingHistory.getPersonCount(),
                    bookingHistory.getPrice(), TimeslotTransactionHistory.TransactionType.CANCEL
            ));

            return new TimeslotBookingCancelResponse(bookingHistory.getPrice());
        } finally {
            redisLockService.unlock(lockKey);
        }
    }

    private void cacheAfterCommit(Long timeslotId, Boolean available, Duration ttl) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    if (available == null) {
                        redisAvailabilityService.del(timeslotId);
                    } else {
                        redisAvailabilityService.setAvailable(timeslotId, available, ttl);
                    }
                } catch (Exception e) {
                    log.warn("afterCommit 캐시 반영 실패: timeslotId={}", timeslotId, e);
                }
            }

            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    try {
                        redisAvailabilityService.del(timeslotId);
                    } catch (Exception e) {
                        log.warn("롤백 시 캐시 삭제 실패: timeslotId={}", timeslotId, e);
                    }
                }
            }
        });
    }

    public boolean isTimeslotAvailable(Long timeslotId) {
        Boolean cached = redisAvailabilityService.isAvailable(timeslotId);
        if (cached != null) {
            return cached;
        }

        return timeslotRepository.findById(timeslotId)
                .map(timeslot -> {
                    boolean available = timeslot.isAvailable();
                    redisAvailabilityService.setAvailable(timeslotId, available, Duration.ofMinutes(3));
                    return available;
                })
                .orElse(false);
    }

    @Transactional
    public TimeslotBookingResponse booking(TimeslotBookingRequest request) {
        String key = request.getRequestId().toString();

        if (!redisLockService.lock(key)) {
            throw CommonErrors.lockAcquisitionFailed(key);
        }

        try {
            TimeslotTransactionHistory history = timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(),
                    TimeslotTransactionHistory.TransactionType.BOOKING
            );

            if (history != null) {
                log.info("이미 타임슬롯을 booking한 이력이 있습니다");
                return new TimeslotBookingResponse(history.getPrice());
            }

            Timeslot timeslot = timeslotRepository.findById(request.getTimeslotId())
                    .orElseThrow(() -> CommonErrors.notFound("타임슬롯이 존재하지 않습니다."));

            timeslot.booking();
            Long totalPrice = timeslot.calculatePrice(request.getPersonCount());

            timeslotTransactionHistoryRepository.save(new TimeslotTransactionHistory(
                    snowflake.nextId(), request.getRequestId(), request.getUserId(), request.getTimeslotId(),
                    request.getPersonCount(), totalPrice, TimeslotTransactionHistory.TransactionType.BOOKING)
            );

            return new TimeslotBookingResponse(totalPrice);
        } finally {
            redisLockService.unlock(key);
        }
    }

    @Transactional
    public TimeslotBookingCancelResponse cancel(TimeslotBookingCancelRequest request) {
        String key = request.getRequestId().toString();

        if (!redisLockService.lock(key)) {
            throw CommonErrors.lockAcquisitionFailed(key);
        }

        try {
            TimeslotTransactionHistory bookingHistory = timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(),
                    TimeslotTransactionHistory.TransactionType.BOOKING
            );

            if (bookingHistory == null) {
//                throw CommonErrors.notFound("타임슬롯 booking 내역이 존재하지 않습니다.");
                log.info("타임슬롯 booking 내역이 존재하지 않습니다");
                return new TimeslotBookingCancelResponse(0L);
            }

            TimeslotTransactionHistory cancelHistory = timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(),
                    TimeslotTransactionHistory.TransactionType.CANCEL
            );

            if (cancelHistory != null) {
                log.info("이미 booking 취소된 타임슬롯 내역입니다.");
                return new TimeslotBookingCancelResponse(cancelHistory.getPrice());
            }

            Timeslot timeslot = timeslotRepository.findById(bookingHistory.getTimeslotId())
                    .orElseThrow(() -> CommonErrors.notFound("타임슬롯이 존재하지 않습니다."));

            timeslot.cancel();
            timeslotTransactionHistoryRepository.save(
                    new TimeslotTransactionHistory(
                            snowflake.nextId(), request.getRequestId(), bookingHistory.getUserId(),
                            bookingHistory.getTimeslotId(), bookingHistory.getPersonCount(), bookingHistory.getPrice(),
                            TimeslotTransactionHistory.TransactionType.CANCEL
                    )
            );

            // 보상 트랜잭션 중 에러 발생
//            if (true) {
//                throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR,"timeslot cancel 중 예외 발생");
//            }

            return new TimeslotBookingCancelResponse(bookingHistory.getPrice());
        } finally {
            redisLockService.unlock(key);
        }
    }
}
