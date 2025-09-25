package cs.escaperoomhub.reservation.exception;

import cs.escaperoomhub.common.exceptionstarter.ErrorCode;
import lombok.Getter;

@Getter
public enum ReservationErrorCode implements ErrorCode {

    INVALID_RESERVATION_STATUS(400, "R001", "Invalid reservation status");

    private final int status;
    private final String code;
    private final String message;

    ReservationErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

