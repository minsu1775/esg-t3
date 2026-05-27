package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.iam.domain.PolicyRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

class PolicyHotReloaderTest {

    @Test
    void 디렉터리에_yaml_추가_시_5초_이내_레지스트리_갱신(@TempDir Path dir) throws Exception {
        var registry = new PolicyRegistry();
        var loader = new PolicyYamlLoader();
        var reloader = new PolicyHotReloader(registry, loader, dir, 200);
        reloader.start();
        try {
            assertThat(registry.evaluator().ruleIdsInEvaluationOrder()).isEmpty();

            Files.writeString(dir.resolve("new.yaml"),
                """
                policies:
                  - id: hot-test
                    description: 핫리로드 테스트
                    effect: PERMIT
                    when:
                      subject.role: ESG_MANAGER
                """);

            await().atMost(5, TimeUnit.SECONDS).until(
                () -> registry.evaluator().ruleIdsInEvaluationOrder().contains("hot-test"));
        } finally {
            reloader.stop();
        }
    }
}
