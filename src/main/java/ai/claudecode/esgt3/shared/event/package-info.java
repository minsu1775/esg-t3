/**
 * shared 모듈의 도메인 이벤트 공통 contract.
 * 모든 도메인 이벤트는 {@link ai.claudecode.esgt3.shared.event.DomainEvent}를 구현해야 하며,
 * 다른 모듈은 이 패키지를 통해서만 contract를 참조할 수 있다.
 */
@org.springframework.modulith.NamedInterface("event")
package ai.claudecode.esgt3.shared.event;
