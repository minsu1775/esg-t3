package ai.claudecode.esgt3.shared.event;

import java.time.Instant;

/**
 * 모든 도메인 이벤트의 마커 인터페이스.
 *
 * <p>이벤트는 다음을 의무로 가진다:
 * <ul>
 *   <li>{@link #tenantId()} — RLS 격리 단위</li>
 *   <li>{@link #occurredAt()} — 발생 시각 (불변)</li>
 *   <li>{@link #idempotencyKey()} — 멱등성 키 (Outbox 중복 방지)</li>
 * </ul>
 *
 * <p>네이밍 컨벤션: {@code <module>.<Resource><PastTenseVerb>} (예: {@code entity.LegalEntityCreated}).
 * ArchUnit이 이 인터페이스 구현 + 컨벤션을 빌드 시 강제한다.
 */
public interface DomainEvent {

    String tenantId();

    Instant occurredAt();

    String idempotencyKey();
}
