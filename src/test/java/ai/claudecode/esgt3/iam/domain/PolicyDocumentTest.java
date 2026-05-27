package ai.claudecode.esgt3.iam.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDocumentTest {

    @Test
    void Rule_when절_단순_평등_매칭() {
        var rule = new PolicyRule(
            "test-rule", "테스트", PolicyEffect.PERMIT, 0,
            Map.of(
                "subject.role", "ESG_MANAGER",
                "action", List.of("READ", "WRITE"),
                "resource.type", "ActivityData"
            ),
            List.of()
        );
        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER",
                UUID.fromString("00000000-0000-0000-0000-000000000001"), Set.of(), null),
            Resource.of("ActivityData",
                UUID.fromString("00000000-0000-0000-0000-000000000001"), Map.of()),
            PolicyAction.WRITE
        );
        assertThat(WhenClauseMatcher.matches(rule.when(), ctx)).isTrue();
    }

    @Test
    void Rule_when절_action_불일치_시_false() {
        var rule = new PolicyRule("r", "d", PolicyEffect.PERMIT, 0,
            Map.of("action", "READ"), List.of());
        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER",
                UUID.randomUUID(), Set.of(), null),
            Resource.of("ActivityData", UUID.randomUUID(), Map.of()),
            PolicyAction.WRITE
        );
        assertThat(WhenClauseMatcher.matches(rule.when(), ctx)).isFalse();
    }

    @Test
    void Rule_변수_치환_subject_tenantId가_resource_tenantId와_일치() {
        var rule = new PolicyRule("r", "d", PolicyEffect.PERMIT, 0,
            Map.of(
                "subject.role", "ESG_MANAGER",
                "subject.tenantId", "${resource.tenantId}"
            ),
            List.of()
        );
        var tenant = UUID.randomUUID();
        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER", tenant, Set.of(), null),
            Resource.of("ActivityData", tenant, Map.of()),
            PolicyAction.READ
        );
        assertThat(WhenClauseMatcher.matches(rule.when(), ctx)).isTrue();
    }

    @Test
    void Rule_contains_매처_assignedEntityIds() {
        var entity = UUID.randomUUID();
        var rule = new PolicyRule("r", "d", PolicyEffect.PERMIT, 0,
            Map.of(
                "subject.assignedEntityIds",
                Map.of("contains", "${resource.entityId}")
            ),
            List.of()
        );
        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER", UUID.randomUUID(),
                Set.of(entity), null),
            Resource.of("ActivityData", UUID.randomUUID(), Map.of("entityId", entity.toString())),
            PolicyAction.READ
        );
        assertThat(WhenClauseMatcher.matches(rule.when(), ctx)).isTrue();
    }

    @Test
    void Rule_in_매처_approvalState() {
        var rule = new PolicyRule("r", "d", PolicyEffect.PERMIT, 0,
            Map.of("resource.approvalState", List.of("DRAFT", "REJECTED")),
            List.of()
        );
        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER", UUID.randomUUID(), Set.of(), null),
            Resource.of("ActivityData", UUID.randomUUID(),
                Map.of("approvalState", "DRAFT")),
            PolicyAction.WRITE
        );
        assertThat(WhenClauseMatcher.matches(rule.when(), ctx)).isTrue();
    }

    @Test
    void Document_priority_내림차순으로_정렬한다() {
        var r1 = new PolicyRule("low", "d", PolicyEffect.PERMIT, 0, Map.of(), List.of());
        var r2 = new PolicyRule("emergency", "d", PolicyEffect.DENY, 250, Map.of(), List.of());
        var r3 = new PolicyRule("disclosed", "d", PolicyEffect.DENY, 200, Map.of(), List.of());
        var doc = new PolicyDocument("test.yaml", List.of(r1, r2, r3));
        assertThat(doc.sortedRules())
            .extracting(PolicyRule::id)
            .containsExactly("emergency", "disclosed", "low");
    }
}
