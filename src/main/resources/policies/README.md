# ESG-T3 ABAC 정책 카탈로그

## 파일 구조

- `iam/` - 6역할 + emergency-lockdown + disclosed-data-immutability
- `_schema/` - 정책 작성 스키마

## 규칙

1. 정책은 **데이터**다. 코드에 인라인 SpEL 금지 (`.claude/rules/13-abac-policy.md`).
2. SUPER_ADMIN 승인 후 핫리로드.
3. 모든 정책은 `tests:` 섹션 의무 — 통과 + 차단 케이스 각 1개 이상.
4. priority 표준: 250(emergency) > 200(disclosed) > 100(SoD) > 0(일반).

## 변경 절차

1. YAML 수정 → PR.
2. CI에서 `PolicyYamlTestCasesTest` 통과 검증.
3. SUPER_ADMIN approve → 배포.
4. 정책 변경 자체도 AuditLog 기록 (Phase 2).
