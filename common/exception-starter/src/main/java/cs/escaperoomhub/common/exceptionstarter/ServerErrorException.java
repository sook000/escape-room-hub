package cs.escaperoomhub.common.exceptionstarter;

public class ServerErrorException extends BusinessException {
    public ServerErrorException(CommonErrorCode c) { super(c); }
    public ServerErrorException(CommonErrorCode c, String m) { super(c, m); }
    public ServerErrorException(CommonErrorCode c, String m, Throwable t) { super(c, m, t); }
}