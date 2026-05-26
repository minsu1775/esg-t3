package ai.claudecode.esgt3.shared.web;

import java.time.Instant;

/**
 * 표준 에러 응답. traceId를 포함하여 사용자가 지원 요청 시 운영자가 역추적 가능하게 한다.
 *
 * @param error 에러 코드 (예: POLICY_DENIED, VALIDATION_FAILED)
 * @param message 한국어 사용자 메시지
 * @param traceId OpenTelemetry trace id (Tempo·AuditLog 역추적용)
 * @param timestamp 응답 생성 시각
 */
public record ErrorResponse(
    String error,
    String message,
    String traceId,
    Instant timestamp
) {
    public static ErrorResponse of(String error, String message, String traceId) {
        return new ErrorResponse(error, message, traceId, Instant.now());
    }
}
