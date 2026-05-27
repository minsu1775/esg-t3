package ai.claudecode.esgt3.iam.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "policy_decisions")
public class PolicyDecisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "subject_user_id")
    private UUID subjectUserId;

    @Column(name = "subject_role", length = 32)
    private String subjectRole;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "resource_type", nullable = false, length = 64)
    private String resourceType;

    @Column(name = "resource_id", length = 128)
    private String resourceId;

    @Column(name = "effect", nullable = false, length = 16)
    private String effect;

    @Column(name = "policy_id", length = 128)
    private String policyId;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    protected PolicyDecisionEntity() {}

    PolicyDecisionEntity(UUID tenantId, Instant occurredAt, UUID subjectUserId, String subjectRole,
                         String action, String resourceType, String resourceId,
                         String effect, String policyId, String reason) {
        this.tenantId = tenantId;
        this.occurredAt = occurredAt;
        this.subjectUserId = subjectUserId;
        this.subjectRole = subjectRole;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.effect = effect;
        this.policyId = policyId;
        this.reason = reason;
    }

    public Long getId() { return id; }
    public String getEffect() { return effect; }
    public String getPolicyId() { return policyId; }
    public UUID getTenantId() { return tenantId; }
    public Instant getOccurredAt() { return occurredAt; }
}
