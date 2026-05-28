/**
 * 테넌트 컨텍스트 - 요청 단위로 현재 tenantId/userId를 유지하고
 * PostgreSQL 세션 변수에 set_config로 전달하여 RLS 정책이 자동 적용되도록 한다.
 *
 * <p>esg-t2 L-P1-03: set_config 호출 시 반드시 PreparedStatement 파라미터 바인딩 사용
 * (문자열 연결은 SQL Injection 위험).
 *
 * <p>L-P1-07 (Phase 1 Task 11에서 발견): {@code SELECT set_config(...)}는 함수라 결과 행을
 * 반환하므로 {@code jdbcTemplate.update()} 대신 {@code jdbcTemplate.queryForObject()} 사용.
 */
@org.springframework.modulith.NamedInterface("tenant")
package ai.claudecode.esgt3.shared.tenant;
