# 13. ABAC 정책 작성 규칙

> **로드 조건**: 항상 (보안 핵심)
> **적용 시작**: Phase 1 (iam 모듈 도입)

## 핵심 원칙

1. **정책은 데이터**: 코드가 아닌 YAML 파일로 외부화. `policies/<module>/<policy-name>.yaml`. SUPER_ADMIN 승인 후 핫리로드.
2. **DENY-default**: NOT_APPLICABLE은 자동으로 DENY. 명시적 PERMIT 없으면 거부.
3. **priority 시스템**:
   - 250: emergency-lockdown (비상 격리, 최우선)
   - 200: disclosed-data-immutability (공시 완료 데이터 불변 — SUPER_ADMIN도 막힘)
   - 100: SoD (자기 승인 금지 등)
   - 기본(없음): 일반 PERMIT 규칙
4. **모든 평가 결과는 AuditLog 대상**: PERMIT/DENY/NOT_APPLICABLE 모두 `policy_decisions` 테이블 INSERT.

## 정책 YAML 표준

```yaml
- id: <module>-<verb>-<resource>
  description: 한 문장 한국어 설명 (왜 이 정책이 존재하는가)
  effect: PERMIT | DENY
  priority: 0 ~ 250 (선택, 기본 0)
  when:
    subject.role: <단일 역할 또는 리스트>
    subject.tenantId: "${resource.tenantId}"   # 같은 테넌트만
    action: [READ, WRITE, ...]
    resource.type: <타입>
    resource.<attribute>: <값 또는 비교>
  tests:                                        # 단위 테스트 케이스
    - name: 통과해야 하는 케이스
      ctx: { ... }
      expect: PERMIT
    - name: 차단해야 하는 케이스
      ctx: { ... }
      expect: DENY
```

## 금지 사항

- ❌ 컨트롤러 메서드에 인라인 SpEL로 정책 작성 (분산 불가)
- ❌ Repository 메서드에서 직접 권한 체크 (PolicyEvaluator 단일 진입점만)
- ❌ 정책 평가 결과를 캐시 (캐싱은 PolicyDocument 자체만, 평가는 매번)

## ArchUnit 강제

- 모든 컨트롤러 메서드는 `@PreAuthorize("@policy.allow(...)")` 또는 `@PermitAll` 명시 (Phase 1+)
- PolicyEvaluator는 `iam.domain` 패키지만 거주
