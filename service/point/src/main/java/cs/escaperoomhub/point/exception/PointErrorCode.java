package cs.escaperoomhub.point.exception;

import cs.escaperoomhub.common.exceptionstarter.ErrorCode;
import lombok.Getter;

@Getter
public enum PointErrorCode implements ErrorCode {

    INSUFFICIENT_BALANCE(409, "P001", "Insufficient balance"),
    ;

    private final int status;
    private final String code;
    private final String message;

    PointErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
