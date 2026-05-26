package ai.claudecode.esgt3.shared.event;

import java.time.Instant;

/**
 * 모든 도메인 이벤트의 공통 contract.
 *
 * <p>이벤트는 다음 3개 필드를 의무로 가지며, 모두 {@code null}일 수 없다.
 * 위반 시 publisher 측에서 {@link IllegalStateException}을 던져야 한다 — Outbox 멱등성과
 * RLS 격리가 이 값들에 의존하기 때문이다.
 *
 * <ul>
 *   <li>{@link #tenantId()} — RLS 격리 단위 (null 금지)</li>
 *   <li>{@link #occurredAt()} — 발생 시각, 불변 (null 금지)</li>
 *   <li>{@link #idempotencyKey()} — Outbox 중복 방지 키 (null 금지, 빈 문자열도 권장 안 함)</li>
 * </ul>
 *
 * <p>네이밍 컨벤션: {@code <module>.<Resource><PastTenseVerb>}
 * (예: {@code entity.LegalEntityCreated}, {@code iam.PolicyDenied}).
 * ArchUnit이 이 contract 구현 + 네이밍을 빌드 시 강제한다 — null 가드는 단위 테스트로 강제한다.
 */
public interface DomainEvent {

    String tenantId();

    Instant occurredAt();

    String idempotencyKey();
}
