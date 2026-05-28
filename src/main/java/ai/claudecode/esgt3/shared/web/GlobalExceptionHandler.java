package ai.claudecode.esgt3.shared.web;

import ai.claudecode.esgt3.shared.exception.EsgException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        log.warn("접근 거부 - path={}, msg={}", req.getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse.of("ACCESS_DENIED", "접근이 거부되었습니다.", MDC.get("traceId")));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException e, HttpServletRequest req) {
        log.warn("인증 실패 - path={}", req.getRequestURI());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse.of("UNAUTHORIZED", "인증이 필요합니다.", MDC.get("traceId")));
    }

    @ExceptionHandler(EsgException.class)
    public ResponseEntity<ErrorResponse> handleEsg(EsgException e) {
        HttpStatus status = switch (e.getErrorCode()) {
            case "INVALID_CREDENTIALS", "REFRESH_TOKEN_REVOKED", "INVALID_TOKEN" -> HttpStatus.UNAUTHORIZED;
            case "POLICY_LOAD_FAILED", "POLICY_PARSE_FAILED", "POLICY_FIELD_MISSING" ->
                HttpStatus.INTERNAL_SERVER_ERROR;
            default -> HttpStatus.BAD_REQUEST;
        };
        log.warn("EsgException - errorCode={}, message={}", e.getErrorCode(), e.getMessage());
        return ResponseEntity.status(status)
            .body(ErrorResponse.of(e.getErrorCode(), e.getMessage(), MDC.get("traceId")));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
            .body(ErrorResponse.of("VALIDATION_FAILED", message, MDC.get("traceId")));
    }
}
