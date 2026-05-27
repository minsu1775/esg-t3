# Runbook 03: 재계산 실패

> **트리거**: `emission_calculation_failures_total` 임계 초과
> **심각도**: P2
> **목표 RTO**: 24시간
> **마지막 훈련**: TBD (Phase 4 완료 후 첫 실행)
> **소유자**: ESG Ops 팀
> **본문 완성 Phase**: Phase 4

## 1. 영향 평가

- 데이터 영향: 일부 활동 데이터에 대한 EmissionRecord 누락
- 사용자 영향: 보고서 생성 불완전
- 규제 영향: 공시 마감 직전이면 P1으로 격상

## 2. 진단

```bash
./scripts/runbook/03-list-failed-calculations.sh <reporting_year>
./scripts/runbook/03-classify-failure.sh <calculation_run_id>
```

## 3. 결정 트리

```
Q1: 실패 원인 분류
  ├─ A. 활동 데이터 문제 (null, 음수) → 활동 데이터 수정
  ├─ B. 배출계수 미존재 (factorAt 조회 실패) → 계수 추가 + 재로드
  ├─ C. 산식 오류 (M+1 Formula DSL) → 산식 버전 롤백
  └─ D. 인프라 (DB, OutOfMemory) → 운영팀 핸드오프
```

## 4. 복구 절차

### 부분 재계산 (전체 재실행 피함)
1. 영향 받은 activity_data id 범위 확정
2. `./scripts/runbook/03-partial-recalculate.sh --activity-ids=<ids> --dry-run`
3. 결과 검증 후 `--apply`
4. AuditLog `ghg.RecalculationApplied` 이벤트 기록 확인

## 5. 사후 조치

- [ ] 실패 패턴 분석 → 단위 테스트 추가
- [ ] 배출계수 마스터 검토

## 6. 에스컬레이션

- 24시간 미해결: 도메인 전문가 호출

## 7. 관련 자료

- Spec §7.3 (calculation_runs)
- ADR-XXX 재계산 정책 (Phase 4 작성 예정)
