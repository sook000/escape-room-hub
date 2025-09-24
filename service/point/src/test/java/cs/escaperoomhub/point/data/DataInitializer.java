package cs.escaperoomhub.point.data;

import cs.escaperoomhub.common.snowflake.Snowflake;
import cs.escaperoomhub.point.entity.Point;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
public class DataInitializer {
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    TransactionTemplate transactionTemplate;
    Snowflake snowflake = new Snowflake();

    @Test
    void initialize() throws InterruptedException {
        Point point = new Point(snowflake.nextId(), 100L, 100000L);
        Point point2 = new Point(snowflake.nextId(), 200L, 200000L);

        transactionTemplate.executeWithoutResult(status -> {
            entityManager.persist(point);
            entityManager.persist(point2);
        });

    }
}
