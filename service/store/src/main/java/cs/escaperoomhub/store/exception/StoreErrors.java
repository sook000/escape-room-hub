package cs.escaperoomhub.store.exception;

import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;

public final class StoreErrors {
    private StoreErrors() {};

    public static ClientErrorException timeslotNotOpenYet() {
        return new ClientErrorException(
                StoreErrorCode.TIMESLOT_NOT_OPEN_YET,
                "아직 예약 오픈이 열리지 않은 타임슬롯입니다."
        );
    }
    public static ClientErrorException timeslotAlreadyReserved() {
        return new ClientErrorException(
                StoreErrorCode.TIMESLOT_ALREADY_RESERVED,
                "이미 예약된 타임슬롯입니다."
        );
    }
}
