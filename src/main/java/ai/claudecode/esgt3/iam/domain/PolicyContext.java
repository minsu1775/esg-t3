package ai.claudecode.esgt3.iam.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * ABAC 정책 평가 입력 - 네 요소 묶음.
 *
 * <p>design.md §3.2:
 * {@code PolicyContext = (Subject, Resource, Action, Environment)}.
 */
public record PolicyContext(
    Subject subject,
    Resource resource,
    PolicyAction action,
    Environment environment
) {
    public PolicyContext {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(resource, "resource");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(environment, "environment");
    }

    public static PolicyContext of(Subject subject, Resource resource, PolicyAction action) {
        return new PolicyContext(subject, resource, action, Environment.now());
    }

    /**
     * 환경 속성 - 시각·IP·MFA 여부.
     */
    public record Environment(Instant timestamp, String requestIp, boolean mfaVerified) {
        public static Environment now() {
            return new Environment(Instant.now(), null, false);
        }
    }
}
