package ai.claudecode.esgt3.shared.exception;

import java.util.Objects;

/**
 * esg-t3 전역 공통 예외의 최상위 타입.
 * 모든 도메인·인프라 예외는 이 클래스를 상속해야 한다.
 *
 * <p>{@code errorCode}는 외부 API 응답({@code ErrorResponse.error})으로 노출되므로
 * 컨벤션을 따라야 한다 — {@code SCREAMING_SNAKE_CASE}, 모듈/도메인 의미 포함
 * (예: {@code POLICY_DENIED}, {@code LEGAL_ENTITY_NOT_FOUND}).
 */
public abstract class EsgException extends RuntimeException {

    private final String errorCode;

    protected EsgException(String errorCode, String message) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode는 null일 수 없습니다");
    }

    protected EsgException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode는 null일 수 없습니다");
    }

    public String getErrorCode() {
        return errorCode;
    }
}
