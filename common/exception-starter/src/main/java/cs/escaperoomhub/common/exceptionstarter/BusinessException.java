package cs.escaperoomhub.common.exceptionstarter;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    public BusinessException(ErrorCode code) {
        super(code.getMessage());
        this.errorCode = code;
    }

    public BusinessException(ErrorCode code, String msg) {
        super(msg);
        this.errorCode = code;
    }

    public BusinessException(ErrorCode code, Throwable cause) {
        super(code.getMessage(), cause);
        this.errorCode = code;
    }

    public BusinessException(ErrorCode code, String msg, Throwable cause) {
        super(msg, cause);
        this.errorCode = code;
    }
}