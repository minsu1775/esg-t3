/**
 * Evidence 모듈 — 증빙 파일 + SHA-256 + Object Storage.
 *
 * <p>활동데이터·스냅샷·보고서가 N:M으로 참조하는 독립 Aggregate.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "Evidence (증빙 파일)",
    allowedDependencies = { "shared", "iam", "audit" }
)
package ai.claudecode.esgt3.evidence;
