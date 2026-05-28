/**
 * IAM 공개 API - 다른 모듈이 import 가능한 단 하나의 패키지.
 *
 * <p>{@link ai.claudecode.esgt3.iam.api.PolicyFacade}가 ABAC 결정 단일 진입점.
 * 컨트롤러는 {@code @PreAuthorize("@policy.allow(authentication, '<ACTION>', #resource)")}로 사용.
 */
@org.springframework.modulith.NamedInterface("api")
package ai.claudecode.esgt3.iam.api;
