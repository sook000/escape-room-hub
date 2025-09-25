package cs.escaperoomhub.reservation.exception;

import cs.escaperoomhub.common.exceptionstarter.ErrorCode;
import lombok.Getter;

@Getter
public enum ReservationErrorCode implements ErrorCode {

    INVALID_RESERVATION_STATUS(400, "R001", "Invalid reservation status"),

    TIMESLOT_ALREADY_RESERVED(409, "R003", "Timeslot already reserved"),
    TIMESLOT_NOT_OPEN_YET(400, "R004", "Timeslot not open yet"),

    INSUFFICIENT_BALANCE(409, "R005", "Insufficient balance"),
    ;

    private final int status;
    private final String code;
    private final String message;

    ReservationErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}

