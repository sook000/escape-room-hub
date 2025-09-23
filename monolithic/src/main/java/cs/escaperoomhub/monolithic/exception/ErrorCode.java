package cs.escaperoomhub.monolithic.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
    METHOD_NOT_ALLOWED(405, "C002", "Method Not Allowed"),
//    HANDLE_ACCESS_DENIED(403, "C003", "Access is denied"),
    RESOURCE_NOT_FOUND(404, "C004", "Resource not found"),

    // Member
//    EMAIL_DUPLICATION(400, "M001", "Email is Duplication"),
//    LOGIN_INPUT_INVALID(400, "M002", "Login input is invalid"),
//    MEMBER_NOT_FOUND(404, "M003", "Member not found"),

    // Point
    INSUFFICIENT_BALANCE(409, "P001", "Insufficient balance"),

    // Store
    TIMESLOT_ALREADY_RESERVED(409, "T001", "Timeslot already reserved"),
    TIMESLOT_NOT_OPEN_YET(400, "T002", "Timeslot not open yet"),

    // Server
    INTERNAL_SERVER_ERROR(500, "S001", "Internal server error"),

    // 외부/일시장애 계열
    EXTERNAL_UNAVAILABLE(503, "X001", "Upstream unavailable"),
//    UPSTREAM_TIMEOUT(504, "X002", "Upstream timed out"),
//    RATE_LIMITED(429, "X003", "Rate limited"),
    ;

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
