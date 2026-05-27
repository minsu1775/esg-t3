# Runbook 06: 배포 절차

> **트리거**: 정기 릴리스 또는 핫픽스
> **심각도**: — (절차)
> **목표 RTO**: 15분 (롤백 시)
> **본문 완성 Phase**: Phase 0 (지금 완성)

## 1. 배포 전 체크리스트

- [ ] ArchUnit 통과
- [ ] ModularityTest 통과
- [ ] Hash Chain 검증 성공 (직전 24시간)
- [ ] 정책 YAML 단위 테스트 통과
- [ ] DB 마이그레이션 dry-run 성공 (`./gradlew flywayValidate`)
- [ ] Runbook 링크 검증 (`./tests/runbook-link-validation.sh`)

## 2. 배포 단계 (Blue-Green + 카나리)

1. Flyway 마이그레이션 적용 (롤백 계획 첨부)
2. 새 버전 deployment (Blue)
3. 카나리 5% 라우팅 → 메트릭 5분 관찰
4. 100% 라우팅
5. 이전 버전(Green) 24시간 보존

## 3. 롤백 트리거

- 5xx 에러율 1% 이상
- AuditLog 누락 발견 (audit_outbox_lag > 60s)
- Hash Chain 불일치
- 정책 평가 DENY 비율 평소 대비 5배 이상

## 4. 롤백 절차

```bash
./scripts/runbook/06-rollback.sh --target=<previous-version>
# 카나리/100% 라우팅을 이전 버전으로 즉시 전환
# 새 마이그레이션이 있었다면 별도 절차 (롤백 SQL)
```

## 5. 배포 후 검증

- [ ] `/actuator/health` UP
- [ ] Hash Chain 검증 (수동 1회)
- [ ] 핵심 사용자 시나리오 (UJ-01) 스모크
- [ ] `infra.DeploymentApplied` AuditLog 기록 확인

## 6. 관련 자료

- Spec §8.2 (Phase 0 DoD)
