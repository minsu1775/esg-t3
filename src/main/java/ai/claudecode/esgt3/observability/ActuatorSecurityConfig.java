package ai.claudecode.esgt3.observability;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Phase 0 임시 Security 설정.
 *
 * <p>health/info/prometheus 엔드포인트는 인증 없이 허용,
 * 나머지는 인증 필요 + HTTP Basic.
 *
 * <p>⚠️ Phase 1에서 JWT + ABAC 정책 엔진으로 완전 교체될 임시 설정.
 */
@Configuration
class ActuatorSecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(b -> {});
        return http.build();
    }
}
