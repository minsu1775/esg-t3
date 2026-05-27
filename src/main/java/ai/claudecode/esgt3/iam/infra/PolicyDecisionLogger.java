package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.iam.domain.PolicyContext;
import ai.claudecode.esgt3.iam.domain.PolicyDecision;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * 정책 평가 결과를 {@code policy_decisions} 테이블에 INSERT.
 *
 * <p>REQUIRES_NEW 트랜잭션 - 호출 측 트랜잭션 롤백과 무관하게 평가 이력 보존.
 * Phase 2에서 outbox 패턴으로 확장 + Hash Chain 연계.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PolicyDecisionLogger {

    private final PolicyDecisionJpaRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(PolicyContext ctx, PolicyDecision decision) {
        var entity = new PolicyDecisionEntity(
            ctx.subject().tenantId(),
            Instant.now(),
            ctx.subject().userId(),
            ctx.subject().role(),
            ctx.action().name(),
            ctx.resource().type(),
            null,
            decision.effect().name(),
            decision.policyId(),
            decision.reason()
        );
        try {
            repository.save(entity);
        } catch (Exception e) {
            log.error("정책 평가 결과 저장 실패 - policyId={}, effect={}",
                decision.policyId(), decision.effect(), e);
        }
    }
}
