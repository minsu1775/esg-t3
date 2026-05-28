/**
 * IAM 모듈 — Identity & Access Management (ABAC 정책 엔진 포함).
 *
 * <p>Tenant, User, Role, ABAC 정책 평가를 담당한다.
 * 다른 도메인 모듈은 이 모듈의 {@code api} 패키지만 import 가능하다.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "IAM (Identity & Access)",
    allowedDependencies = { "shared", "shared::exception", "shared::event", "shared::web", "shared::tenant" }
)
package ai.claudecode.esgt3.iam;
