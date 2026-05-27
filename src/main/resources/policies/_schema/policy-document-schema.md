# Policy Document YAML 스키마

`PolicyYamlLoader`가 `resources/policies/**/*.yaml`을 파싱한다.

## 최상위 구조

```yaml
policies:
  - id: <module>-<verb>-<resource>     # 필수, kebab-case
    description: 한 줄 한국어 사유       # 필수
    effect: PERMIT | DENY               # 필수
    priority: 0 ~ 250                    # 선택, 기본 0
    when:                                # 필수, 매칭 조건 키-값
      subject.role: ESG_MANAGER
      subject.tenantId: "${resource.tenantId}"
      subject.assignedEntityIds: { contains: "${resource.entityId}" }
      action: [WRITE, READ]
      resource.type: ActivityData
      resource.approvalState: [DRAFT, REJECTED]
    tests:                               # 선택(권장), CI에서 자동 실행
      - name: 통과 케이스 이름
        ctx:
          subject: { role: ESG_MANAGER, tenantId: t1, assignedEntityIds: [e1] }
          resource: { type: ActivityData, tenantId: t1, attributes: { entityId: e1, approvalState: DRAFT } }
          action: WRITE
        expect: PERMIT
```

## priority 표준

| 값 | 용도 |
|---|---|
| 250 | emergency-lockdown (비상 격리) |
| 200 | disclosed-data-immutability (공시 완료 데이터 불변) |
| 100 | SoD (자기 승인 금지 등) |
| 0   | 일반 PERMIT/DENY |

## 변수 치환

`"${subject.tenantId}"`, `"${resource.entityId}"`, `"${env.lockedTenantIds}"` 형식 지원.
- `subject.*`: userId, role, tenantId, assignedEntityIds, departmentId
- `resource.*`: type, tenantId, attributes의 키
- `env.*`: requestIp, mfaVerified, lockedTenantIds

## 매처

- 스칼라 값: 평등 비교
- 리스트: in 매처 (실제 값이 리스트 원소 중 하나)
- `{contains: X}`: 컬렉션 속성이 X를 포함
- `{in: [a, b]}`: 실제 값이 리스트 a/b 중 하나
