package cs.escaperoomhub.reservation.client;

import cs.escaperoomhub.common.exceptionstarter.CommonErrorCode;
import cs.escaperoomhub.common.exceptionstarter.ErrorCode;
import cs.escaperoomhub.reservation.exception.ReservationErrorCode;

import java.util.Optional;

public class PointToReservationErrorMapper {
    public ErrorCode map(String remoteCode, int httpStatus) {
        if ("P001".equals(remoteCode)) return ReservationErrorCode.INSUFFICIENT_BALANCE;

        Optional<CommonErrorCode> commonErrorCode = CommonErrorCode.byCode(remoteCode);
        return commonErrorCode.orElseGet(() -> httpStatus >= 500 ? CommonErrorCode.EXTERNAL_UNAVAILABLE
                : CommonErrorCode.INVALID_INPUT_VALUE);
    }
}
