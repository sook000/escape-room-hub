package cs.escaperoomhub.monolithic.exception;

public final class Errors {
    private Errors() {}

    public static ClientErrorException timeslotNotOpenYet() {
        return new ClientErrorException(
                ErrorCode.TIMESLOT_NOT_OPEN_YET,
                "아직 예약 오픈이 열리지 않은 타임슬롯입니다."
        );
    }
    public static ClientErrorException timeslotAlreadyReserved() {
        return new ClientErrorException(
                ErrorCode.TIMESLOT_ALREADY_RESERVED,
                "이미 예약된 타임슬롯입니다."
        );
    }
    public static ClientErrorException insufficientBalance(long required, long current) {
        return new ClientErrorException(
                ErrorCode.INSUFFICIENT_BALANCE,
                String.format("잔액이 부족합니다. 필요: %d, 보유: %d", required, current)
        );
    }

    public static ClientErrorException notFound(String message) {
        return new ClientErrorException(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ClientErrorException lockAcquisitionFailed(String key) {
        return new ClientErrorException(
                ErrorCode.LOCK_ACQUISITION_FAILED,
                String.format("락을 획득하지 못했습니다. key=%s", key)
        );
    }
}