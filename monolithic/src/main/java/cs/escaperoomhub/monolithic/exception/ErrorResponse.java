package cs.escaperoomhub.monolithic.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String message;
    private String code;

    private List<FieldError> errors;
    private String traceId;
    private String serviceId;
    private final String path;
    private final String timestamp;

    @Getter
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String value;
        private String reason;
    }

    public static ErrorResponse of(ErrorCode ec, String traceId, String serviceId, String path) {
        return ErrorResponse.builder()
                .message(ec.getMessage())
                .code(ec.getCode())
                .traceId(traceId)
                .serviceId(serviceId)
                .path(path)
                .timestamp(java.time.OffsetDateTime.now().toString())
                .build();
    }

    public static ErrorResponse of(ErrorCode ec, List<FieldError> fieldErrors,
                                   String traceId, String serviceId, String path) {
        return ErrorResponse.builder()
                .message(ec.getMessage())
                .code(ec.getCode())
                .errors(fieldErrors)
                .traceId(traceId)
                .serviceId(serviceId)
                .path(path)
                .timestamp(java.time.OffsetDateTime.now().toString())
                .build();
    }

    public static ErrorResponse ofBindingResult(ErrorCode ec, org.springframework.validation.BindingResult br,
                                                String traceId, String serviceId, String path) {
        List<FieldError> list = br.getFieldErrors().stream()
                .map(fe -> new FieldError(
                        fe.getField(),
                        fe.getRejectedValue() == null ? null : String.valueOf(fe.getRejectedValue()),
                        fe.getDefaultMessage()
                ))
                .toList();
        return of(ec, list, traceId, serviceId, path);
    }

    public static ErrorResponse ofTypeMismatch(ErrorCode ec, org.springframework.web.method.annotation.MethodArgumentTypeMismatchException e,
                                               String traceId, String serviceId, String path) {
        List<FieldError> list = List.of(new FieldError(
                e.getName(),
                e.getValue() == null ? null : String.valueOf(e.getValue()),
                "Type mismatch: expected " + (e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "unknown")
        ));
        return of(ec, list, traceId, serviceId, path);
    }
}