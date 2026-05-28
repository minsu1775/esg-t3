package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.shared.tenant.TenantContext;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Bearer 토큰 추출 → JwtAuthentication → SecurityContext + TenantContext 설정.
 *
 * <p>실패 시 SecurityContext 미설정 — 다음 필터·컨트롤러에서 401/403 발생.
 * 토큰이 없으면 통과(다음 필터로 위임).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final RedisTokenBlacklist blacklist;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String token = header.substring(BEARER_PREFIX.length()).trim();
            try {
                if (blacklist.isAccessTokenBlacklisted(token)) {
                    log.warn("블랙리스트된 access token 거부");
                } else {
                    JwtAuthentication auth = tokenProvider.parseAccessToken(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    TenantContext.set(auth.getTenantId(), auth.getUserId());
                }
            } catch (JwtException e) {
                log.debug("JWT 검증 실패: {}", e.getMessage());
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            // TenantContextInterceptor.afterCompletion이 TenantContext clear 책임 — 여기서는 SecurityContext만
            SecurityContextHolder.clearContext();
        }
    }
}
