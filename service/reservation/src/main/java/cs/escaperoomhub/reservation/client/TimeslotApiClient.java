package cs.escaperoomhub.reservation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import cs.escaperoomhub.common.exceptionstarter.*;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingApiRequest;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingApiResponse;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingCancelApiRequest;
import cs.escaperoomhub.reservation.client.dto.TimeslotBookingCancelApiResponse;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class TimeslotApiClient {

    private RestClient restClient;
    private final ObjectMapper objectMapper;
    private final StoreToReservationErrorMapper errorMapper;
    @Value("${endpoints.store-service.url}")
    private String storeServiceUrl;

    @PostConstruct
    public void initRestClient() {
        this.restClient = RestClient.builder()
                .baseUrl(storeServiceUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
                    ErrorResponse er = readErrorResponse(res.getBody());

                    int http = res.getStatusCode().value();
                    String remoteCode = er != null ? er.getCode() : null;

                    ErrorCode mapped = errorMapper.map(remoteCode, http);

                    String message = mapped.getMessage();

                    log.info("TimeslotApiClient error: http={}, code={}, traceId={}, path={}, msg={}",
                            http, remoteCode,
                            er != null ? er.getTraceId() : null,
                            er != null ? er.getPath() : null,
                            er != null ? er.getMessage() : "null");

                    if (res.getStatusCode().is5xxServerError()) {
                        throw new RetryableBusinessException(CommonErrorCode.EXTERNAL_UNAVAILABLE,
                                "매장 서비스 장애");
                    }

                    throw new ClientErrorException(mapped, message);
                })
                .build();
    }

    @Retryable(
        retryFor = { RetryableBusinessException.class },
        maxAttempts = 3,
        backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public TimeslotBookingApiResponse booking(TimeslotBookingApiRequest request) {
        try {
            return restClient
                    .post()
                    .uri("/timeslot/booking")
                    .body(request)
                    .retrieve()
                    .body(TimeslotBookingApiResponse.class);
        } catch (RestClientException e) {
            throw new RetryableBusinessException(CommonErrorCode.EXTERNAL_UNAVAILABLE,
                    "매장 서비스 호출 실패", e);
        }

    }

    @Retryable(
            retryFor = { RetryableBusinessException.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public TimeslotBookingCancelApiResponse cancel(TimeslotBookingCancelApiRequest request) {
        try {
            return restClient
                    .post()
                    .uri("/timeslot/booking/cancel")
                    .body(request)
                    .retrieve()
                    .body(TimeslotBookingCancelApiResponse.class);
        } catch (RestClientException e) {
            throw new RetryableBusinessException(CommonErrorCode.EXTERNAL_UNAVAILABLE,
                    "매장 서비스 호출 실패", e);
        }
    }

    private ErrorResponse readErrorResponse(InputStream is) {
        if (is == null) return null;
        try (is) {
            return objectMapper.readValue(is, ErrorResponse.class);
        } catch (IOException ex) {
            log.warn("Failed to parse upstream ErrorResponse body", ex);
            return null;
        }
    }

}
