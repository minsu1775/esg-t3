package ai.claudecode.esgt3.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenTelemetry SDK 설정.
 *
 * <p>Spring Boot 4 OTel Starter가 자동 설정을 대부분 제공한다.
 * 도메인 수동 계측에서 사용할 Tracer 빈을 노출한다.
 */
@Configuration
class OtelConfig {

    @Bean
    Tracer esgT3Tracer(OpenTelemetry openTelemetry,
                       @Value("${spring.application.name:esg-t3}") String serviceName) {
        return openTelemetry.getTracer(serviceName);
    }
}
