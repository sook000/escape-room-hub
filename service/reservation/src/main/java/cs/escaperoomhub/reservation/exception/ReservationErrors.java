package cs.escaperoomhub.reservation.exception;

import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;
import cs.escaperoomhub.reservation.entity.Reservation;

import static cs.escaperoomhub.reservation.exception.ReservationErrorCode.INVALID_RESERVATION_STATUS;

public final class ReservationErrors {
    private ReservationErrors() {};

    public static ClientErrorException invalidReservationStatus(Reservation.ReservationStatus expected,
                                                                Reservation.ReservationStatus actual) {
        return new ClientErrorException(INVALID_RESERVATION_STATUS,
                String.format("잘못된 reservation 상태 변경 요청: 현재 상태가 %s가 아닙니다. 현재 상태=%s ", expected, actual)
        );
    }
}
