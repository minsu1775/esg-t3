package ai.claudecode.esgt3.iam.api;

import ai.claudecode.esgt3.iam.domain.PolicyAction;
import ai.claudecode.esgt3.iam.domain.PolicyContext;
import ai.claudecode.esgt3.iam.domain.PolicyDecision;
import ai.claudecode.esgt3.iam.domain.PolicyRegistry;
import ai.claudecode.esgt3.iam.domain.Resource;
import ai.claudecode.esgt3.iam.domain.Subject;
import ai.claudecode.esgt3.iam.infra.PolicyDecisionLogger;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ABAC 정책 평가 단일 진입점.
 *
 * <p>{@code @PreAuthorize("@policy.allow(authentication, 'WRITE', #resource)")}에서 호출.
 * 빈 이름이 {@code policy}가 되도록 클래스명 {@code PolicyFacade} → 빈 이름 명시.
 */
@Component("policy")
@RequiredArgsConstructor
public class PolicyFacade {

    private final PolicyRegistry registry;
    private final PolicyDecisionLogger logger;
    private final OpenTelemetry openTelemetry;

    private Tracer tracer() {
        return openTelemetry.getTracer("ai.claudecode.esgt3.iam");
    }

    /**
     * 평가 + 기록 + OTel Span. 반환 true → 호출 허용, false → AccessDeniedException 발생 유도.
     */
    public boolean allow(Authentication authentication, String action, Object resource) {
        Span span = tracer().spanBuilder("iam.evaluate_policy").startSpan();
        try (var scope = span.makeCurrent()) {
            PolicyContext ctx = buildContext(authentication, action, resource);
            PolicyDecision decision = registry.evaluator().evaluate(ctx);

            span.setAttribute("tenant.id",
                ctx.subject().tenantId() != null ? ctx.subject().tenantId().toString() : "");
            span.setAttribute("subject.user_id",
                ctx.subject().userId() != null ? ctx.subject().userId().toString() : "");
            span.setAttribute("subject.role", ctx.subject().role());
            span.setAttribute("action", action);
            span.setAttribute("resource.type", ctx.resource().type());
            span.setAttribute("effect", decision.effect().name());
            if (decision.policyId() != null) {
                span.setAttribute("policy.id", decision.policyId());
            }

            logger.log(ctx, decision);
            return decision.isAllowed();
        } finally {
            span.end();
        }
    }

    private PolicyContext buildContext(Authentication auth, String action, Object resource) {
        Subject subject = SubjectMapper.fromAuthentication(auth);
        Resource res = resource instanceof Resource r
            ? r
            : Resource.of("Unknown", subject.tenantId(), Map.of());
        return PolicyContext.of(subject, res, PolicyAction.valueOf(action));
    }
}
