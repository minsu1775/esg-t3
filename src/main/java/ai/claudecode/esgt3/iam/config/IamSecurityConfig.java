package ai.claudecode.esgt3.iam.config;

import ai.claudecode.esgt3.iam.infra.JwtAuthFilter;
import ai.claudecode.esgt3.iam.infra.JwtTokenProvider;
import ai.claudecode.esgt3.iam.infra.RedisTokenBlacklist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Phase 1 - JWT + ABAC 보안 설정.
 *
 * <p>Phase 0의 임시 {@code ActuatorSecurityConfig}를 완전 교체.
 * <ul>
 *   <li>인증 없이 허용: /actuator/health, /info, /prometheus, /api/v1/auth/**, /swagger-ui/**, /v3/api-docs/**</li>
 *   <li>그 외 모든 요청: JWT 인증 + {@code @PreAuthorize}로 ABAC 정책 평가</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class IamSecurityConfig {

    @Bean
    public JwtAuthFilter jwtAuthFilter(JwtTokenProvider tokenProvider, RedisTokenBlacklist blacklist) {
        return new JwtAuthFilter(tokenProvider, blacklist);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info", "/actuator/prometheus").permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
