package ai.claudecode.esgt3.iam.infra;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 시스템 전체 격리 테넌트 집합 + emergency-lockdown 정책의 env.lockedTenantIds 동기화.
 *
 * <p>{@code System.setProperty("esg.lockdown.tenants", csv)}로 WhenClauseMatcher가
 * 읽는 값을 즉시 갱신. 분산 환경(M+1)에서는 Redis pub/sub로 전체 노드에 전파 예정.
 */
@Component
@Slf4j
public class LockdownState {

    private final Set<UUID> locked = ConcurrentHashMap.newKeySet();

    public void activate(UUID tenantId, String reason) {
        locked.add(tenantId);
        syncSystemProperty();
        log.warn("emergency-lockdown 활성화 - tenantId={}, reason={}", tenantId, reason);
    }

    public void deactivate(UUID tenantId, String reason) {
        locked.remove(tenantId);
        syncSystemProperty();
        log.warn("emergency-lockdown 해제 - tenantId={}, reason={}", tenantId, reason);
    }

    public Set<UUID> snapshot() {
        return Set.copyOf(locked);
    }

    private void syncSystemProperty() {
        String csv = String.join(",", locked.stream().map(UUID::toString).toList());
        System.setProperty("esg.lockdown.tenants", csv);
    }
}
