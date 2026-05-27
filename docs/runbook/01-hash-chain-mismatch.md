# Runbook 01: Hash Chain 무결성 오류

> **트리거**: `esg_t3_hash_chain_integrity_check_total{result="mismatch"} > 0`
> **심각도**: P1 (데이터 무결성)
> **목표 RTO**: 4시간
> **마지막 훈련**: TBD (Phase 2 완료 후 첫 실행)
> **소유자**: ESG Ops 팀
> **본문 완성 Phase**: Phase 2

## 1. 영향 평가

- 데이터 영향: 해당 tenant의 AuditLog Hash Chain 무결성 깨짐
- 사용자 영향: emergency-lockdown 정책 자동 활성화로 WRITE 차단
- 규제 영향: KSSB 감사 대응에 영향. 외부 검증인 통보 필요 시점 발생

## 2. 진단

```bash
./scripts/runbook/01-diagnose-hash-chain.sh <tenant_id>
```

관련 대시보드: http://grafana.local/d/esg-t3-04 (Audit Integrity)
Loki 쿼리: `{tenantId="<tenant_id>"} | json | event_type =~ "audit.*"`

## 3. 결정 트리

```
Q1: 영향 범위가 단일 테넌트인가, 전역인가?
  ├─ 단일 테넌트 → Q2로
  └─ 전역 다수 테넌트 → 비상 격리 + Runbook 04 (DR) 동시 발동

Q2: 직렬화 불일치(버그) vs DB 직접 조작(위변조)?
  ├─ canonicalPayload() 재실행 결과가 일치 → A. 직렬화 불일치
  └─ 재실행해도 불일치 → B. 직접 조작 의심
```

## 4. 복구 절차

### 4.A 직렬화 불일치 (버그)
1. `./scripts/runbook/01-recover-canonical.sh <tenant_id> --dry-run`
2. SUPER_ADMIN 승인 후 `--apply` 실행
3. Hash Chain 재계산 → 검증 통과 확인
4. emergency-lockdown 해제 (`DELETE /api/v1/admin/lockdown/<tenant_id>`)

### 4.B DB 직접 조작 (위변조)
1. 영향 범위 확정 (의심 audit_log id 범위, 시간 창)
2. 백업에서 해당 tenant의 audit_logs 복원 (`./scripts/runbook/04-restore-from-backup.sh`)
3. 법무 + 외부 감사 통보
4. 침해 사고 보고서 작성 (`docs/postmortems/`)
5. emergency-lockdown은 보고서 완료 후 해제

## 5. 사후 조치

- [ ] 전 테넌트 Hash Chain 재검증
- [ ] 포스트모템 작성 (P1 의무)
- [ ] AuditIntegrityScheduler 검증 주기 검토
- [ ] 알림 임계값 재조정 필요 여부 결정

## 6. 에스컬레이션

- 30분 미해결: 팀 리드
- 1시간 미해결: CTO
- 2시간 미해결 + B(위변조): 법무·감사 (필수)

## 7. 관련 자료

- ADR-008-hash-chain-disaster-recovery.md
- Spec §4 (audit 모듈)
- 과거 사례: (TBD)
