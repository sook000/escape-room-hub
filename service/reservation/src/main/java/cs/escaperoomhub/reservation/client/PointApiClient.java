package cs.escaperoomhub.reservation.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import cs.escaperoomhub.common.exceptionstarter.*;
import cs.escaperoomhub.reservation.client.dto.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.io.InputStream;


@Slf4j
@Component
@RequiredArgsConstructor
public class PointApiClient {

    private RestClient restClient;
    private final ObjectMapper objectMapper;
    private final StoreToReservationErrorMapper errorMapper;

    @Value("${endpoints.point-service.url}")
    private String pointServiceUrl;

    @PostConstruct
    public void initRestClient() {
        this.restClient = RestClient.builder()
                .baseUrl(pointServiceUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> {
                    ErrorResponse er = readErrorResponse(res.getBody());
                    int http = res.getStatusCode().value();
                    String remoteCode = er != null ? er.getCode() : null;

                    ErrorCode mapped = errorMapper.map(remoteCode, http);
                    log.warn("PointApiClient error: http={}, code={}, traceId={}, path={}, msg={}",
                            http, remoteCode,
                            er != null ? er.getTraceId() : null,
                            er != null ? er.getPath() : null,
                            er != null ? er.getMessage() : "null");

                    // 5xx는 재시도 가능 예외로
                    if (res.getStatusCode().is5xxServerError()) {
                        throw new RetryableBusinessException(CommonErrorCode.EXTERNAL_UNAVAILABLE, "포인트 서비스 장애");
                    }
                    // 4xx는 비재시도
                    throw new ClientErrorException(mapped, mapped.getMessage());
                })
                .build();
    }

    @Retryable(
            retryFor = RetryableBusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void use(PointUseApiRequest request) {
        restClient
                .post()
                .uri("/point/use")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    @Retryable(
            retryFor = RetryableBusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500)
    )
    public void cancel(PointUseCancelApiRequest request) {
        restClient
                .post()
                .uri("/point/use/cancel")
                .body(request)
                .retrieve()
                .body(TimeslotBookingCancelApiResponse.class);
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