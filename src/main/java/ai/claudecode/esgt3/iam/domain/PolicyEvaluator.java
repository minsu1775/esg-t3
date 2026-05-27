package ai.claudecode.esgt3.iam.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * ABAC 정책 평가 엔진. 순수 도메인 서비스 - DB·Spring 의존 0.
 *
 * <p>평가 알고리즘 (design.md §3.7):
 * <ol>
 *   <li>모든 PolicyDocument에서 PolicyRule을 수집</li>
 *   <li>priority 내림차순 정렬</li>
 *   <li>위에서부터 순회: when 절 매칭 시 → 해당 정책의 effect 반환 (단락)</li>
 *   <li>모든 정책이 매칭 실패 → NOT_APPLICABLE 반환</li>
 * </ol>
 *
 * <p>호출 측({@code PolicyDecision.isAllowed()})은 NOT_APPLICABLE을 DENY로 취급 (DENY-default).
 */
public final class PolicyEvaluator {

    private final List<PolicyRule> sortedRules;

    public PolicyEvaluator(List<PolicyDocument> documents) {
        Objects.requireNonNull(documents, "documents");
        this.sortedRules = documents.stream()
            .flatMap(doc -> doc.rules().stream())
            .sorted(Comparator.comparingInt(PolicyRule::priority).reversed())
            .toList();
    }

    public PolicyDecision evaluate(PolicyContext ctx) {
        Objects.requireNonNull(ctx, "ctx");
        for (PolicyRule rule : sortedRules) {
            if (WhenClauseMatcher.matches(rule.when(), ctx)) {
                String reason = "%s 매칭 (priority=%d)".formatted(rule.id(), rule.priority());
                return rule.effect() == PolicyEffect.PERMIT
                    ? PolicyDecision.permit(rule.id(), reason)
                    : PolicyDecision.deny(rule.id(), reason);
            }
        }
        return PolicyDecision.notApplicable();
    }

    /** 테스트·디버깅용 - 평가 순서 확인. */
    public List<String> ruleIdsInEvaluationOrder() {
        return sortedRules.stream().map(PolicyRule::id).toList();
    }
}
