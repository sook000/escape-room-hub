package cs.escaperoomhub.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@EntityScan(basePackages = "cs.escaperoomhub")
@EnableJpaRepositories(basePackages = "cs.escaperoomhub")
@SpringBootApplication(scanBasePackages = {"cs.escaperoomhub.reservation",
        "cs.escaperoomhub.common.exceptionstarter"})
public class ReservationApplication {
    public static void main(String[] args) {
        SpringApplication.run(ReservationApplication.class, args);
    }
}
