package cs.escaperoomhub.point.service;

import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;
import cs.escaperoomhub.point.dto.request.PointUseCancelRequest;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointCancelServiceTest {

    @Mock PointRepository pointRepository;
    @Mock PointTransactionHistoryRepository pointTransactionHistoryRepository;
    @Mock RedisLockService redisLockService;

    @InjectMocks PointService pointService;

    private static final String REQ_ID = "CANCEL-REQ-123";
    private static final Long POINT_ID = 111L;
    private static final long USE_AMOUNT = 10_000L;

    private PointUseCancelRequest req(String requestId) {
        PointUseCancelRequest r = mock(PointUseCancelRequest.class);
        when(r.getRequestId()).thenReturn(requestId);
        return r;
    }

    private PointTransactionHistory useHistory() {
        return new PointTransactionHistory(
                1L, REQ_ID, POINT_ID, USE_AMOUNT, TransactionType.USE
        );
    }

    private PointTransactionHistory cancelHistory() {
        return new PointTransactionHistory(
                2L, REQ_ID, POINT_ID, USE_AMOUNT, TransactionType.CANCEL
        );
    }

    @Nested
    class SuccessCases {

        @Test
        @DisplayName("정상 취소: USE 이력 있음 & CANCEL 이력 없음 → point.cancel 호출 & CANCEL 이력 저장")
        void cancelSuccess() {
            // given
            PointUseCancelRequest request = mock(PointUseCancelRequest.class);
            when(request.getRequestId()).thenReturn(REQ_ID);

            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(new PointTransactionHistory(
                            1L, REQ_ID, POINT_ID, USE_AMOUNT, TransactionType.USE   // USE_AMOUNT = 10_000
                    ));
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.CANCEL))
                    .thenReturn(null);

            Point realPoint = new Point(POINT_ID, 777L, 90_000L);
            when(pointRepository.findById(POINT_ID)).thenReturn(Optional.of(realPoint));

            ArgumentCaptor<PointTransactionHistory> cap = ArgumentCaptor.forClass(PointTransactionHistory.class);

            // when
            pointService.cancel(request);

            // then
            assertThat(realPoint.getAmount()).isEqualTo(100_000L);

            verify(pointTransactionHistoryRepository).save(cap.capture());
            assertThat(cap.getValue().getTransactionType()).isEqualTo(TransactionType.CANCEL);
            verify(redisLockService).unlock(REQ_ID);
        }

        @Test
        @DisplayName("이미 CANCEL 이력 존재 → 추가 처리 없이 return (idempotent)")
        void alreadyCanceled() {
            // given
            PointUseCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(useHistory());
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.CANCEL))
                    .thenReturn(cancelHistory());

            // when
            pointService.cancel(request);

            // then
            verify(pointRepository, never()).findById(anyLong());
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
            PointUseCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> pointService.cancel(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(redisLockService, never()).unlock(anyString());
            verify(pointTransactionHistoryRepository, never()).save(any());
            verify(pointRepository, never()).findById(anyLong());
        }

        @Test
        @DisplayName("USE 이력 없음 → notFound 예외 & unlock 호출")
        void useHistoryNotFound() {
            // given
            PointUseCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(null);

            // when / then
            assertThatThrownBy(() -> pointService.cancel(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(pointTransactionHistoryRepository, never()).save(any());
            verify(pointRepository, never()).findById(anyLong());
            verify(redisLockService).unlock(REQ_ID);
        }

        @Test
        @DisplayName("Point 엔티티 없음 → notFound 예외 & unlock 호출")
        void pointNotFound() {
            // given
            PointUseCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.USE))
                    .thenReturn(useHistory());
            when(pointTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.CANCEL))
                    .thenReturn(null);
            when(pointRepository.findById(POINT_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> pointService.cancel(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(pointTransactionHistoryRepository, never()).save(any());
            verify(redisLockService).unlock(REQ_ID);
        }
    }
}
