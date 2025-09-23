package cs.escaperoomhub.monolithic.exception;

public class RetryableBusinessException extends ServerErrorException {
    public RetryableBusinessException(ErrorCode c, String m, Throwable t) { super(c, m, t); }
    public RetryableBusinessException(Throwable t) { super(ErrorCode.EXTERNAL_UNAVAILABLE, "Temporary upstream issue", t); }
}