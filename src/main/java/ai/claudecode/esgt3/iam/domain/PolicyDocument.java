package ai.claudecode.esgt3.iam.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 단일 YAML 파일 - 여러 PolicyRule 묶음.
 *
 * <p>{@link #sortedRules()}는 priority 내림차순 정렬된 불변 리스트를 반환한다.
 * PolicyEvaluator는 정렬된 순서로 첫 매칭 시 즉시 반환(DENY 우선).
 */
public record PolicyDocument(String source, List<PolicyRule> rules) {

    public PolicyDocument {
        Objects.requireNonNull(source, "source");
        rules = rules == null ? List.of() : List.copyOf(rules);
    }

    public List<PolicyRule> sortedRules() {
        return rules.stream()
            .sorted(Comparator.comparingInt(PolicyRule::priority).reversed())
            .toList();
    }
}
