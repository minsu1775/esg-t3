/**
 * Audit 모듈 — AuditLog + Hash Chain + Outbox (@Auditable AOP).
 *
 * <p>모든 도메인 모듈이 의존 가능한 횡단 모듈.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Audit (AuditLog + Hash Chain)",
    allowedDependencies = { "shared" }
)
package ai.claudecode.esgt3.audit;
