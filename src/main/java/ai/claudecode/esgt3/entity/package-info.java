/**
 * Entity 모듈 — 법인(LegalEntity) 관리 + 계층·연결 경계.
 *
 * <p>도메인 클래스는 {@code LegalEntity*} 명명 (JPA {@code @Entity}와 구분).
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Entity (LegalEntity)",
    allowedDependencies = { "shared", "iam" }
)
package ai.claudecode.esgt3.entity;
