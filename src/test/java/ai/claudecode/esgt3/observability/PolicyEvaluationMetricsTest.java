package ai.claudecode.esgt3.observability;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyEvaluationMetricsTest {

    @Test
    void Counter와_Timer가_등록된다() {
        var registry = new SimpleMeterRegistry();
        var metrics = new PolicyEvaluationMetrics(registry);
        metrics.recordEvaluation("PERMIT", "ESG_MANAGER",
            "esg-manager-write-own-entity", Duration.ofMillis(1));

        assertThat(registry.find("esg_t3_policy_evaluation_total")
            .tag("effect", "PERMIT").tag("role", "ESG_MANAGER").counter().count())
            .isEqualTo(1.0);
        assertThat(registry.find("esg_t3_policy_evaluation_duration_seconds")
            .tag("effect", "PERMIT").timer().count())
            .isEqualTo(1L);
    }

    @Test
    void policyId가_null이면_none_태그로_등록() {
        var registry = new SimpleMeterRegistry();
        var metrics = new PolicyEvaluationMetrics(registry);
        metrics.recordEvaluation("NOT_APPLICABLE", "ESG_VIEWER", null, Duration.ofMillis(1));
        assertThat(registry.find("esg_t3_policy_evaluation_total")
            .tag("policy_id", "_none").counter().count())
            .isEqualTo(1.0);
    }

    @Test
    void 같은_라벨_조합은_단일_Counter_재사용() {
        var registry = new SimpleMeterRegistry();
        var metrics = new PolicyEvaluationMetrics(registry);
        for (int i = 0; i < 5; i++) {
            metrics.recordEvaluation("PERMIT", "ESG_MANAGER", "p1", Duration.ofMillis(1));
        }
        assertThat(registry.find("esg_t3_policy_evaluation_total")
            .tag("effect", "PERMIT").tag("role", "ESG_MANAGER").tag("policy_id", "p1")
            .counter().count()).isEqualTo(5.0);
    }
}
