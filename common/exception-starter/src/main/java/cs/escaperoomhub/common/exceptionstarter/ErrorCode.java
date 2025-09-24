package cs.escaperoomhub.common.exceptionstarter;

public interface ErrorCode {
    int getStatus();
    String getCode();
    String getMessage();
}
