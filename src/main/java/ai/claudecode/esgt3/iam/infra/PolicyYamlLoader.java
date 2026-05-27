package ai.claudecode.esgt3.iam.infra;

import ai.claudecode.esgt3.iam.domain.PolicyDocument;
import ai.claudecode.esgt3.iam.domain.PolicyEffect;
import ai.claudecode.esgt3.iam.domain.PolicyRule;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 정책 YAML 파일 → {@link PolicyDocument} 변환.
 *
 * <p>{@code classpath:policies/iam/*.yaml} 패턴으로 다중 로드.
 * SnakeYAML 사용 (Spring Boot 동봉).
 */
public class PolicyYamlLoader {

    private final Yaml yaml = new Yaml();
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public List<PolicyDocument> loadFromClasspath(String directory) {
        try {
            String pattern = directory.endsWith("/") ? directory : directory + "/";
            Resource[] resources = resolver.getResources("classpath*:" + pattern + "*.yaml");
            List<PolicyDocument> docs = new ArrayList<>(resources.length);
            for (Resource r : resources) {
                docs.add(loadFromResource(r));
            }
            return List.copyOf(docs);
        } catch (IOException e) {
            throw new PolicyConfigurationException("POLICY_LOAD_FAILED", "정책 YAML 로드 실패: " + directory, e);
        }
    }

    public PolicyDocument loadFromResource(Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            Map<String, Object> parsed = yaml.load(in);
            return parseDocument(resource.getDescription(), parsed);
        } catch (IOException e) {
            throw new PolicyConfigurationException("POLICY_PARSE_FAILED",
                "정책 YAML 파싱 실패: " + resource.getDescription(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private PolicyDocument parseDocument(String source, Map<String, Object> root) {
        if (root == null || !(root.get("policies") instanceof List<?> policies)) {
            return new PolicyDocument(source, List.of());
        }
        List<PolicyRule> rules = new ArrayList<>(policies.size());
        for (Object item : policies) {
            if (item instanceof Map<?, ?> m) {
                rules.add(toRule((Map<String, Object>) m));
            }
        }
        return new PolicyDocument(source, rules);
    }

    @SuppressWarnings("unchecked")
    private PolicyRule toRule(Map<String, Object> m) {
        String id = required(m, "id");
        String description = String.valueOf(m.getOrDefault("description", ""));
        PolicyEffect effect = PolicyEffect.valueOf(required(m, "effect"));
        int priority = m.get("priority") instanceof Number n ? n.intValue() : 0;
        Map<String, Object> when = (Map<String, Object>) m.getOrDefault("when", Map.of());
        List<Map<String, Object>> testsRaw = (List<Map<String, Object>>) m.getOrDefault("tests", List.of());
        List<PolicyRule.PolicyTestCase> tests = new ArrayList<>(testsRaw.size());
        for (Map<String, Object> t : testsRaw) {
            String name = String.valueOf(t.get("name"));
            Map<String, Object> ctx = (Map<String, Object>) t.getOrDefault("ctx", Map.of());
            PolicyEffect expect = PolicyEffect.valueOf(String.valueOf(t.get("expect")));
            tests.add(new PolicyRule.PolicyTestCase(name, ctx, expect));
        }
        return new PolicyRule(id, description, effect, priority, when, tests);
    }

    private static String required(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) {
            throw new PolicyConfigurationException("POLICY_FIELD_MISSING", "필수 필드 누락: " + key);
        }
        return String.valueOf(v);
    }
}
