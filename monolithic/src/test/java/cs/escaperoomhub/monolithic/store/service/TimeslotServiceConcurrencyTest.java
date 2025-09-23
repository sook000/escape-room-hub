package cs.escaperoomhub.monolithic.store.service;

import cs.escaperoomhub.monolithic.store.entity.Timeslot;
import cs.escaperoomhub.monolithic.store.repository.TimeslotRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TimeslotServiceConcurrencyTest {

    @Autowired
    private TimeslotService timeslotService;

    @Autowired
    private TimeslotRepository timeslotRepository;
    @PersistenceContext
    EntityManager em;


    @Test
    void reserveConcurrentRequests() throws InterruptedException {
        // Given: 예약 가능한 타임슬롯을 준비
        Long timeslotId = 100L;
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        Timeslot timeslot = new Timeslot(timeslotId, 1L, 1L, past, past, 10000L);
        timeslotRepository.saveAndFlush(timeslot);

        int numberOfRequests = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger success = new AtomicInteger();
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        long start = System.nanoTime();

        // When: 요청을 동시에 실행
        for (int i = 0; i < numberOfRequests; i++) {
            executorService.submit(() -> {
                try {
                    timeslotService.reserve(timeslotId, 1L);
//                    timeslotService.reserveWithPessimisticLock(timeslotId, 1L);
                    success.incrementAndGet();
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(1, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.println("총 소요(ms): " + elapsedMs);

        // Then
        System.out.println("성공 스레드 개수(정상경우는 1개): " + success.get());
        System.out.println("실패 스레드 개수: " + failures.size());

        assertThat(success.get()).isEqualTo(1);
        assertThat(failures.size()).isEqualTo(numberOfRequests - 1);

        em.clear();
        Timeslot finalTs = timeslotRepository.findById(timeslotId).orElseThrow();
        assertThat(finalTs.isAvailable()).isFalse();
    }
}