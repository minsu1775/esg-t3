package ai.claudecode.esgt3.iam.config;

import ai.claudecode.esgt3.iam.domain.PolicyDocument;
import ai.claudecode.esgt3.iam.domain.PolicyRegistry;
import ai.claudecode.esgt3.iam.infra.PolicyYamlLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 정책 엔진 빈 와이어링 - 도메인 객체를 Spring 빈으로 노출.
 *
 * <p>도메인 패키지 자체는 Spring 의존 0. config 패키지가 bridge 역할.
 */
@Configuration
public class PolicyEngineConfig {

    @Bean
    public PolicyYamlLoader policyYamlLoader() {
        return new PolicyYamlLoader();
    }

    @Bean
    public PolicyRegistry policyRegistry(
        PolicyYamlLoader loader,
        @Value("${esg.policy.directory:policies/iam/}") String directory
    ) {
        PolicyRegistry registry = new PolicyRegistry();
        List<PolicyDocument> initial = loader.loadFromClasspath(directory);
        registry.replace(initial);
        return registry;
    }
}
