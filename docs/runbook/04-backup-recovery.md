# Runbook 04: 백업·복구 (Disaster Recovery)

> **트리거**: 데이터 손실, 디스크 장애, 위변조 사후 복원 요청
> **심각도**: P1
> **목표 RTO**: 4시간 (Spec §5.2 계승)
> **목표 RPO**: 1시간 (일 4회 스냅샷)
> **마지막 훈련**: TBD (Phase 8 Game Day에 1회 + 운영 분기)
> **소유자**: ESG Ops 팀
> **본문 완성 Phase**: Phase 8

## 1. 영향 평가

- 전체 시스템 영향 가능
- 복구 중 WRITE 차단 필요

## 2. 진단

- 최신 정상 백업 확인: `./scripts/runbook/04-list-backups.sh`
- 손실 데이터 범위 파악

## 3. 결정 트리

```
Q1: 백업이 무결한가?
  ├─ 예 → 4.A 표준 복구
  └─ 아니오 → 4.B 다중 백업 조합

Q2: 부분 복구로 충분한가?
  ├─ 단일 tenant 손실 → 해당 tenant만 복구
  └─ 전역 손실 → 전체 복구
```

## 4. 복구 절차

### 4.A 표준 복구
1. `./scripts/runbook/04-restore-from-backup.sh --backup-id=<id> --target=<full|tenant:<id>>`
2. 복구 우선순위: AuditLog → Snapshot → ActivityData → 사용자·정책
3. `./scripts/runbook/04-verify-hash-chain-all.sh` (전 테넌트 Hash Chain 재검증)
4. 검증 통과 시에만 WRITE 재개

## 5. 사후 조치

- [ ] RTO 실측 기록
- [ ] 백업 절차 개선 필요 항목 식별
- [ ] 분기 Game Day 일정 검토

## 6. 에스컬레이션

- 운영 1시간: CTO
- 4시간 초과 위험: 비상대응팀

## 7. 관련 자료

- Spec §5.2, §5.10 (SLO)
- ADR-008-hash-chain-disaster-recovery.md
