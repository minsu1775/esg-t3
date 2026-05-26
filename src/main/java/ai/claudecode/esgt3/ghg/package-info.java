/**
 * GHG 모듈 — 배출량 계산 (Scope 1/2; Scope 3는 M+1).
 *
 * <p>EmissionCalculator, EmissionFactorResolver, UnitConverter, ConsolidationEngine 포함.
 */
@org.springframework.modulith.ApplicationModule(
    displayName = "GHG (Emission Calculation)",
    allowedDependencies = { "shared", "iam", "entity", "evidence", "audit" }
)
package ai.claudecode.esgt3.ghg;
