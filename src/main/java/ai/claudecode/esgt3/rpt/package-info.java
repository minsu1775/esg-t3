/**
 * RPT 모듈 — 공시 보고서 생성 (KSSB 2 PDF + narrative 슬롯).
 *
 * <p>narrative 본문은 MVP는 수동 입력, M+1에서 LLM 자동 생성.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "RPT (Disclosure Report)",
    allowedDependencies = { "shared", "iam", "entity", "ghg", "evidence", "audit" }
)
package ai.claudecode.esgt3.rpt;
