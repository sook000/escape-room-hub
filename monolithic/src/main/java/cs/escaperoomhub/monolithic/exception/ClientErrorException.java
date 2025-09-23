package cs.escaperoomhub.monolithic.exception;

import cs.escaperoomhub.monolithic.exception.BusinessException;
import cs.escaperoomhub.monolithic.exception.ErrorCode;

public class ClientErrorException extends BusinessException {
    public ClientErrorException(ErrorCode c) { super(c); }
    public ClientErrorException(ErrorCode c, String m) { super(c, m); }
    public ClientErrorException(ErrorCode c, String m, Throwable t) { super(c, m, t); }
}