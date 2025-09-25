package cs.escaperoomhub.point.service;

import cs.escaperoomhub.common.exceptionstarter.CommonErrors;
import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.point.dto.request.PointUseCancelRequest;
import cs.escaperoomhub.point.dto.request.PointUseRequest;
import cs.escaperoomhub.point.entity.Point;
import cs.escaperoomhub.point.entity.PointTransactionHistory;
import cs.escaperoomhub.point.repository.PointRepository;
import cs.escaperoomhub.point.repository.PointTransactionHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
public class PointService {
    private final Snowflake snowflake = new Snowflake();
    private final PointRepository pointRepository;
    private final PointTransactionHistoryRepository pointTransactionHistoryRepository;
    private final RedisLockService redisLockService;

    @Transactional
    public void use(PointUseRequest request) {
        String key = request.getRequestId().toString();

        if (!redisLockService.lock(key)) {
            throw CommonErrors.lockAcquisitionFailed(key);
        }

        try {
            PointTransactionHistory useHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(),
                    PointTransactionHistory.TransactionType.USE
            );

            if (useHistory != null) {
                log.info("이미 포인트 사용 이력이 존재합니다");
                return;
            }

            Point point = pointRepository.findByUserId(request.getUserId())
                    .orElseThrow(() -> CommonErrors.notFound("포인트가 존재하지 않습니다."));

            point.use(request.getAmount());
            pointTransactionHistoryRepository.save(
                    new PointTransactionHistory(snowflake.nextId(), request.getRequestId(),
                            point.getPointId(), request.getAmount(),
                            PointTransactionHistory.TransactionType.USE
                    )
            );

            throw CommonErrors.notFound("Point 서비스에서 use 처리 중 예시 예외 notFound 발생1");

        } finally {
            redisLockService.unlock(key);
        }
    }

    @Transactional
    public void cancel(PointUseCancelRequest request) {
        String key = request.getRequestId().toString();

        if (!redisLockService.lock(key)) {
            throw CommonErrors.lockAcquisitionFailed(key);
        }

        try {
            PointTransactionHistory useHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(),
                    PointTransactionHistory.TransactionType.USE
            );

            if (useHistory == null) {
//                throw CommonErrors.notFound("포인트 사용 내역이 존재하지 않습니다.");
                log.info("포인트 사용 내역이 존재하지 않습니다");
                return;
            }

            PointTransactionHistory cancelHistory = pointTransactionHistoryRepository.findByRequestIdAndTransactionType(
                    request.getRequestId(),
                    PointTransactionHistory.TransactionType.CANCEL
            );

            if (cancelHistory != null) {
                log.info("이미 취소된 요청입니다");
                return;
            }

            Point point = pointRepository.findById(useHistory.getPointId())
                    .orElseThrow(() -> CommonErrors.notFound("포인트가 존재하지 않습니다."));

            point.cancel(useHistory.getAmount());
            pointTransactionHistoryRepository.save(
                    new PointTransactionHistory(snowflake.nextId(), request.getRequestId(),
                            point.getPointId(), useHistory.getAmount(),
                            PointTransactionHistory.TransactionType.CANCEL)
            );
        } finally {
            redisLockService.unlock(key);
        }
    }
}
