package cs.escaperoomhub.store.service;

import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;
import cs.escaperoomhub.common.exceptionstarter.CommonErrors;
import cs.escaperoomhub.store.dto.request.TimeslotBookingRequest;
import cs.escaperoomhub.store.dto.response.TimeslotBookingResponse;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TimeslotServiceTest {

    @Mock TimeslotRepository timeslotRepository;
    @Mock TimeslotTransactionHistoryRepository timeslotTransactionHistoryRepository;
    @Mock RedisLockService redisLockService;

    @InjectMocks TimeslotService timeslotService;

    private static final String REQ_ID = "123";
    private static final Long USER_ID = 777L;
    private static final Long TIMESLOT_ID = 999L;
    private static final Long PERSON_COUNT = 3L;

    private TimeslotBookingRequest mockRequest() {
        TimeslotBookingRequest req = mock(TimeslotBookingRequest.class);
        when(req.getRequestId()).thenReturn(REQ_ID);
        when(req.getUserId()).thenReturn(USER_ID);
        when(req.getTimeslotId()).thenReturn(TIMESLOT_ID);
        when(req.getPersonCount()).thenReturn(PERSON_COUNT);
        return req;
    }

    private Timeslot buildTimeslotAvailable(long pricePerPerson) {
        LocalDateTime now = LocalDateTime.now();
        return new Timeslot(
                TIMESLOT_ID,1L, 2L,
                now.plusDays(1),
                now.minusMinutes(1),
                pricePerPerson,
                true
        );
    }

    @Nested
    class SuccessCases {

        @Test
        @DisplayName("예약 정상 성공")
        void bookingSuccess() {
            // given
            TimeslotBookingRequest req = mockRequest();
            when(redisLockService.lock(REQ_ID.toString())).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(null);

            long pricePerPerson = 1_000L;
            Timeslot timeslot = buildTimeslotAvailable(pricePerPerson);
            when(timeslotRepository.findById(TIMESLOT_ID)).thenReturn(Optional.of(timeslot));

            // when
            TimeslotBookingResponse res = timeslotService.booking(req);

            // then
            assertThat(res).isNotNull();
            assertThat(res.getTotalPrice()).isEqualTo(pricePerPerson * PERSON_COUNT);
            verify(redisLockService).unlock(REQ_ID);
        }

        @Test
        @DisplayName("중복 이미 구매이력 있으면 추가 처리 없이 반환")
        void bookingAlreadyBookedReturnResponse() {
            // given
            TimeslotBookingRequest req = mockRequest();
            when(redisLockService.lock(REQ_ID)).thenReturn(true);

            long previousPrice = 5_000L;
            TimeslotTransactionHistory existing = new TimeslotTransactionHistory(
                     1L, REQ_ID, USER_ID, TIMESLOT_ID, PERSON_COUNT, previousPrice, TransactionType.BOOKING
            );
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(existing);

            // when
            TimeslotBookingResponse res = timeslotService.booking(req);

            // then
            assertThat(res.getTotalPrice()).isEqualTo(previousPrice);

            verify(timeslotRepository, never()).findById(any());
            verify(timeslotTransactionHistoryRepository, never()).save(any());

            verify(redisLockService).unlock(REQ_ID);
        }
    }

    @Nested
    class FailureCases {

        @Test
        @DisplayName("락 획득 실패 시 예외")
        void bookingLockAcquisitionFailed() {
            // given
            TimeslotBookingRequest req = mockRequest();
            when(redisLockService.lock(REQ_ID)).thenReturn(false);

            // when / then
            assertThatThrownBy(() -> timeslotService.booking(req))
                    .isInstanceOf(ClientErrorException.class);

            verify(redisLockService, never()).unlock(anyString());
            verify(timeslotTransactionHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("타임 슬롯 없으면 예외 발생")
        void bookingTimeslotNotFound() {
            // given
            TimeslotBookingRequest req = mockRequest();
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(null);
            when(timeslotRepository.findById(TIMESLOT_ID)).thenReturn(Optional.empty());

            // when / then
            assertThatThrownBy(() -> timeslotService.booking(req))
                    .isInstanceOf(ClientErrorException.class);

            verify(redisLockService).unlock(REQ_ID.toString());
            verify(timeslotTransactionHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("아직 예약 가능 시간이 되지 않았으면 예외가 발생")
        void bookingTimeslotNotOpenAt() {
            // given
            TimeslotBookingRequest req = mockRequest();
            when(redisLockService.lock(REQ_ID)).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(null);

            // openAt 미래(아직 오픈 전)
            LocalDateTime now = LocalDateTime.now();
            Timeslot notOpen = new Timeslot(
                    TIMESLOT_ID, 1L, 2L,
                    now.plusDays(1), now.plusMinutes(10),
                    1000L, true
            );
            when(timeslotRepository.findById(TIMESLOT_ID)).thenReturn(Optional.of(notOpen));

            // when / then
            assertThatThrownBy(() -> timeslotService.booking(req))
                    .isInstanceOf(ClientErrorException.class);

            verify(redisLockService).unlock(REQ_ID);
            verify(timeslotTransactionHistoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("타임슬롯이 이미 예약된 상태이면 예외 발생")
        void bookingTimeslotAlreadyReserved() {
            // given
            TimeslotBookingRequest req = mockRequest();
            when(redisLockService.lock(REQ_ID.toString())).thenReturn(true);
            when(timeslotTransactionHistoryRepository.findByRequestIdAndTransactionType(REQ_ID, TransactionType.BOOKING))
                    .thenReturn(null);

            Timeslot alreadyReserved = new Timeslot(
                    TIMESLOT_ID, 1L, 2L,
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().minusMinutes(1),
                    1000L,
                    false // 예약 불가 상태
            );
            when(timeslotRepository.findById(TIMESLOT_ID)).thenReturn(Optional.of(alreadyReserved));

            // when / then
            assertThatThrownBy(() -> timeslotService.booking(req))
                    .isInstanceOf(ClientErrorException.class);

            verify(redisLockService).unlock(REQ_ID);
            verify(timeslotTransactionHistoryRepository, never()).save(any());
        }
    }
}
