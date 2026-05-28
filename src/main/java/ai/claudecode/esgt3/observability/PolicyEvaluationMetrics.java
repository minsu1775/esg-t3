package ai.claudecode.esgt3.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * ABAC 정책 평가 메트릭 - design.md §5.5.
 *
 * <p>{@code esg_t3_policy_evaluation_total{effect,role,policy_id}} counter
 * + {@code esg_t3_policy_evaluation_duration_seconds{effect,role}} histogram.
 */
@Component
public class PolicyEvaluationMetrics {

    private final MeterRegistry registry;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    public PolicyEvaluationMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordEvaluation(String effect, String role, String policyId, Duration duration) {
        counter(effect, role, policyId).increment();
        timer(effect, role).record(duration);
    }

    private Counter counter(String effect, String role, String policyId) {
        String resolvedPolicyId = policyId == null ? "_none" : policyId;
        String key = effect + "|" + role + "|" + resolvedPolicyId;
        return counters.computeIfAbsent(key, k -> Counter.builder("esg_t3_policy_evaluation_total")
            .tag("effect", effect)
            .tag("role", role)
            .tag("policy_id", resolvedPolicyId)
            .register(registry));
    }

    private Timer timer(String effect, String role) {
        String key = effect + "|" + role;
        return timers.computeIfAbsent(key, k -> Timer.builder("esg_t3_policy_evaluation_duration_seconds")
            .tag("effect", effect)
            .tag("role", role)
            .publishPercentileHistogram()
            .register(registry));
    }
}
