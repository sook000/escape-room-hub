package cs.escaperoomhub.common.exceptionstarter;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.StaleObjectStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @Value("${spring.application.name}")
    private String serviceId;

//    private String traceId() {
//        return org.slf4j.MDC.get("traceId"); // 필터에서 넣어둔 값(임시)
//    }
    // 임시 traceId
    private String traceId() {
        return UUID.randomUUID().toString();
    }
    private String path(HttpServletRequest req) {
        return req != null ? req.getRequestURI() : null;
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class,
            StaleObjectStateException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockFailure(ObjectOptimisticLockingFailureException e, HttpServletRequest request) {
        log.warn("Optimistic lock failure", e);
        ErrorCode errorCode = CommonErrorCode.CONCURRENCY_CONFLICT;
        ErrorResponse body = ErrorResponse.of(errorCode, errorCode.getMessage(), traceId(), serviceId, path(request));
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    /**
     *  javax.validation.Valid or @Validated 으로 binding error 발생시 발생한다.
     *  HttpMessageConverter 에서 등록한 HttpMessageConverter binding 못할경우 발생
     *  주로 @RequestBody, @RequestPart 어노테이션에서 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        log.warn("Validation failed", e);
        ErrorResponse body = ErrorResponse.ofBindingResult(CommonErrorCode.INVALID_INPUT_VALUE, e.getBindingResult(), traceId(), serviceId, path(request));
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT_VALUE.getStatus()).body(body);
    }

    /**
     * @ModelAttribute 으로 binding error 발생시 BindException 발생한다.
     * ref https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-ann-modelattrib-method-args
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException e, HttpServletRequest request) {
        log.warn("Bind failed", e);
        ErrorResponse body = ErrorResponse.ofBindingResult(CommonErrorCode.INVALID_INPUT_VALUE, e.getBindingResult(), traceId(), serviceId, path(request));
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT_VALUE.getStatus()).body(body);
    }

    /**
     * enum type 일치하지 않아 binding 못할 경우 발생
     * 주로 @RequestParam enum으로 binding 못했을 경우 발생
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e,
                                                            HttpServletRequest request) {
        log.warn("Type mismatch", e);
        ErrorResponse body = ErrorResponse.ofTypeMismatch(
                CommonErrorCode.INVALID_INPUT_VALUE, e, traceId(), serviceId, path(request));
        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT_VALUE.getStatus()).body(body);
    }

    /**
     * 지원하지 않은 HTTP method 호출 할 경우 발생
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e,
                                                                HttpServletRequest request) {
        log.warn("Method not allowed", e);
        ErrorResponse body = ErrorResponse.of(CommonErrorCode.METHOD_NOT_ALLOWED, traceId(), serviceId, path(request));
        return ResponseEntity.status(CommonErrorCode.METHOD_NOT_ALLOWED.getStatus()).body(body);
    }

    /**
     * Authentication 객체가 필요한 권한을 보유하지 않은 경우 발생합
     */
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e,
//                                                            HttpServletRequest request) {
//        log.warn("Access denied", e);
//        ErrorResponse body = ErrorResponse.of(ErrorCode.HANDLE_ACCESS_DENIED, traceId(), serviceId, path(request));
//        return ResponseEntity.status(ErrorCode.HANDLE_ACCESS_DENIED.getStatus()).body(body);
//    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        log.warn("Entity not found", e);
        ErrorResponse body = ErrorResponse.of(CommonErrorCode.RESOURCE_NOT_FOUND, traceId(), serviceId, path(request));
        return ResponseEntity.status(CommonErrorCode.RESOURCE_NOT_FOUND.getStatus()).body(body);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        if (e instanceof RetryableBusinessException) log.error("Retryable business exception", e);
        else log.warn("Business exception", e);

        ErrorCode ec = e.getErrorCode();
        ErrorResponse body = ErrorResponse.of(ec, e.getMessage(), traceId(), serviceId, path(request));
        return ResponseEntity.status(ec.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAny(Exception e, HttpServletRequest request) {
        log.error("Unhandled exception", e);
        ErrorResponse body = ErrorResponse.of(CommonErrorCode.INTERNAL_SERVER_ERROR,
                traceId(), serviceId, path(request));
        return ResponseEntity.status(CommonErrorCode.INTERNAL_SERVER_ERROR.getStatus()).body(body);
    }

}
