package ai.claudecode.esgt3.iam.api;

import ai.claudecode.esgt3.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

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
    @Autowired JdbcTemplate jdbcTemplate;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    @Test
    void 정상_자격증명으로_로그인_성공_시_200_및_토큰_반환() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String hash = new BCryptPasswordEncoder().encode("pw-1234!");
        jdbcTemplate.update("INSERT INTO tenants (id, name, country_code) VALUES (?, ?, ?)",
            tenantId, "T", "KR");
        jdbcTemplate.update(
            "INSERT INTO users (id, tenant_id, email, display_name, password_hash) VALUES (?, ?, ?, ?, ?)",
            userId, tenantId, "u1@test.com", "U1", hash);
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, ?)",
            userId, "ESG_MANAGER");

        mvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"u1@test.com\",\"password\":\"pw-1234!\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists())
            .andExpect(jsonPath("$.accessTtlSeconds").value(900));
    }

    @Test
    void 잘못된_비밀번호는_4xx() throws Exception {
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String hash = new BCryptPasswordEncoder().encode("pw-1234!");
        jdbcTemplate.update("INSERT INTO tenants (id, name, country_code) VALUES (?, ?, ?)",
            tenantId, "T", "KR");
        jdbcTemplate.update(
            "INSERT INTO users (id, tenant_id, email, display_name, password_hash) VALUES (?, ?, ?, ?, ?)",
            userId, tenantId, "u2@test.com", "U2", hash);
        jdbcTemplate.update("INSERT INTO user_roles (user_id, role) VALUES (?, ?)",
            userId, "ESG_MANAGER");

        mvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"u2@test.com\",\"password\":\"wrong\"}"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void 미존재_사용자_로그인은_4xx() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                .contentType("application/json")
                .content("{\"email\":\"nonexistent@test.com\",\"password\":\"any\"}"))
            .andExpect(status().is4xxClientError());
    }
}
