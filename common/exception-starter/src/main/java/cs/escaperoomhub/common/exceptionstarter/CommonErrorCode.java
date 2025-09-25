package cs.escaperoomhub.common.exceptionstarter;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Getter
public enum CommonErrorCode implements ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", "Method Not Allowed"),
//    HANDLE_ACCESS_DENIED(403, "C003", "Access is denied"),
    RESOURCE_NOT_FOUND(404, "C004", "Resource not found"),
    LOCK_ACQUISITION_FAILED(409, "C005", "Failed to acquire lock"),
    CONCURRENCY_CONFLICT(409, "C006", "Concurrency conflict"),

    // Server
    INTERNAL_SERVER_ERROR(500, "S001", "Internal server error"),

    // 외부, 일시장애 계열
    EXTERNAL_UNAVAILABLE(503, "X001", "Upstream unavailable"),
//    UPSTREAM_TIMEOUT(504, "X002", "Upstream timed out"),
//    RATE_LIMITED(429, "X003", "Rate limited"),
    ;

    private final int status;
    private final String code;
    private final String message;

    CommonErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    private static final Map<String, CommonErrorCode> BY_CODE = Arrays.stream(values())
            .collect(Collectors.toUnmodifiableMap(CommonErrorCode::getCode, e -> e));

    public static Optional<CommonErrorCode> byCode(String code) {
        if (code == null) return Optional.empty();
        return Optional.ofNullable(BY_CODE.get(code.trim()));
    }
}
