package ai.claudecode.esgt3.shared.tenant;

import java.util.UUID;

/**
 * 요청 스레드 단위 tenantId 보관. ServletRequest 종료 시 반드시 clear.
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT_ID = new ThreadLocal<>();
    private static final ThreadLocal<UUID> USER_ID = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(UUID tenantId, UUID userId) {
        TENANT_ID.set(tenantId);
        USER_ID.set(userId);
    }

    public static UUID currentTenantId() {
        return TENANT_ID.get();
    }

    public static UUID currentUserId() {
        return USER_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
        USER_ID.remove();
    }
}
