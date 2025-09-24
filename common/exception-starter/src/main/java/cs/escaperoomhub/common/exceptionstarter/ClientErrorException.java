package cs.escaperoomhub.common.exceptionstarter;

public class ClientErrorException extends BusinessException {
    public ClientErrorException(ErrorCode c) { super(c); }
    public ClientErrorException(ErrorCode c, String m) { super(c, m); }
    public ClientErrorException(ErrorCode c, String m, Throwable t) { super(c, m, t); }
}