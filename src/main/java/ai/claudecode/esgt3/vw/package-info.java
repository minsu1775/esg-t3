/**
 * VW 모듈 — Verification Workspace (외부 검증인 Snapshot/Comment).
 *
 * <p>VERIFIER 역할은 지정 Snapshot 외 접근 불가 (RLS + ABAC 이중).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "VW (Verification Workspace)",
    allowedDependencies = { "shared", "iam", "entity", "ghg", "evidence", "rpt", "audit" }
)
package ai.claudecode.esgt3.vw;
