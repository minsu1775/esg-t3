package ai.claudecode.esgt3.iam.domain;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * ABAC 주체 - 사용자 인증 정보 + 속성 prefetch 결과.
 *
 * @param userId 사용자 식별자 (null 허용 - 시스템 호출)
 * @param role 6역할 중 하나
 * @param tenantId 소속 테넌트
 * @param assignedEntityIds ESG_MANAGER 담당 법인 (다른 역할은 빈 Set)
 * @param departmentId 부서 ID (M+1)
 */
public record Subject(
    UUID userId,
    String role,
    UUID tenantId,
    Set<UUID> assignedEntityIds,
    UUID departmentId
) {
    public Subject {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(tenantId, "tenantId");
        Objects.requireNonNull(assignedEntityIds, "assignedEntityIds");
        assignedEntityIds = Set.copyOf(assignedEntityIds);
    }
}
