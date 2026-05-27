package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.AbstractIntegrationTest;
import ai.claudecode.esgt3.iam.domain.PolicyAction;
import ai.claudecode.esgt3.iam.domain.PolicyContext;
import ai.claudecode.esgt3.iam.domain.PolicyDecision;
import ai.claudecode.esgt3.iam.domain.Resource;
import ai.claudecode.esgt3.iam.domain.Subject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PolicyDecisionLoggerIntegrationTest extends AbstractIntegrationTest {

    @Autowired PolicyDecisionLogger logger;
    @Autowired PolicyDecisionJpaRepository repository;
    @Autowired JdbcTemplate jdbcTemplate;

    @Test
    void PERMIT_평가_결과를_INSERT한다() {
        UUID tenant = UUID.randomUUID();
        jdbcTemplate.queryForObject("SELECT set_config('app.current_tenant_id', ?, true)",
            String.class, tenant.toString());

        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER", tenant, Set.of(), null),
            Resource.of("ActivityData", tenant, Map.of()),
            PolicyAction.READ
        );
        var decision = PolicyDecision.permit("test-rule", "테스트 PERMIT");

        long before = repository.countByTenantId(tenant);
        logger.log(ctx, decision);
        long after = repository.countByTenantId(tenant);

        assertThat(after).isEqualTo(before + 1);
        var recent = repository.findTop100ByTenantIdOrderByOccurredAtDesc(tenant);
        assertThat(recent.get(0).getEffect()).isEqualTo("PERMIT");
        assertThat(recent.get(0).getPolicyId()).isEqualTo("test-rule");
    }

    @Test
    void DENY_평가_결과도_저장된다() {
        UUID tenant = UUID.randomUUID();
        jdbcTemplate.queryForObject("SELECT set_config('app.current_tenant_id', ?, true)",
            String.class, tenant.toString());

        var ctx = PolicyContext.of(
            new Subject(UUID.randomUUID(), "ESG_MANAGER", tenant, Set.of(), null),
            Resource.of("ActivityData", tenant, Map.of()),
            PolicyAction.WRITE
        );
        logger.log(ctx, PolicyDecision.deny("disclosed-data-immutability", "공시 완료"));

        var recent = repository.findTop100ByTenantIdOrderByOccurredAtDesc(tenant);
        assertThat(recent.get(0).getEffect()).isEqualTo("DENY");
    }
}
