package cs.escaperoomhub.store.service;

import cs.escaperoomhub.common.exceptionstarter.CommonErrors;
import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.store.dto.request.TimeslotBookingRequest;
import cs.escaperoomhub.store.dto.response.TimeslotBookingResponse;
import cs.escaperoomhub.store.entity.Timeslot;
import cs.escaperoomhub.store.entity.TimeslotTransactionHistory;
import cs.escaperoomhub.store.repository.TimeslotTransactionHistoryRepository;
import cs.escaperoomhub.store.repository.TimeslotRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class TimeslotService {
    private final Snowflake snowflake = new Snowflake();
    private TimeslotRepository timeslotRepository;
    private TimeslotTransactionHistoryRepository timeslotTransactionHistoryRepository;
    private final RedisLockService redisLockService;

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
                log.info("이미 구매한 이력이 있습니다");
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
}
