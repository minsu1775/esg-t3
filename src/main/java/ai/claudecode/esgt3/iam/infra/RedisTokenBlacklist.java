package ai.claudecode.esgt3.iam.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

/**
 * JWT 블랙리스트 - Refresh JTI(로그아웃 시) + Access Token(분실·악용 시).
 *
 * <p>TTL 기반 자동 만료 (Redis SET EX). Access Token은 SHA-256 hex로 키 축약하여
 * 토큰 원문 길이가 키에 노출되지 않도록 한다.
 */
@Component
@RequiredArgsConstructor
public class RedisTokenBlacklist {

    private final StringRedisTemplate redis;

    @Value("${esg.redis.blacklist-prefix}")
    private String prefix;

    public void blacklistRefreshJti(String jti, Duration ttl) {
        redis.opsForValue().set(refreshKey(jti), "1", ttl);
    }

    public boolean isRefreshJtiBlacklisted(String jti) {
        return Boolean.TRUE.equals(redis.hasKey(refreshKey(jti)));
    }

    public void blacklistAccessToken(String token, Duration ttl) {
        redis.opsForValue().set(accessKey(token), "1", ttl);
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redis.hasKey(accessKey(token)));
    }

    private String refreshKey(String jti) {
        return prefix + "refresh:" + jti;
    }

    private String accessKey(String token) {
        return prefix + "access:" + sha256Hex(token);
    }

    private static String sha256Hex(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
