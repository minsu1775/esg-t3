# ADR-016: RLS set_config 파라미터 바인딩 + 빈 문자열 방어

## 상태
Accepted (2026-05-28, Phase 1)

## 배경
esg-t2 Phase 1에서 `set_config('app.current_tenant_id', '" + id + "', true)` 문자열 연결 패턴이 SQL Injection 위험으로 코드 리뷰에서 차단됨(L-P1-03). esg-t3는 RLS를 1차 방어선으로 도입하므로 이 패턴을 표준화한다.

## 결정
1. **파라미터 바인딩 필수**: 모든 set_config 호출은 PreparedStatement `?` 자리표시자 사용. 문자열 연결 금지.
2. **queryForObject 사용** (L-P1-07): `SELECT set_config(...)`는 함수라 결과 행을 반환 → `jdbcTemplate.update()`는 예외. `jdbcTemplate.queryForObject("SELECT set_config('app.current_tenant_id', ?, true)", String.class, tenantId)` 사용.
3. **빈 문자열 방어** (L-P1-10): RLS 정책은 `NULLIF(current_setting('app.current_tenant_id', true), '')::uuid` 사용. 빈 문자열을 NULL로 변환해 "테넌트 미설정 = 전체 차단"(fail-closed). HikariCP 커넥션 풀 재사용 시 세션 상태 누출 방어.

## 구현
- `TenantContextInterceptor`: 요청 단위 ThreadLocal `TenantContext`에서 tenantId 읽어 set_config 주입 + MDC(tenantId/userId)
- `/api/v1/auth/**`는 인터셉터 제외 (로그인 시점엔 tenantId 없음)
- RLS 적용 테이블: users, legal_entities, entity_relationships, policy_decisions (V4 + V6)
- 글로벌 마스터 제외: tenants, emission_factors, disclosure_schedules

## 이중 방어 (design.md §3.8)
- **RLS (1차)**: DB 레벨 tenant_id 격리. 애플리케이션 버그로도 우회 불가.
- **ABAC (2차)**: 같은 테넌트 내 entityId/sensitivity/approvalState 세밀 제어.
- 둘 다 통과해야 데이터 접근 가능.

## 검증
- Testcontainers PG 18에서 `SET ROLE app_role` 전환 후 tenantA/tenantB 각 1행만 노출
- set_config 미설정 시 전체 차단 확인
- 운영 환경은 app_role로 접속하여 RLS 강제, 로컬/테스트는 superuser로 BYPASS 후 SET ROLE로 검증
