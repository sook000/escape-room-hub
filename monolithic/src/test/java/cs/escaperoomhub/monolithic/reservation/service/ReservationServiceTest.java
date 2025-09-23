package cs.escaperoomhub.monolithic.reservation.service;

import cs.escaperoomhub.monolithic.exception.ClientErrorException;
import cs.escaperoomhub.monolithic.exception.Errors;
import cs.escaperoomhub.monolithic.point.service.PointService;
import cs.escaperoomhub.monolithic.reservation.dto.PlaceReservationRequest;
import cs.escaperoomhub.monolithic.reservation.entity.Reservation;
import cs.escaperoomhub.monolithic.reservation.repository.ReservationRepository;
import cs.escaperoomhub.monolithic.store.service.TimeslotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    ReservationRepository reservationRepository;

    @Mock
    PointService pointService;

    @Mock
    TimeslotService timeslotService;

    @InjectMocks
    ReservationService reservationService;

    private PlaceReservationRequest makeReq(long userId, long timeslotId, long personCount) {
        PlaceReservationRequest req = mock(PlaceReservationRequest.class);
        given(req.getUserId()).willReturn(userId);
        given(req.getTimeslotId()).willReturn(timeslotId);
        given(req.getPersonCount()).willReturn(personCount);
        return req;
    }

    @Test
    @DisplayName("정상 예약 save → reserve → point.use 호출 및 금액 전달")
    void placeReservationSuccess() {
        // given
        var req = makeReq(7L, 111L, 3L);

        // save는 전달받은 Reservation을 리턴
        given(reservationRepository.save(any(Reservation.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(timeslotService.reserve(111L, 3L)).willReturn(30_000L);

        // when
        reservationService.placeReservation(req);

        // then
        verify(reservationRepository).save(any(Reservation.class));
        verify(timeslotService).reserve(111L, 3L);
        verify(pointService).use(7L, 30_000L);
    }

    @Test
    @DisplayName("타임슬롯 예약 실패 시 예외 전파, use는 호출 안 됨 (save는 이미 호출됨)")
    void placeReservationTimeslotFails() {
        // given
        var req = makeReq(7L, 222L, 2L);
        given(reservationRepository.save(any(Reservation.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(timeslotService.reserve(222L, 2L))
                .willThrow(Errors.timeslotAlreadyReserved());

        // when // then
        assertThatThrownBy(() -> reservationService.placeReservation(req))
                .isInstanceOf(ClientErrorException.class);

        // save는 호출되었고, point.use는 호출되지 않아야 한다
        verify(reservationRepository).save(any(Reservation.class));
        verify(pointService, never()).use(anyLong(), anyLong());
    }

    @Test
    @DisplayName("포인트 사용 실패 시 예외 전파, save, reserve는 이미 호출됨")
    void placeReservationPointFails() {
        // given
        var req = makeReq(9L, 333L, 4L);
        given(reservationRepository.save(any(Reservation.class)))
                .willAnswer(inv -> inv.getArgument(0));
        given(timeslotService.reserve(333L, 4L)).willReturn(40_000L);
        willThrow(Errors.insufficientBalance(40000L, 10000L))
                .given(pointService).use(9L, 40_000L);

        // when // then
        assertThatThrownBy(() -> reservationService.placeReservation(req))
                .isInstanceOf(ClientErrorException.class);

        // save,reserve는 호출되고 use에서 실패
        verify(reservationRepository).save(any(Reservation.class));
        verify(timeslotService).reserve(333L, 4L);
        verify(pointService).use(9L, 40_000L);
    }
}