package ai.claudecode.esgt3.shared.tenant;

import ai.claudecode.esgt3.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TenantContextInterceptorIntegrationTest extends AbstractIntegrationTest {

    @Autowired JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanup() { TenantContext.clear(); }

    @Test
    @Transactional
    void set_config_파라미터_바인딩으로_RLS가_적용된다() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        // 사전: 두 테넌트 + 각 테넌트에 사용자 1명씩 (superuser 연결로 RLS BYPASS)
        jdbcTemplate.update("INSERT INTO tenants (id, name, country_code) VALUES (?, ?, ?)",
            tenantA, "Tenant A", "KR");
        jdbcTemplate.update("INSERT INTO tenants (id, name, country_code) VALUES (?, ?, ?)",
            tenantB, "Tenant B", "KR");
        jdbcTemplate.update("INSERT INTO users (tenant_id, email, display_name, password_hash) VALUES (?, ?, ?, ?)",
            tenantA, "a@test.com", "A user", "hash");
        jdbcTemplate.update("INSERT INTO users (tenant_id, email, display_name, password_hash) VALUES (?, ?, ?, ?)",
            tenantB, "b@test.com", "B user", "hash");

        // tenantA로 컨텍스트 설정 + set_config (L-P1-07: queryForObject)
        jdbcTemplate.queryForObject("SELECT set_config('app.current_tenant_id', ?, true)",
            String.class, tenantA.toString());

        // superuser 연결이므로 RLS BYPASS — 검증을 위해 SET ROLE app_role
        jdbcTemplate.execute("SET ROLE app_role");
        try {
            Integer countWhenA = jdbcTemplate.queryForObject("SELECT count(*) FROM users", Integer.class);
            assertThat(countWhenA).isEqualTo(1);

            // tenantB로 전환
            jdbcTemplate.queryForObject("SELECT set_config('app.current_tenant_id', ?, true)",
                String.class, tenantB.toString());
            Integer countWhenB = jdbcTemplate.queryForObject("SELECT count(*) FROM users", Integer.class);
            assertThat(countWhenB).isEqualTo(1);
        } finally {
            jdbcTemplate.execute("RESET ROLE");
        }
    }

    @Test
    @Transactional
    void set_config_미설정_시_RLS가_모든_행_차단() {
        UUID tenant = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO tenants (id, name, country_code) VALUES (?, ?, ?)",
            tenant, "Tenant", "KR");
        jdbcTemplate.update("INSERT INTO users (tenant_id, email, display_name, password_hash) VALUES (?, ?, ?, ?)",
            tenant, "x@test.com", "X", "hash");

        jdbcTemplate.execute("SET ROLE app_role");
        try {
            // app.current_tenant_id 미설정 → NULL 비교 → 모든 행 차단
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM users", Integer.class);
            assertThat(count).isEqualTo(0);
        } finally {
            jdbcTemplate.execute("RESET ROLE");
        }
    }
}
