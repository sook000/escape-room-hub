package cs.escaperoomhub.store.service;

import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;
import cs.escaperoomhub.store.dto.request.TimeslotBookingCancelRequest;
import cs.escaperoomhub.store.dto.response.TimeslotBookingCancelResponse;
import cs.escaperoomhub.store.entity.Timeslot;
import cs.escaperoomhub.store.entity.TimeslotTransactionHistory;
import cs.escaperoomhub.store.entity.TimeslotTransactionHistory.TransactionType;
import cs.escaperoomhub.store.repository.TimeslotRepository;
import cs.escaperoomhub.store.repository.TimeslotTransactionHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeslotBookingCancelTest {

    @Mock TimeslotRepository timeslotRepository;
    @Mock TimeslotTransactionHistoryRepository timeslotTransactionHistoryRepository;
    @Mock RedisLockService redisLockService;

    @InjectMocks TimeslotService timeslotService;

    private static final String REQ_ID = "CANCEL-REQ-123";
    private static final Long USER_ID = 777L;
    private static final Long TIMESLOT_ID = 999L;
    private static final Long PERSON_COUNT = 3L;
    private static final long BOOKING_PRICE = 12_000L;

    private TimeslotTransactionHistory bookingHistory() {
        return new TimeslotTransactionHistory(1L,
                REQ_ID, USER_ID, TIMESLOT_ID, PERSON_COUNT, BOOKING_PRICE, TransactionType.BOOKING
        );
    }

    private TimeslotTransactionHistory cancelHistory(long price) {
        return new TimeslotTransactionHistory(2L,
                REQ_ID, USER_ID, TIMESLOT_ID, PERSON_COUNT, price, TransactionType.CANCEL
        );
    }

    private Timeslot reservedTimeslotEntity() {
        // 이미 예약된 상태(false)
        LocalDateTime now = LocalDateTime.now();
        return new Timeslot(
                TIMESLOT_ID, 1L, 2L,
                now.plusDays(1), now.minusMinutes(1),  1000L,
                false
        );
    }

    private TimeslotBookingCancelRequest req(String requestId) {
        TimeslotBookingCancelRequest req = mock(TimeslotBookingCancelRequest.class);
        when(req.getRequestId()).thenReturn(requestId);
        return req;
    }

    @Nested
    class SuccessCases {

        @Test
        @DisplayName("취소 정상 성공: 예약 이력 있음, 취소 이력 없음 → 취소 저장 & 금액 반환")
        void cancelSuccess() {
            // given
            TimeslotBookingCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(bookingHistory());
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.CANCEL))
                    .thenReturn(null);

            ArgumentCaptor<TimeslotTransactionHistory> cap = ArgumentCaptor.forClass(TimeslotTransactionHistory.class);

            // when
            TimeslotBookingCancelResponse res = timeslotService.cancel(request);

            // then
            assertThat(res.getTotalPrice()).isEqualTo(BOOKING_PRICE);
            verify(timeslotTransactionHistoryRepository).save(cap.capture());

            assertThat(cap.getValue().getTransactionType()).isEqualTo(TransactionType.CANCEL);
            verify(redisLockService).unlock(REQ_ID);
        }

        @Test
        @DisplayName("이미 취소 이력 있음 → 추가 처리 없이 기존 취소 금액 반환")
        void cancelAlreadyCanceled() {
            // given
            TimeslotBookingCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(bookingHistory());

            long canceledPrice = BOOKING_PRICE;
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.CANCEL))
                    .thenReturn(cancelHistory(canceledPrice));

            // when
            TimeslotBookingCancelResponse res = timeslotService.cancel(request);

            // then
            assertThat(res.getTotalPrice()).isEqualTo(canceledPrice);

            verify(timeslotRepository, never()).findById(any());
            verify(timeslotTransactionHistoryRepository, never()).save(any());
            verify(redisLockService).unlock(REQ_ID);
        }
    }

    @Nested
    class FailureCases {

        @Test
        @DisplayName("락 획득 실패 시 예외")
        void lockFailed() {
            // given
            TimeslotBookingCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> timeslotService.cancel(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(redisLockService, never()).unlock(anyString());
            verify(timeslotTransactionHistoryRepository, never()).save(any());
            verify(timeslotRepository, never()).findById(any());
        }

        @Test
        @DisplayName("예약 이력 없음 → notFound 예외")
        void bookingHistoryNotFound() {
            // given
            TimeslotBookingCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(null);

            // when / then
            assertThatThrownBy(() -> timeslotService.cancel(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(timeslotTransactionHistoryRepository, never()).save(any());
            verify(timeslotRepository, never()).findById(any());
            verify(redisLockService).unlock(REQ_ID);
        }

        @Test
        @DisplayName("타임슬롯 없음 → notFound 예외")
        void timeslotNotFound() {
            // given
            TimeslotBookingCancelRequest request = req(REQ_ID);
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(bookingHistory());
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.CANCEL))
                    .thenReturn(null);
            when(timeslotRepository.findById(TIMESLOT_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> timeslotService.cancel(request))
                    .isInstanceOf(ClientErrorException.class);

            verify(timeslotTransactionHistoryRepository, never()).save(any());
            verify(redisLockService).unlock(REQ_ID);
        }
    }
}
