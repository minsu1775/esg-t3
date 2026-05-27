package ai.claudecode.esgt3.iam.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluatorPriorityTest {

    @Test
    void priority_200_DENY가_priority_0_PERMIT보다_우선() {
        var permit = new PolicyRule(
            "esg-manager-write", "ESG_MANAGER WRITE",
            PolicyEffect.PERMIT, 0,
            Map.of("subject.role", "ESG_MANAGER", "action", "WRITE"),
            List.of()
        );
        var deny = new PolicyRule(
            "disclosed-data-immutability", "공시 완료 불변",
            PolicyEffect.DENY, 200,
            Map.of("action", "WRITE", "resource.approvalState", "DISCLOSED"),
            List.of()
        );
        var evaluator = new PolicyEvaluator(List.of(
            new PolicyDocument("perm.yaml", List.of(permit)),
            new PolicyDocument("immut.yaml", List.of(deny))
        ));
        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER", UUID.randomUUID(), Set.of(), null),
            Resource.of("ActivityData", UUID.randomUUID(), Map.of("approvalState", "DISCLOSED")),
            PolicyAction.WRITE
        );
        var d = evaluator.evaluate(ctx);
        assertThat(d.effect()).isEqualTo(PolicyEffect.DENY);
        assertThat(d.policyId()).isEqualTo("disclosed-data-immutability");
    }

    @Test
    void priority_250_emergency_lockdown이_최우선() {
        var lockdown = new PolicyRule(
            "emergency-lockdown", "비상 격리",
            PolicyEffect.DENY, 250,
            Map.of("action", List.of("WRITE", "APPROVE")),
            List.of()
        );
        var permit = new PolicyRule(
            "super-admin-all", "SUPER_ADMIN 전권",
            PolicyEffect.PERMIT, 100,
            Map.of("subject.role", "SUPER_ADMIN"),
            List.of()
        );
        var evaluator = new PolicyEvaluator(List.of(
            new PolicyDocument("a.yaml", List.of(lockdown)),
            new PolicyDocument("b.yaml", List.of(permit))
        ));
        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "SUPER_ADMIN", UUID.randomUUID(), Set.of(), null),
            Resource.of("ActivityData", UUID.randomUUID(), Map.of()),
            PolicyAction.WRITE
        );
        assertThat(evaluator.evaluate(ctx).policyId()).isEqualTo("emergency-lockdown");
    }
}
