package cs.escaperoomhub.monolithic.store.service;

import cs.escaperoomhub.monolithic.store.entity.Timeslot;
import cs.escaperoomhub.monolithic.store.repository.TimeslotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
class TimeslotApiOptimisticLockTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    TimeslotRepository timeslotRepository;

    @BeforeEach
    void setUp() {
        // 열린 타임슬롯 준비 (버전 컬럼이 있어야 낙관락 동작)
        Long id = 1L;
        LocalDateTime past = LocalDateTime.now().minusMinutes(1);
        Timeslot t = new Timeslot(id, 1L, 1L, past, past, 10000L);
        timeslotRepository.saveAndFlush(t);
    }

    @Test
    void concurrentPlaceReservation() throws Exception {
        int threads = 2;
        ExecutorService pool = newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch latch = new CountDownLatch(threads);

        List<Integer> statuses = Collections.synchronizedList(new ArrayList<>());
        List<String> bodies = Collections.synchronizedList(new ArrayList<>());

        Runnable task = () -> {
            try {
                start.await(); // 동시에 출발
                String json = """
                  { "timeslotId": 100, "personCount": 1 }
                """;
                MvcResult res = mockMvc.perform(post("/timeslot/test")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andReturn();
                statuses.add(res.getResponse().getStatus());
                bodies.add(res.getResponse().getContentAsString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                latch.countDown();
            }
        };

        pool.submit(task);
        pool.submit(task);

        start.countDown();
        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        // 하나는 성공, 하나는 409(CONFLICT)
        long success = statuses.stream().filter(s -> s == 200 || s == 201 || s == 204).count();
        long conflict = statuses.stream().filter(s -> s == 409).count();
        assertThat(success).isEqualTo(1);
        assertThat(conflict).isEqualTo(1);

        // 에러 바디 검사(전역 핸들러에서 낙관락 -> TIMESLOT_ALREADY_RESERVED로 매핑되어야 함)
        String conflictBody = bodies.get(statuses.indexOf(409));
        assertThat(conflictBody).contains("\"code\":\"T001\""); // 예: T001 = TIMESLOT_ALREADY_RESERVED
    }
}
