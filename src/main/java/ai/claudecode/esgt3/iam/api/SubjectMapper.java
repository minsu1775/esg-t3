package ai.claudecode.esgt3.iam.api;

import ai.claudecode.esgt3.iam.domain.Subject;
import ai.claudecode.esgt3.iam.infra.JwtAuthentication;
import org.springframework.security.core.Authentication;

import java.util.Set;
import java.util.UUID;

/**
 * Spring Security Authentication → ABAC Subject 변환 helper.
 *
 * <p>{@link JwtAuthentication}이 표준 형태. 익명·시스템 호출은 ANONYMOUS 역할로 처리.
 */
final class SubjectMapper {

    private static final UUID ZERO_TENANT = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private SubjectMapper() {}

    static Subject fromAuthentication(Authentication auth) {
        if (auth instanceof JwtAuthentication ja) {
            return new Subject(
                ja.getUserId(),
                ja.getRole(),
                ja.getTenantId(),
                ja.getAssignedEntityIds() == null ? Set.of() : Set.copyOf(ja.getAssignedEntityIds()),
                null
            );
        }
        return new Subject(null, "ANONYMOUS", ZERO_TENANT, Set.of(), null);
    }
}
