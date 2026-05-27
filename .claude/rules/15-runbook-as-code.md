# 15. Runbook-as-Code 규칙

> **로드 조건**: `docs/runbook/**`, `scripts/runbook/**`, `infra/observability/alerts/**`
> **적용**: 운영 절차 신규/수정 시

## Runbook 파일 표준 구조

`docs/runbook/NN-<topic>.md` (Section 6.2):

```
# Runbook NN: <사고 이름>

> 트리거 / 심각도 / RTO / 마지막 훈련 / 소유자 / 본문 완성 Phase

## 1. 영향 평가
## 2. 진단
## 3. 결정 트리
## 4. 복구 절차
## 5. 사후 조치
## 6. 에스컬레이션
## 7. 관련 자료
```

## 알림과의 자동 연결

`infra/observability/alerts/*.yml`의 모든 알림에 `runbook:` label 필수:
```yaml
labels:
  runbook: "docs/runbook/NN-<topic>.md"
```

CI에서 `tests/runbook-link-validation.sh`가 모든 링크 유효성 검증. 누락 시 빌드 실패.

## 스크립트 표준

`scripts/runbook/**/*.sh`:
- bash + `set -euo pipefail`
- 모든 변경 작업은 `--dry-run` 옵션 제공
- 실행 결과를 AuditLog 기록 (Phase 2+에서 `runbook.ScriptExecuted` 이벤트)
- SUPER_ADMIN 토큰 확인 (`require_super_admin_token`)
- 공통 헬퍼는 `scripts/runbook/lib/` 사용

## Game Day 의무

분기 1회 시나리오 순환 (Q1: Hash Chain 위변조 / Q2: RLS 누락 / Q3: 재계산 실패 / Q4: DR). 결과를 `docs/postmortems/`에 기록.
