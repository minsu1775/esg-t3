package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.iam.domain.PolicyDocument;
import ai.claudecode.esgt3.iam.domain.PolicyEffect;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyYamlLoaderTest {

    @Test
    void resources_policies_iam_하위_YAML을_모두_로드한다() {
        var loader = new PolicyYamlLoader();
        List<PolicyDocument> docs = loader.loadFromClasspath("policies/iam/");
        assertThat(docs).hasSizeGreaterThanOrEqualTo(6);
    }

    @Test
    void esg_manager_yaml의_세_규칙을_파싱한다() {
        var loader = new PolicyYamlLoader();
        var doc = loader.loadFromResource(new ClassPathResource("policies/iam/esg-manager.yaml"));
        assertThat(doc.rules()).hasSize(3);
        assertThat(doc.rules().get(0).id()).isEqualTo("esg-manager-write-own-entity");
        assertThat(doc.rules().get(1).effect()).isEqualTo(PolicyEffect.DENY);
        assertThat(doc.rules().get(1).priority()).isEqualTo(100);
    }

    @Test
    void emergency_lockdown_yaml의_priority가_250() {
        var loader = new PolicyYamlLoader();
        var doc = loader.loadFromResource(new ClassPathResource("policies/iam/emergency-lockdown.yaml"));
        assertThat(doc.rules()).singleElement()
            .satisfies(r -> {
                assertThat(r.priority()).isEqualTo(250);
                assertThat(r.effect()).isEqualTo(PolicyEffect.DENY);
            });
    }
}
