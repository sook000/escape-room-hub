package cs.escaperoomhub.monolithic.reservation.service;

import cs.escaperoomhub.monolithic.exception.ClientErrorException;
import cs.escaperoomhub.monolithic.exception.ErrorCode;
import cs.escaperoomhub.monolithic.reservation.dto.request.PlaceReservationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class ReservationServiceConcurrencyTest {
    @Autowired
    ReservationService reservationService;

    @Test
    void concurrentPlaceReservation() throws ExecutionException, InterruptedException, TimeoutException {
        Long id = 229194860457967616L;

        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);

        Callable<String> task = () -> {
            start.await();
            try {
                reservationService.placeReservation(new PlaceReservationRequest(id));
                return "OK";
            } catch (ClientErrorException e) {
                // 409 (락 실패 or 타임슬롯 충돌)만 허용
                if (e.getErrorCode() == ErrorCode.LOCK_ACQUISITION_FAILED
                        || e.getErrorCode() == ErrorCode.TIMESLOT_ALREADY_RESERVED) {
                    return "409";
                }
                throw e;
            }
        };

        Future<String> f1 = pool.submit(task);
        Future<String> f2 = pool.submit(task);

        start.countDown(); // 동시에 출발

        String r1 = f1.get(10, TimeUnit.SECONDS);
        String r2 = f2.get(10, TimeUnit.SECONDS);

        // 하나는 성공, 하나는 409 계열이어야 정상
        System.out.println("r1=" + r1 + ", r2=" + r2);

        pool.shutdown();
    }
}
