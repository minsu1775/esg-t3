package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class RedisTokenBlacklistIntegrationTest extends AbstractIntegrationTest {

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

    @Autowired RedisTokenBlacklist blacklist;

    @Test
    void refresh_token_jti_등록_후_차단() {
        String jti = "test-jti-123";
        assertThat(blacklist.isRefreshJtiBlacklisted(jti)).isFalse();
        blacklist.blacklistRefreshJti(jti, Duration.ofMinutes(1));
        assertThat(blacklist.isRefreshJtiBlacklisted(jti)).isTrue();
    }

    @Test
    void access_token_원문_등록_후_차단() {
        String token = "eyJhbGc-test-access-token";
        assertThat(blacklist.isAccessTokenBlacklisted(token)).isFalse();
        blacklist.blacklistAccessToken(token, Duration.ofMinutes(15));
        assertThat(blacklist.isAccessTokenBlacklisted(token)).isTrue();
    }
}
