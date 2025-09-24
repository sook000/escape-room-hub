package cs.escaperoomhub.point;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"cs.escaperoomhub.point","cs.escaperoomhub.common.exceptionstarter"})
public class PointApplication {
    public static void main(String[] args) {
        SpringApplication.run(PointApplication.class, args);
    }
}
