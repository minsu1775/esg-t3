package ai.claudecode.esgt3.shared.tenant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * 매 요청마다 ThreadLocal {@link TenantContext}에서 tenantId를 읽어
 * PostgreSQL 세션 변수에 set_config로 주입한다.
 *
 * <p><strong>esg-t2 L-P1-03 — 파라미터 바인딩 필수.</strong>
 * <p><strong>L-P1-07 (Phase 1 발견) — set_config는 함수라 queryForObject 사용.</strong>
 * <pre>
 * SELECT set_config('app.current_tenant_id', ?, true)  -- 결과 행 반환
 * </pre>
 * 문자열 연결은 SQL Injection.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TenantContextInterceptor implements HandlerInterceptor {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        UUID tenantId = TenantContext.currentTenantId();
        UUID userId = TenantContext.currentUserId();
        if (tenantId != null) {
            jdbcTemplate.queryForObject(
                "SELECT set_config('app.current_tenant_id', ?, true)",
                String.class,
                tenantId.toString()
            );
            MDC.put("tenantId", tenantId.toString());
        }
        if (userId != null) {
            MDC.put("userId", userId.toString());
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
        MDC.remove("tenantId");
        MDC.remove("userId");
    }
}
