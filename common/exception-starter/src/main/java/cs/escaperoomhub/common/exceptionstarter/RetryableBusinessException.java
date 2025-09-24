package cs.escaperoomhub.common.exceptionstarter;

public class RetryableBusinessException extends ServerErrorException {
    public RetryableBusinessException(CommonErrorCode c, String m, Throwable t) { super(c, m, t); }
    public RetryableBusinessException(Throwable t) { super(CommonErrorCode.EXTERNAL_UNAVAILABLE, "Temporary upstream issue", t); }
}