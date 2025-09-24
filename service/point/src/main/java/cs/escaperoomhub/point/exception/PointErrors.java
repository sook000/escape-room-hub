package cs.escaperoomhub.point.exception;

import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;

public final class PointErrors {
    private PointErrors() {};

    public static ClientErrorException insufficientBalance(long required, long current) {
        return new ClientErrorException(
                PointErrorCode.INSUFFICIENT_BALANCE,
                String.format("잔액이 부족합니다. 필요: %d, 보유: %d", required, current)
        );
    }
}
