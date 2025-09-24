package cs.escaperoomhub.point.service;

import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;
import cs.escaperoomhub.point.dto.request.PointUseRequest;
import cs.escaperoomhub.point.entity.Point;
import cs.escaperoomhub.point.entity.PointTransactionHistory;
import cs.escaperoomhub.point.entity.PointTransactionHistory.TransactionType;
import cs.escaperoomhub.point.repository.PointRepository;
import cs.escaperoomhub.point.repository.PointTransactionHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PointUseServiceTest {

    @Mock PointRepository pointRepository;
    @Mock PointTransactionHistoryRepository pointTransactionHistoryRepository;
    @Mock RedisLockService redisLockService;

    @InjectMocks PointService pointService;

    private static final String REQ_ID = "USE-REQ-123";
    private static final Long USER_ID = 777L;
    private static final Long POINT_ID = 111L;
    private static final long USE_AMOUNT = 10_000L;

    private PointUseRequest req(String requestId, Long userId, long amount) {
        PointUseRequest r = mock(PointUseRequest.class);
        when(r.getRequestId()).thenReturn(requestId);
        when(r.getUserId()).thenReturn(userId);
        when(r.getAmount()).thenReturn(amount);
        return r;
    }

    @Nested
    class SuccessCases {

        @Test
        @DisplayName("정상 포인트 사용: 중복 이력 없음 → 잔액 차감 & USE 이력 저장")
        void useSuccessBalanceDecreasedAndSaved() {
            // given
            PointUseRequest request = req(REQ_ID, USER_ID, USE_AMOUNT);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(null);

            Point point = new Point(POINT_ID, USER_ID, 100_000L);
            when(pointRepository.findByUserId(USER_ID)).thenReturn(Optional.of(point));

            ArgumentCaptor<PointTransactionHistory> cap = ArgumentCaptor.forClass(PointTransactionHistory.class);

            // when
            pointService.use(request);

            // then
            assertThat(point.getAmount()).isEqualTo(90_000L);

            verify(pointTransactionHistoryRepository).save(cap.capture());
            assertThat(cap.getValue().getTransactionType()).isEqualTo(TransactionType.USE);
        }

        @Test
        @DisplayName("이미 사용 이력 존재 → 아무 처리 없이 return")
        void alreadyUsed() {
            // given
            PointUseRequest request = req(REQ_ID, USER_ID, USE_AMOUNT);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(new PointTransactionHistory(1L, REQ_ID, POINT_ID, USE_AMOUNT, TransactionType.USE));

            // when
            pointService.use(request);

            // then
            verify(pointRepository, never()).findByUserId(anyLong());
            verify(pointTransactionHistoryRepository, never()).save(any());
            verify(redisLockService).unlock(REQ_ID);
        }
    }

    @Nested
    class FailureCases {

        @Test
        @DisplayName("락 획득 실패 → 예외 & unlock 호출 안 함")
        void lockFailed() {
            // given
            PointUseRequest request = req(REQ_ID, USER_ID, USE_AMOUNT);
            when(redisLockService.lock(REQ_ID)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> pointService.use(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(redisLockService, never()).unlock(anyString());
            verify(pointRepository, never()).findByUserId(anyLong());
            verify(pointTransactionHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("포인트 없음 → notFound 예외 & 이력 저장 안 함 & unlock 호출")
        void pointNotFound() {
            // given
            PointUseRequest request = req(REQ_ID, USER_ID, USE_AMOUNT);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(null);
            when(pointRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> pointService.use(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(pointTransactionHistoryRepository, never()).save(any());
            verify(redisLockService).unlock(REQ_ID);
        }

        @Test
        @DisplayName("잔액 부족 → 예외 & 이력 저장 안 함 & unlock 호출")
        void insufficientBalance() {
            // given
            PointUseRequest request = req(REQ_ID, USER_ID, USE_AMOUNT); // 10,000 사용
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(null);

            // 잔액 5,000으로 부족하게 세팅
            Point point = new Point(POINT_ID, USER_ID, 5_000L);
            when(pointRepository.findByUserId(USER_ID)).thenReturn(Optional.of(point));

            // when / then
            assertThatThrownBy(() -> pointService.use(request))
                    .isInstanceOf(ClientErrorException.class);

            // 상태/저장 검증
            assertThat(point.getAmount()).isEqualTo(5_000L); // 차감되지 않음
            verify(pointTransactionHistoryRepository, never()).save(any());
            verify(redisLockService).unlock(REQ_ID);
        }
    }
}
