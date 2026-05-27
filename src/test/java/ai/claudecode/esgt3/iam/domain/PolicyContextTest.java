package ai.claudecode.esgt3.iam.domain;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyContextTest {

    @Test
    void Subject_생성_시_tenantId가_null이면_예외() {
        assertThatThrownBy(() -> new Subject(UUID.randomUUID(), "ESG_MANAGER", null, Set.of(), null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("tenantId");
    }

    @Test
    void Resource_생성_시_type이_빈문자열이면_예외() {
        assertThatThrownBy(() -> Resource.of("", UUID.randomUUID(), null))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void PolicyContext가_네_요소를_모두_묶는다() {
        var subject = new Subject(UUID.randomUUID(), "ESG_MANAGER",
            UUID.fromString("00000000-0000-0000-0000-000000000001"), Set.of(), null);
        var resource = Resource.of("ActivityData",
            UUID.fromString("00000000-0000-0000-0000-000000000001"), null);
        var ctx = PolicyContext.of(subject, resource, PolicyAction.WRITE);

        assertThat(ctx.subject()).isSameAs(subject);
        assertThat(ctx.resource()).isSameAs(resource);
        assertThat(ctx.action()).isEqualTo(PolicyAction.WRITE);
        assertThat(ctx.environment()).isNotNull();
    }

    @Test
    void Resource_attributes를_immutable로_노출한다() {
        var resource = Resource.of("ActivityData", UUID.randomUUID(),
            Map.of("approvalState", "DRAFT", "createdBy", "user-1"));
        assertThatThrownBy(() -> resource.attributes().put("k", "v"))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void Subject_assignedEntityIds도_immutable로_노출한다() {
        var entities = new java.util.HashSet<UUID>();
        entities.add(UUID.randomUUID());
        var subject = new Subject(UUID.randomUUID(), "ESG_MANAGER",
            UUID.randomUUID(), entities, null);
        assertThatThrownBy(() -> subject.assignedEntityIds().add(UUID.randomUUID()))
            .isInstanceOf(UnsupportedOperationException.class);
    }
}
