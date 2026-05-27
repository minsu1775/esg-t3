package ai.claudecode.esgt3.iam.domain;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyRegistryTest {

    @Test
    void 초기_레지스트리는_빈_평가자를_제공() {
        var registry = new PolicyRegistry();
        assertThat(registry.evaluator().ruleIdsInEvaluationOrder()).isEmpty();
    }

    @Test
    void replace_호출_시_새_평가자로_교체() {
        var registry = new PolicyRegistry();
        var rule = new PolicyRule("p", "d", PolicyEffect.PERMIT, 0,
            Map.of("subject.role", "ESG_MANAGER"), List.of());
        registry.replace(List.of(new PolicyDocument("t.yaml", List.of(rule))));
        assertThat(registry.evaluator().ruleIdsInEvaluationOrder()).containsExactly("p");
    }

    @Test
    void replace_중에도_concurrent_evaluate가_NPE_없이_동작한다() throws Exception {
        var registry = new PolicyRegistry();
        var rule = new PolicyRule("p", "d", PolicyEffect.PERMIT, 0,
            Map.of("subject.role", "ESG_MANAGER"), List.of());
        registry.replace(List.of(new PolicyDocument("t.yaml", List.of(rule))));

        var pool = Executors.newFixedThreadPool(4);
        var done = new CountDownLatch(4);
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    registry.evaluator().evaluate(PolicyContext.of(
                        new Subject(UUID.randomUUID(), "ESG_MANAGER",
                            UUID.randomUUID(), Set.of(), null),
                        Resource.of("ActivityData", UUID.randomUUID(), Map.of()),
                        PolicyAction.READ
                    ));
                }
                done.countDown();
            });
        }
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                for (int j = 0; j < 100; j++) {
                    registry.replace(List.of(new PolicyDocument("t.yaml", List.of(rule))));
                }
                done.countDown();
            });
        }
        assertThat(done.await(5, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();
    }
}
