package ai.claudecode.esgt3.iam.domain;

import java.util.Objects;

/**
 * 정책 평가 결과. policyId·reason은 AuditLog·OTel Span attribute에 사용된다.
 */
public record PolicyDecision(PolicyEffect effect, String policyId, String reason) {

    public PolicyDecision {
        Objects.requireNonNull(effect, "effect");
        if (effect != PolicyEffect.NOT_APPLICABLE) {
            Objects.requireNonNull(policyId, "policyId");
        }
    }

    public static PolicyDecision permit(String policyId, String reason) {
        return new PolicyDecision(PolicyEffect.PERMIT, policyId, reason);
    }

    public static PolicyDecision deny(String policyId, String reason) {
        return new PolicyDecision(PolicyEffect.DENY, policyId, reason);
    }

    public static PolicyDecision notApplicable() {
        return new PolicyDecision(PolicyEffect.NOT_APPLICABLE, null, "매칭 정책 없음");
    }

    public boolean isAllowed() {
        return effect == PolicyEffect.PERMIT;
    }
}
