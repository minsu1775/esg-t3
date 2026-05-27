# 16. Domain Event Catalog 규칙

> **로드 조건**: `**/event/**`, `**/api/event/**`, `shared/event/**`
> **적용**: 도메인 이벤트 신규/수정 시

## 네이밍 컨벤션

`<module>.<Resource><PastTenseVerb>`:
- `entity.LegalEntityCreated`
- `iam.PolicyDenied`
- `ghg.EmissionCalculated`
- `audit.HashChainMismatchDetected`

ArchUnit이 자동 검증 (ConventionTest):
- `event` 패키지 내 클래스 + `Created/Updated/Deleted/Approved/Rejected` 접미사 → `DomainEvent` 구현 의무

## 필수 필드 (DomainEvent 인터페이스)

```java
public interface DomainEvent {
    String tenantId();      // RLS 격리, null 금지
    Instant occurredAt();   // 발생 시각, 불변, null 금지
    String idempotencyKey();// Outbox 중복 방지, null 금지
}
```

null 검증은 publisher가 책임 (`IllegalStateException`). ArchUnit은 런타임 null 검증 불가.

## Modulith Documenter

빌드 시 `build/spring-modulith-docs/` 자동 생성. CI에서 산출물 업로드.

## 금지 사항

- ❌ Event를 mutable 클래스로 작성 (record 또는 immutable 객체만)
- ❌ Event가 도메인 객체 참조 보유 (id·VO만)
- ❌ Event 발행에서 동일 모듈의 다른 도메인 객체 직접 호출 (Listener에서 처리)
