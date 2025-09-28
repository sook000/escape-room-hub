package cs.escaperoomhub.store.service;

import cs.escaperoomhub.store.dto.request.TimeslotBookingRequest;
import cs.escaperoomhub.store.entity.Timeslot;
import cs.escaperoomhub.store.repository.TimeslotRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Slf4j
public class TimeslotServiceConcurrencyTest {

    @Autowired
    private TimeslotService timeslotService;
    @Autowired
    private RedisAvailabilityService redisAvailabilityService;

    @Autowired
    private TimeslotRepository timeslotRepository;
    @PersistenceContext
    EntityManager em;

    @BeforeEach
    @Transactional
    void setup() {
        redisAvailabilityService.del(100L);

        timeslotRepository.deleteById(100L);
        em.clear();
        log.info("Redis 캐시 및 DB 초기화 완료.");
    }

    public long generateUserIdByTime() {
        return System.currentTimeMillis();
    }
    @Test
    void reserveConcurrentRequests() throws InterruptedException {
        // Given
        Long timeslotId = 100L;
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        Timeslot timeslot = new Timeslot(timeslotId, 1L, 1L, past, past, 10000L);
        timeslotRepository.saveAndFlush(timeslot);

        int numberOfRequests = 5000;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(numberOfRequests);
        AtomicInteger success = new AtomicInteger();
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        long start = System.nanoTime();

        // When
        for (int i = 0; i < numberOfRequests; i++) {
            executorService.submit(() -> {
                try {
//                    timeslotService.booking2(new TimeslotBookingRequest(UUID.randomUUID().toString(),
//                            generateUserIdByTime() ,timeslotId , 1L));
                    timeslotService.booking(new TimeslotBookingRequest(UUID.randomUUID().toString(),
                            generateUserIdByTime() ,timeslotId , 1L));
                    success.incrementAndGet();
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();
        executorService.awaitTermination(3, TimeUnit.SECONDS);

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        // Then
        log.info("총 소요(ms): {}", elapsedMs);
        log.info("성공 스레드 개수(정상경우는 1개): {}", success.get());
        log.info("실패 스레드 개수: {}", failures.size());

        assertThat(success.get()).isEqualTo(1);
        assertThat(failures.size()).isEqualTo(numberOfRequests - 1);

        em.clear();
        Timeslot finalTs = timeslotRepository.findById(timeslotId).orElseThrow();
        assertThat(finalTs.isAvailable()).isFalse();
    }


    @Test
    void reserveConcurrentRequests2() throws Exception {
        // Given
        Long timeslotId = 100L;

        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        Timeslot ts = new Timeslot(timeslotId, 1L, 1L, past, past, 10000L);
        timeslotRepository.saveAndFlush(ts);
        long startTime = System.nanoTime();

        int N = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done  = new CountDownLatch(N);

        AtomicInteger success = new AtomicInteger();
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < N; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    timeslotService.booking2(new TimeslotBookingRequest(
                            UUID.randomUUID().toString(),
                            System.currentTimeMillis(),
                            timeslotId,
                            1L
                    ));
                    success.incrementAndGet();
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown(); // 동시에 출발
        assertThat(done.await(30, TimeUnit.SECONDS)).isTrue();
        pool.shutdown();
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Then
        assertThat(success.get()).isEqualTo(1);
        assertThat(failures.size()).isEqualTo(N - 1);
        log.info("총 소요(ms): {}", elapsedMs);
        log.info("성공 스레드 개수(정상경우는 1개): {}", success.get());
        log.info("실패 스레드 개수: {}", failures.size());

        // DB: false
        assertThat(timeslotRepository.findById(timeslotId).orElseThrow().isAvailable()).isFalse();

    }
}