package ai.claudecode.esgt3.iam.infra;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Append-only Repository (rule 08) - {@code Repository<T, ID>} 마커 인터페이스 사용.
 * <p>{@code delete*}·{@code deleteAll*} 메서드 의도적 미노출.
 */
public interface PolicyDecisionJpaRepository extends Repository<PolicyDecisionEntity, Long> {

    PolicyDecisionEntity save(PolicyDecisionEntity entity);

    long countByTenantId(UUID tenantId);

    List<PolicyDecisionEntity> findTop100ByTenantIdOrderByOccurredAtDesc(UUID tenantId);
}
