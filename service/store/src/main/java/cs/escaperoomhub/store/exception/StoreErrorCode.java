package cs.escaperoomhub.store.exception;

import cs.escaperoomhub.common.exceptionstarter.ErrorCode;
import lombok.Getter;

@Getter
public enum StoreErrorCode implements ErrorCode {
    // Store
    TIMESLOT_ALREADY_RESERVED(409, "T001", "Timeslot already reserved"),
    TIMESLOT_NOT_OPEN_YET(400, "T002", "Timeslot not open yet"),
    ;

    private final int status;
    private final String code;
    private final String message;

    StoreErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
