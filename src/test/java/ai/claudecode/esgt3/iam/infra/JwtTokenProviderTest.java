package ai.claudecode.esgt3.iam.infra;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenProviderTest {

    private static final String SECRET =
        "test-only-secret-256bit-padding-for-hmac-sha256-test-key-padding-padding";

    @Test
    void access_token_생성_후_파싱_시_subject_복원() {
        var provider = new JwtTokenProvider(SECRET, "esg-t3-test",
            Duration.ofMinutes(15), Duration.ofDays(7));
        UUID userId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID entity = UUID.randomUUID();

        String token = provider.createAccessToken(userId, tenantId, "ESG_MANAGER", Set.of(entity));
        JwtAuthentication auth = provider.parseAccessToken(token);

        assertThat(auth.getUserId()).isEqualTo(userId);
        assertThat(auth.getTenantId()).isEqualTo(tenantId);
        assertThat(auth.getRole()).isEqualTo("ESG_MANAGER");
        assertThat(auth.getAssignedEntityIds()).containsExactly(entity);
    }

    @Test
    void 비밀키_불일치_시_JwtException() {
        var producer = new JwtTokenProvider(SECRET, "esg-t3-test",
            Duration.ofMinutes(15), Duration.ofDays(7));
        var consumer = new JwtTokenProvider(SECRET.replace("test", "TEST"),
            "esg-t3-test", Duration.ofMinutes(15), Duration.ofDays(7));
        String token = producer.createAccessToken(UUID.randomUUID(), UUID.randomUUID(),
            "ESG_MANAGER", Set.of());
        assertThatThrownBy(() -> consumer.parseAccessToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void TTL_경과_토큰은_ExpiredJwtException() throws Exception {
        var provider = new JwtTokenProvider(SECRET, "esg-t3-test",
            Duration.ofMillis(1), Duration.ofDays(7));
        String token = provider.createAccessToken(UUID.randomUUID(), UUID.randomUUID(),
            "ESG_VIEWER", Set.of());
        Thread.sleep(20);
        assertThatThrownBy(() -> provider.parseAccessToken(token))
            .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void secret이_빈_문자열이면_생성자에서_예외() {
        assertThatThrownBy(() -> new JwtTokenProvider("", "iss",
            Duration.ofMinutes(15), Duration.ofDays(7)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secret이_32B_미만이면_예외() {
        assertThatThrownBy(() -> new JwtTokenProvider("short", "iss",
            Duration.ofMinutes(15), Duration.ofDays(7)))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void refresh_token은_jti_고유() {
        var provider = new JwtTokenProvider(SECRET, "esg-t3-test",
            Duration.ofMinutes(15), Duration.ofDays(7));
        UUID userId = UUID.randomUUID();
        String t1 = provider.createRefreshToken(userId);
        String t2 = provider.createRefreshToken(userId);
        assertThat(provider.extractJti(t1)).isNotEqualTo(provider.extractJti(t2));
    }

    @Test
    void parseRefreshToken_정상_파싱_시_userId_반환() {
        var provider = new JwtTokenProvider(SECRET, "esg-t3-test",
            Duration.ofMinutes(15), Duration.ofDays(7));
        UUID userId = UUID.randomUUID();
        String token = provider.createRefreshToken(userId);
        assertThat(provider.parseRefreshToken(token)).isEqualTo(userId);
    }

    @Test
    void parseRefreshToken에_access_token_전달_시_예외() {
        var provider = new JwtTokenProvider(SECRET, "esg-t3-test",
            Duration.ofMinutes(15), Duration.ofDays(7));
        String access = provider.createAccessToken(UUID.randomUUID(), UUID.randomUUID(),
            "ESG_MANAGER", Set.of());
        assertThatThrownBy(() -> provider.parseRefreshToken(access))
            .isInstanceOf(JwtException.class)
            .hasMessageContaining("refresh");
    }
}
