package cs.escaperoomhub.monolithic.reservation.service;

import cs.escaperoomhub.monolithic.exception.ClientErrorException;
import cs.escaperoomhub.monolithic.exception.CommonErrors;
import cs.escaperoomhub.monolithic.point.service.PointService;
import cs.escaperoomhub.monolithic.reservation.dto.request.CreateReservationRequest;
import cs.escaperoomhub.monolithic.reservation.dto.request.PlaceReservationRequest;
import cs.escaperoomhub.monolithic.reservation.entity.Reservation;
import cs.escaperoomhub.monolithic.reservation.repository.ReservationRepository;
import cs.escaperoomhub.monolithic.store.service.TimeslotService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    private PlaceReservationRequest makePlaceReq(long reservationId) {
        var req = mock(PlaceReservationRequest.class);
        given(req.getReservationId()).willReturn(reservationId);
        return req;
    }

    @Test
    @DisplayName("예약 생성: save 호출되고 id 반환")
    void createReservationSuccess() {
        // given
        CreateReservationRequest req = mock(CreateReservationRequest.class);
        given(req.getUserId()).willReturn(7L);
        given(req.getTimeslotId()).willReturn(111L);
        given(req.getPersonCount()).willReturn(3L);

        given(reservationRepository.save(any(Reservation.class)))
                .willAnswer(inv -> inv.getArgument(0));

        // when
        var res = reservationService.createReservation(req);

        // then
        verify(reservationRepository).save(any(Reservation.class));
        assertThat(res.getReservationId()).isNotNull();
    }

    @Test
    @DisplayName("예약 처리 성공: findById → reserve → point use → save, 상태 COMPLETED")
    void placeReservationSuccess() {
        // given
        long reservationId = 1L;
        Reservation reservation = new Reservation(reservationId, 7L, 111L, 3L);
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(timeslotService.reserve(111L, 3L)).willReturn(30_000L);

        PlaceReservationRequest req = makePlaceReq(reservationId);

        // when
        reservationService.placeReservation(req);

        // then
        verify(reservationRepository).findById(reservationId);
        verify(timeslotService).reserve(111L, 3L);
        verify(pointService).use(7L, 30_000L);
        verify(reservationRepository).save(reservation);
        assertThat(reservation.getStatus()).isEqualTo(Reservation.ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("타임슬롯 예약 실패 시 예외 전파, use, save 호출 안 됨")
    void placeReservationTimeslotFails() {
        long reservationId = 2L;
        var reservation = new Reservation(reservationId, 7L, 222L, 2L);
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(timeslotService.reserve(222L, 2L)).willThrow(CommonErrors.timeslotAlreadyReserved());

        var req = makePlaceReq(reservationId);

        assertThatThrownBy(() -> reservationService.placeReservation(req))
                .isInstanceOf(ClientErrorException.class);

        verify(pointService, never()).use(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("포인트 부족 시 예외 전파, save 호출 안 됨")
    void placeReservationPointFails() {
        long reservationId = 3L;
        var reservation = new Reservation(reservationId, 9L, 333L, 4L);
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));
        given(timeslotService.reserve(333L, 4L)).willReturn(40_000L);
        willThrow(CommonErrors.insufficientBalance(40000L, 10000L))
                .given(pointService).use(9L, 40_000L);

        var req = makePlaceReq(reservationId);

        assertThatThrownBy(() -> reservationService.placeReservation(req))
                .isInstanceOf(ClientErrorException.class);

        verify(reservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 COMPLETED면 조용히 종료(추가 호출 없음)")
    void placeReservationAlreadyCompletedReturn() {
        long reservationId = 4L;
        var reservation = new Reservation(reservationId, 7L, 444L, 2L);
        reservation.complete(); // 미리 완료 상태
        given(reservationRepository.findById(reservationId)).willReturn(java.util.Optional.of(reservation));

        var req = makePlaceReq(reservationId);

        reservationService.placeReservation(req);

        verify(timeslotService, never()).reserve(anyLong(), anyLong());
        verify(pointService, never()).use(anyLong(), anyLong());
        verify(reservationRepository, never()).save(any());
    }
}