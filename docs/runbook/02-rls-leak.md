# Runbook 02: RLS 누락 / 테넌트 간 데이터 유출 의심

> **트리거**: 감사·외부 보고로 발견 (자동 탐지 어려움)
> **심각도**: P1 (보안 사고)
> **목표 RTO**: 즉시 격리, 1시간 1차 대응
> **마지막 훈련**: TBD (Phase 1 완료 후 첫 실행)
> **소유자**: ESG Ops 팀
> **본문 완성 Phase**: Phase 1

## 1. 영향 평가

- 데이터 영향: 다른 테넌트의 데이터가 노출됐을 가능성
- 사용자 영향: 영향 받은 모든 테넌트 사용자
- 규제 영향: 개인정보보호법·KSSB 위반. 사용자 통보 의무

## 2. 진단

```bash
./scripts/runbook/02-rls-trace-analyze.sh <suspected_trace_id>
./scripts/runbook/02-find-missing-set-config.sh   # SET app.current_tenant_id 누락 SQL 패턴 탐색
```

## 3. 결정 트리

```
Q1: 의심 trace_id를 확인 가능한가?
  ├─ 예 → 해당 trace 분석 → 누락 지점 식별
  └─ 아니오 → 전수 audit_logs에서 tenant_id 일치하지 않는 PolicyEvaluation 탐색

Q2: TenantContextInterceptor 동작 누락인가?
  ├─ 예 → 4.A
  └─ 아니오 → 4.B (Repository 메서드의 명시적 tenant 누락)
```

## 4. 복구 절차

### 4.0 즉시 격리 (공통 선행, Phase 1 도입)
1. `POST /api/v1/admin/lockdown/{tenantId}` (SUPER_ADMIN) — emergency-lockdown 정책(priority 250)이 해당 테넌트의 WRITE/APPROVE/DELETE 즉시 차단
2. 원인 분석·복구 완료 후 `DELETE /api/v1/admin/lockdown/{tenantId}` (감사 사유 reason 필수)

### 4.A TenantContextInterceptor 누락
1. `./scripts/runbook/02-revoke-tenant-sessions.sh <tenant_id>` (모든 세션 만료)
2. 정확한 누락 지점에 `TenantContextInterceptor` 적용 여부 검증 (`/api/**` 매핑, `/api/v1/auth/**` 제외)
3. ArchUnit 규칙 추가 (모든 컨트롤러 메서드에 인터셉터 의존 검증)

### 4.B 명시적 tenant 누락
1. 누출 범위 SQL 쿼리로 산정
2. 영향 사용자 통보 (개인정보보호법 §34 위반 통보 의무)
3. RLS 정책 재검토 + Repository 단위 테스트 추가

### 4.C set_config 값 오염 (Phase 1 L-P1-10)
1. `current_setting('app.current_tenant_id', true)`가 빈 문자열·잘못된 값을 반환하는지 확인
2. RLS 정책은 `NULLIF(current_setting(...), '')::uuid`로 빈 문자열을 NULL 처리(fail-closed) — V6 마이그레이션 적용 여부 확인
3. set_config 호출이 파라미터 바인딩(`?`) + `queryForObject` 패턴을 따르는지 검증 (L-P1-03/07)

## 5. 사후 조치

- [ ] 침투 테스트 1회 실행
- [ ] 정기 침투 테스트 일정 갱신
- [ ] 포스트모템 작성

## 6. 에스컬레이션

- 즉시: 보안 책임자
- 1시간: CTO + 법무
- 24시간: 사용자 통보 결정

## 7. 관련 자료

- ADR-016-rls-set-config-binding.md (set_config 바인딩 + 빈 문자열 방어)
- ADR-014-abac-yaml-dsl.md (emergency-lockdown 정책)
- design.md §3.8 (ABAC + RLS 이중 방어)
- insight.md L-P1-03 / L-P1-07 / L-P1-10
