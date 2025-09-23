package cs.escaperoomhub.monolithic.exception;

import cs.escaperoomhub.monolithic.exception.BusinessException;
import cs.escaperoomhub.monolithic.exception.ErrorCode;

public class ServerErrorException extends BusinessException {
    public ServerErrorException(ErrorCode c) { super(c); }
    public ServerErrorException(ErrorCode c, String m) { super(c, m); }
    public ServerErrorException(ErrorCode c, String m, Throwable t) { super(c, m, t); }
}