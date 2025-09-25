package cs.escaperoomhub.reservation.client;

import cs.escaperoomhub.reservation.client.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;


@Slf4j
@Component
@RequiredArgsConstructor
public class PointApiClient {

    private RestClient restClient;

    @Value("${endpoints.point-service.url}")
    private String pointServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(pointServiceUrl);
    }


    public void use(PointUseApiRequest request) {
        restClient
                .post()
                .uri("/point/use")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void cancel(PointUseCancelApiRequest request) {
        restClient
                .post()
                .uri("/point/use/cancel")
                .body(request)
                .retrieve()
                .body(TimeslotBookingCancelApiResponse.class);
    }

}