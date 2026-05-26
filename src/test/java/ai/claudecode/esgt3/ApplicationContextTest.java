package ai.claudecode.esgt3;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationContextTest extends AbstractIntegrationTest {

    @Test
    void 스프링_컨텍스트가_정상적으로_로드된다() {
        // SpringBootTest 부트스트랩 자체가 검증.
        // AbstractIntegrationTest(Task 7)가 Testcontainers PostgreSQL 18 컨테이너를 시작한다.
    }
}
