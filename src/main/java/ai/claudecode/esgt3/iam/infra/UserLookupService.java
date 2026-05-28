package ai.claudecode.esgt3.iam.infra;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * 로그인·리프레시 시 사용자 조회 - Phase 1은 JdbcTemplate 기반 단순 조회.
 * Phase 5에서 entity 모듈 본격화되면 JPA Repository로 이관.
 */
@Component
@RequiredArgsConstructor
public class UserLookupService {

    private final JdbcTemplate jdbcTemplate;

    public record AuthenticatedUser(
        UUID userId,
        UUID tenantId,
        String role,
        Set<UUID> entityIds,
        String passwordHash
    ) {}

    public Optional<AuthenticatedUser> findByEmail(String email) {
        List<AuthenticatedUser> rows = jdbcTemplate.query(
            """
            SELECT u.id, u.tenant_id, u.password_hash, ur.role
            FROM users u
            JOIN user_roles ur ON ur.user_id = u.id
            WHERE u.email = ? AND u.active = true
            LIMIT 1
            """,
            (rs, i) -> new AuthenticatedUser(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(4),
                Set.of(),
                rs.getString(3)
            ),
            email
        );
        if (rows.isEmpty()) return Optional.empty();
        AuthenticatedUser base = rows.get(0);
        Set<UUID> entityIds = loadEntityIds(base.userId());
        return Optional.of(new AuthenticatedUser(
            base.userId(), base.tenantId(), base.role(), entityIds, base.passwordHash()));
    }

    public Optional<AuthenticatedUser> findById(UUID userId) {
        List<AuthenticatedUser> rows = jdbcTemplate.query(
            """
            SELECT u.id, u.tenant_id, u.password_hash, ur.role
            FROM users u
            JOIN user_roles ur ON ur.user_id = u.id
            WHERE u.id = ? AND u.active = true
            LIMIT 1
            """,
            (rs, i) -> new AuthenticatedUser(
                rs.getObject(1, UUID.class),
                rs.getObject(2, UUID.class),
                rs.getString(4),
                Set.of(),
                rs.getString(3)
            ),
            userId
        );
        if (rows.isEmpty()) return Optional.empty();
        AuthenticatedUser base = rows.get(0);
        Set<UUID> entityIds = loadEntityIds(base.userId());
        return Optional.of(new AuthenticatedUser(
            base.userId(), base.tenantId(), base.role(), entityIds, base.passwordHash()));
    }

    private Set<UUID> loadEntityIds(UUID userId) {
        return new HashSet<>(jdbcTemplate.query(
            "SELECT entity_id FROM user_entity_assignments WHERE user_id = ?",
            (rs, i) -> rs.getObject(1, UUID.class),
            userId
        ));
    }
}
