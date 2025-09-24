package cs.escaperoomhub.store.data;

import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.store.entity.Timeslot;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

@SpringBootTest
public class DataInitializer {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();

    @Test
    void initialize() throws InterruptedException {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        Timeslot timeslot = new Timeslot(snowflake.nextId(), 1L, 1L, past, past, 10000L);
        Timeslot timeslot2 = new Timeslot(snowflake.nextId(), 1L, 2L, past, past, 20000L);

        transactionTemplate.executeWithoutResult(status -> {
            entityManager.persist(timeslot);
            entityManager.persist(timeslot2);
        });

    }
}
