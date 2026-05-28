package ai.claudecode.esgt3.iam.api.controller;

import ai.claudecode.esgt3.iam.api.LoginRequest;
import ai.claudecode.esgt3.iam.api.LoginResponse;
import ai.claudecode.esgt3.iam.api.RefreshRequest;
import ai.claudecode.esgt3.iam.infra.InvalidCredentialsException;
import ai.claudecode.esgt3.iam.infra.JwtTokenProvider;
import ai.claudecode.esgt3.iam.infra.RedisTokenBlacklist;
import ai.claudecode.esgt3.iam.infra.UserLookupService;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserLookupService userLookup;
    private final JwtTokenProvider tokenProvider;
    private final RedisTokenBlacklist blacklist;
    // BCrypt는 stateless - Task 18에서 IamSecurityConfig가 빈 등록 시 의존 주입으로 교체.
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PreAuthorize("permitAll()")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        var user = userLookup.findByEmail(req.email())
            .orElseThrow(() -> new InvalidCredentialsException(
                "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다"));
        if (!passwordEncoder.matches(req.password(), user.passwordHash())) {
            throw new InvalidCredentialsException(
                "INVALID_CREDENTIALS", "이메일 또는 비밀번호가 올바르지 않습니다");
        }
        return ResponseEntity.ok(issueTokens(user.userId(), user.tenantId(), user.role(), user.entityIds()));
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        UUID userId;
        try {
            userId = tokenProvider.parseRefreshToken(req.refreshToken());
        } catch (JwtException e) {
            throw new InvalidCredentialsException("INVALID_TOKEN", "refresh token 검증 실패", e);
        }
        String oldJti = tokenProvider.extractJti(req.refreshToken());
        if (blacklist.isRefreshJtiBlacklisted(oldJti)) {
            throw new InvalidCredentialsException(
                "REFRESH_TOKEN_REVOKED", "취소된 refresh token입니다");
        }
        var user = userLookup.findById(userId)
            .orElseThrow(() -> new InvalidCredentialsException(
                "INVALID_CREDENTIALS", "사용자를 찾을 수 없습니다"));

        // 기존 refresh를 블랙리스트 → 신규 발급 (rotating refresh)
        blacklist.blacklistRefreshJti(oldJti, tokenProvider.getRefreshTtl());
        return ResponseEntity.ok(issueTokens(user.userId(), user.tenantId(), user.role(), user.entityIds()));
    }

    @PreAuthorize("permitAll()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshRequest req) {
        try {
            String jti = tokenProvider.extractJti(req.refreshToken());
            blacklist.blacklistRefreshJti(jti, tokenProvider.getRefreshTtl());
        } catch (JwtException ignore) {
            // 잘못된 토큰이라도 로그아웃은 멱등 처리 (idempotent)
        }
        return ResponseEntity.noContent().build();
    }

    private LoginResponse issueTokens(UUID userId, UUID tenantId, String role,
                                       java.util.Set<UUID> entityIds) {
        String access = tokenProvider.createAccessToken(userId, tenantId, role, entityIds);
        String refresh = tokenProvider.createRefreshToken(userId);
        return new LoginResponse(access, refresh, tokenProvider.getAccessTtl().toSeconds());
    }
}
