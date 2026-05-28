package ai.claudecode.esgt3.iam.infra;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * JWT 토큰 파싱 결과 - Spring Security Authentication 구현.
 *
 * <p>{@link ai.claudecode.esgt3.iam.api.PolicyFacade}가 Authentication에서 ABAC Subject 속성 추출.
 * <p>Task 14의 {@code JwtTokenProvider.parseAccessToken()}이 생성·반환한다.
 */
@Getter
public class JwtAuthentication extends AbstractAuthenticationToken {

    private final UUID userId;
    private final UUID tenantId;
    private final String role;
    private final Set<UUID> assignedEntityIds;

    public JwtAuthentication(UUID userId, UUID tenantId, String role, Set<UUID> assignedEntityIds) {
        super(List.of(new SimpleGrantedAuthority("ROLE_" + role)));
        this.userId = userId;
        this.tenantId = tenantId;
        this.role = role;
        this.assignedEntityIds = assignedEntityIds == null ? Set.of() : Set.copyOf(assignedEntityIds);
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() { return ""; }

    @Override
    public Object getPrincipal() { return userId; }
}
