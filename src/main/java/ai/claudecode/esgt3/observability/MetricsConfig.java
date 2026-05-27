package ai.claudecode.esgt3.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 비즈니스 커스텀 메트릭 빈 등록. (Section 5.5 메트릭 카탈로그)
 *
 * <p>도메인 Phase에서 추가 메트릭을 등록할 수 있다.
 */
@Configuration
class MetricsConfig {

    @Bean
    Timer policyEvaluationTimer(MeterRegistry registry) {
        return Timer.builder("esg_t3_policy_evaluation_duration_seconds")
            .description("ABAC 정책 평가 소요 시간")
            .register(registry);
    }

    @Bean
    Counter hashChainIntegrityCheckCounter(MeterRegistry registry) {
        return Counter.builder("esg_t3_hash_chain_integrity_check_total")
            .description("AuditLog Hash Chain 무결성 검증 결과")
            .register(registry);
    }
}
