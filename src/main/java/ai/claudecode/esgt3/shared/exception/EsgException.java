package ai.claudecode.esgt3.shared.exception;

/**
 * esg-t3 전역 공통 예외의 최상위 타입.
 * 모든 도메인·인프라 예외는 이 클래스를 상속해야 한다.
 */
public abstract class EsgException extends RuntimeException {

    private final String errorCode;

    protected EsgException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected EsgException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
