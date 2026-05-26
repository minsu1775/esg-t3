package ai.claudecode.esgt3;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * 모든 통합 테스트의 부모 클래스.
 *
 * <p>esg-t2/esg-t1 학습 반영:
 * <ul>
 *   <li>L-P0-03: Docker Desktop 29.x + TestContainers 호환 (DOCKER_HOST 패스스루는 build.gradle.kts)</li>
 *   <li>L-P0-04: PostgreSQL JDBC URL 교체 시 driver-class-name도 오버라이드</li>
 *   <li>L-P0-05: H2 shutdown WARN은 무해 (테스트는 PostgreSQL 18 사용)</li>
 *   <li>L-P0-16: {@code static { POSTGRES.start(); }} 패턴 — @DynamicPropertySource 전 컨테이너 시작 보장</li>
 * </ul>
 */
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("esgt3_test")
            .withUsername("esgt3_test")
            .withPassword("esgt3-test-pw")
            .withReuse(false);

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.jpa.database-platform",
            () -> "org.hibernate.dialect.PostgreSQLDialect");
    }
}
