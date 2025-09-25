package cs.escaperoomhub.reservation.client;

import cs.escaperoomhub.common.exceptionstarter.CommonErrorCode;
import cs.escaperoomhub.common.exceptionstarter.ErrorCode;
import cs.escaperoomhub.reservation.exception.ReservationErrorCode;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class StoreToReservationErrorMapper {

    public ErrorCode map(String remoteCode, int httpStatus) {
        if ("T001".equals(remoteCode)) return ReservationErrorCode.TIMESLOT_ALREADY_RESERVED;
        if ("T002".equals(remoteCode)) return ReservationErrorCode.TIMESLOT_NOT_OPEN_YET;

//        Optional<CommonErrorCode> commonErrorCode = CommonErrorCode.byCode(remoteCode);
//        if (commonErrorCode.isPresent()) return commonErrorCode.get();
//
//        return httpStatus >= 500 ? CommonErrorCode.EXTERNAL_UNAVAILABLE
//                : CommonErrorCode.INVALID_INPUT_VALUE;

        Optional<CommonErrorCode> commonErrorCode = CommonErrorCode.byCode(remoteCode);
        return commonErrorCode.orElseGet(() -> httpStatus >= 500 ? CommonErrorCode.EXTERNAL_UNAVAILABLE
                : CommonErrorCode.INVALID_INPUT_VALUE);

    }
}