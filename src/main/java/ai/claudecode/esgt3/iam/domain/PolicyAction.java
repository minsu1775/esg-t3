package ai.claudecode.esgt3.iam.domain;

/**
 * ABAC 액션 타입. design.md §3.2.
 *
 * <p>DELETE는 정책상 거의 사용되지 않음 (INSERT-only 원칙).
 * 정책 YAML의 {@code action} 필드와 1:1 대응.
 */
public enum PolicyAction {
    READ,
    WRITE,
    APPROVE,
    VERIFY,
    EXPORT,
    COMMENT,
    SIGN,
    DELETE
}
