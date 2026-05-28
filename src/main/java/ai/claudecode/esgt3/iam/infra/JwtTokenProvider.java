package ai.claudecode.esgt3.iam.infra;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JWT 발급·파싱 - HMAC-SHA256 자체 서명.
 *
 * <p>esg-t2 L-P1-04: SecretKey는 빈 생성 시 1회만 초기화하여 캐싱.
 * 매 호출 시 키 객체 새로 만들지 않음 (성능).
 *
 * <p>M+1 Keycloak 전환: {@code NimbusJwtDecoder.withJwkSetUri(url)} 빈으로 교체하면
 * 컨트롤러·필터 변경 없이 OIDC로 이전 가능.
 *
 * <p>L-P1-05: secret 미설정 또는 32B 미만이면 시작 시 IllegalStateException → 부팅 차단.
 */
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final String issuer;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public JwtTokenProvider(
        @Value("${esg.jwt.secret}") String secret,
        @Value("${esg.jwt.issuer}") String issuer,
        @Value("#{T(java.time.Duration).ofMinutes(${esg.jwt.access-token-ttl-minutes})}") Duration accessTtl,
        @Value("#{T(java.time.Duration).ofDays(${esg.jwt.refresh-token-ttl-days})}") Duration refreshTtl
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                "esg.jwt.secret이 비어 있습니다 - 운영 환경에서는 ESG_JWT_SECRET 환경변수 필수");
        }
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException(
                "esg.jwt.secret은 HMAC-SHA256용 32바이트 이상 필요 - 현재 " + bytes.length + "B");
        }
        this.signingKey = Keys.hmacShaKeyFor(bytes);
        this.issuer = issuer;
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    public String createAccessToken(UUID userId, UUID tenantId, String role, Set<UUID> entityIds) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(issuer)
            .subject(userId.toString())
            .claim("tenantId", tenantId.toString())
            .claim("role", role)
            .claim("entityIds",
                entityIds == null ? List.of() : entityIds.stream().map(UUID::toString).toList())
            .claim("typ", "access")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(accessTtl)))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    public String createRefreshToken(UUID userId) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(issuer)
            .subject(userId.toString())
            .id(UUID.randomUUID().toString())
            .claim("typ", "refresh")
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plus(refreshTtl)))
            .signWith(signingKey, Jwts.SIG.HS256)
            .compact();
    }

    public JwtAuthentication parseAccessToken(String token) {
        Claims claims = parse(token);
        if (!"access".equals(claims.get("typ"))) {
            throw new JwtException("access 타입 토큰 아님");
        }
        UUID userId = UUID.fromString(claims.getSubject());
        UUID tenantId = UUID.fromString((String) claims.get("tenantId"));
        String role = (String) claims.get("role");
        @SuppressWarnings("unchecked")
        List<String> entityIdStrings = (List<String>) claims.getOrDefault("entityIds", List.of());
        Set<UUID> entityIds = new HashSet<>();
        for (String s : entityIdStrings) entityIds.add(UUID.fromString(s));
        return new JwtAuthentication(userId, tenantId, role, entityIds);
    }

    /**
     * Refresh token 검증 + subject userId 반환.
     * <p>{@code typ=refresh} 명시 검증 + 만료·서명 자동 검증.
     */
    public UUID parseRefreshToken(String token) {
        Claims claims = parse(token);
        if (!"refresh".equals(claims.get("typ"))) {
            throw new JwtException("refresh 타입 토큰 아님");
        }
        return UUID.fromString(claims.getSubject());
    }

    public String extractJti(String refreshToken) {
        return parse(refreshToken).getId();
    }

    public Duration getAccessTtl() { return accessTtl; }
    public Duration getRefreshTtl() { return refreshTtl; }

    private Claims parse(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .requireIssuer(issuer)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
