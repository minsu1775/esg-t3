package ai.claudecode.esgt3.iam.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluatorTest {

    @Test
    void 매칭_정책이_없으면_NOT_APPLICABLE_반환() {
        var evaluator = new PolicyEvaluator(List.of(
            new PolicyDocument("empty.yaml", List.of())
        ));
        var ctx = sampleCtx("ESG_MANAGER", PolicyAction.WRITE);
        assertThat(evaluator.evaluate(ctx).effect()).isEqualTo(PolicyEffect.NOT_APPLICABLE);
    }

    @Test
    void 단일_PERMIT_정책_매칭_시_PERMIT_반환() {
        var rule = new PolicyRule("r1", "ESG_MANAGER READ 허용", PolicyEffect.PERMIT, 0,
            Map.of("subject.role", "ESG_MANAGER", "action", "READ"),
            List.of()
        );
        var evaluator = new PolicyEvaluator(List.of(new PolicyDocument("t.yaml", List.of(rule))));
        var d = evaluator.evaluate(sampleCtx("ESG_MANAGER", PolicyAction.READ));
        assertThat(d.effect()).isEqualTo(PolicyEffect.PERMIT);
        assertThat(d.policyId()).isEqualTo("r1");
    }

    @Test
    void 매칭_안되는_PERMIT은_NOT_APPLICABLE() {
        var rule = new PolicyRule("r1", "VIEWER만", PolicyEffect.PERMIT, 0,
            Map.of("subject.role", "ESG_VIEWER"), List.of()
        );
        var evaluator = new PolicyEvaluator(List.of(new PolicyDocument("t.yaml", List.of(rule))));
        assertThat(evaluator.evaluate(sampleCtx("ESG_MANAGER", PolicyAction.READ)).effect())
            .isEqualTo(PolicyEffect.NOT_APPLICABLE);
    }

    private static PolicyContext sampleCtx(String role, PolicyAction action) {
        return PolicyContext.of(
            new Subject(UUID.randomUUID(), role, UUID.randomUUID(), Set.of(), null),
            Resource.of("ActivityData", UUID.randomUUID(), Map.of()),
            action
        );
    }
}
