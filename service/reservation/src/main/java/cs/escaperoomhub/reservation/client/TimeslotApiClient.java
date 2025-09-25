package cs.escaperoomhub.reservation.client;

import cs.escaperoomhub.reservation.client.dto.TimeslotBookingApiRequest;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingApiResponse;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingCancelApiRequest;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingCancelApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeslotApiClient {

    private RestClient restClient;

    @Value("${endpoints.store-service.url}")
    private String storeServiceUrl;

    @PostConstruct
    public void initRestClient() {
        restClient = RestClient.create(storeServiceUrl);
    }

    public TimeslotBookingApiResponse booking(TimeslotBookingApiRequest request) {
        return restClient
                .post()
                .uri("/timeslot/booking")
                .body(request)
                .retrieve()
                .body(TimeslotBookingApiResponse.class);
    }

    public TimeslotBookingCancelApiResponse cancel(TimeslotBookingCancelApiRequest request) {
        return restClient
                .post()
                .uri("/timeslot/booking/cancel")
                .body(request)
                .retrieve()
                .body(TimeslotBookingCancelApiResponse.class);
    }

}
