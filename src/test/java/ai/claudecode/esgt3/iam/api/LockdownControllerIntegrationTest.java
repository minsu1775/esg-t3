package ai.claudecode.esgt3.iam.api;

import ai.claudecode.esgt3.AbstractIntegrationTest;
import ai.claudecode.esgt3.iam.infra.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Set;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class LockdownControllerIntegrationTest extends AbstractIntegrationTest {

    static final GenericContainer<?> REDIS =
        new GenericContainer<>(DockerImageName.parse("redis:7"))
            .withExposedPorts(6379);

    static {
        REDIS.start();
    }

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @Autowired WebApplicationContext context;
    @Autowired JwtTokenProvider tokenProvider;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void SUPER_ADMIN이_lockdown_활성화_시_200() throws Exception {
        String token = tokenProvider.createAccessToken(UUID.randomUUID(), UUID.randomUUID(),
            "SUPER_ADMIN", Set.of());
        mvc.perform(post("/api/v1/admin/lockdown/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"reason\":\"Hash chain mismatch detected\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void ESG_MANAGER가_lockdown_시도_시_403() throws Exception {
        String token = tokenProvider.createAccessToken(UUID.randomUUID(), UUID.randomUUID(),
            "ESG_MANAGER", Set.of());
        mvc.perform(post("/api/v1/admin/lockdown/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"reason\":\"unauthorized attempt\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void Bearer_없으면_접근_차단() throws Exception {
        // Spring Security 7 기본: 익명 사용자 → @PreAuthorize 평가 시 403 (anonymous는 인증 객체 존재)
        mvc.perform(post("/api/v1/admin/lockdown/" + UUID.randomUUID())
                .contentType("application/json")
                .content("{\"reason\":\"no auth\"}"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void SUPER_ADMIN_lockdown_해제_시_200() throws Exception {
        String token = tokenProvider.createAccessToken(UUID.randomUUID(), UUID.randomUUID(),
            "SUPER_ADMIN", Set.of());
        UUID tenantId = UUID.randomUUID();
        mvc.perform(post("/api/v1/admin/lockdown/" + tenantId)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"reason\":\"activate test\"}"));
        mvc.perform(delete("/api/v1/admin/lockdown/" + tenantId)
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .content("{\"reason\":\"deactivate after runbook complete\"}"))
            .andExpect(status().isOk());
    }
}
