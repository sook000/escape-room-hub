package cs.escaperoomhub.store.controller;

import cs.escaperoomhub.common.exceptionstarter.BusinessException;
import cs.escaperoomhub.common.exceptionstarter.ClientErrorException;
import cs.escaperoomhub.common.exceptionstarter.CommonErrorCode;
import cs.escaperoomhub.store.dto.request.TimeslotBookingCancelRequest;
import cs.escaperoomhub.store.dto.request.TimeslotBookingRequest;
import cs.escaperoomhub.store.dto.response.TimeslotBookingCancelResponse;
import cs.escaperoomhub.store.dto.response.TimeslotBookingResponse;
import cs.escaperoomhub.store.exception.StoreErrorCode;
import cs.escaperoomhub.store.service.RedisLockService;
import cs.escaperoomhub.store.service.TimeslotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TimeslotController {
    private final TimeslotService timeslotService;

    int count = 0;

    @PostMapping("/timeslot/booking")
    public ResponseEntity<TimeslotBookingResponse> booking(
            @Valid @RequestBody TimeslotBookingRequest request) {

        // Retryable를 테스트하기 위한 로직
//        System.out.println("timeslot service start");
//        if (count % 2 == 0) {
//            count++;
////            throw new ClientErrorException(StoreErrorCode.TIMESLOT_NOT_OPEN_YET, "booking service error");
//            throw new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR, "booking service error");
//        }

        TimeslotBookingResponse response = timeslotService.booking(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/timeslot/booking/cancel")
    public ResponseEntity<TimeslotBookingCancelResponse> cancel(
            @Valid @RequestBody TimeslotBookingCancelRequest request) {
        TimeslotBookingCancelResponse response = timeslotService.cancel(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
