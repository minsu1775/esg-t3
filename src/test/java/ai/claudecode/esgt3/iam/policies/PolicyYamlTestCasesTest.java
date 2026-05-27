package ai.claudecode.esgt3.iam.policies;

import ai.claudecode.esgt3.iam.domain.*;
import ai.claudecode.esgt3.iam.infra.PolicyYamlLoader;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 정책 YAML 파일의 {@code tests:} 섹션을 JUnit 동적 테스트로 자동 실행한다.
 *
 * <p>Phase 1 DoD: ABAC 정책 단위 테스트 ≥ 20건.
 */
class PolicyYamlTestCasesTest {

    private final PolicyYamlLoader loader = new PolicyYamlLoader();

    @TestFactory
    Stream<DynamicTest> 모든_정책_YAML의_tests_섹션이_통과한다() {
        List<PolicyDocument> docs = loader.loadFromClasspath("policies/iam/");
        var evaluator = new PolicyEvaluator(docs);
        return docs.stream()
            .flatMap(doc -> doc.rules().stream())
            .flatMap(rule -> rule.tests().stream().map(tc -> toDynamicTest(rule, tc, evaluator)));
    }

    private DynamicTest toDynamicTest(PolicyRule rule, PolicyRule.PolicyTestCase tc, PolicyEvaluator evaluator) {
        return DynamicTest.dynamicTest("%s :: %s".formatted(rule.id(), tc.name()), () -> {
            try {
                PolicyContext ctx = buildContext(tc.ctx());
                PolicyDecision actual = evaluator.evaluate(ctx);
                assertThat(actual.effect())
                    .as("규칙 %s, 케이스 %s", rule.id(), tc.name())
                    .isEqualTo(tc.expect());
            } finally {
                System.clearProperty("esg.lockdown.tenants");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private PolicyContext buildContext(Map<String, Object> raw) {
        Map<String, Object> subjectMap = (Map<String, Object>) raw.getOrDefault("subject", Map.of());
        Map<String, Object> resourceMap = (Map<String, Object>) raw.getOrDefault("resource", Map.of());
        Map<String, Object> envMap = (Map<String, Object>) raw.getOrDefault("env", Map.of());
        PolicyAction action = PolicyAction.valueOf(String.valueOf(raw.get("action")));

        UUID tenantId = uuid(subjectMap.get("tenantId"));
        UUID userId = uuid(subjectMap.get("userId"));
        Set<UUID> entities = new HashSet<>();
        Object aei = subjectMap.get("assignedEntityIds");
        if (aei instanceof List<?> list) {
            for (Object id : list) entities.add(uuid(id));
        }
        // verifier.yaml은 assignedEntityIds에 snapshotId 동거 (단일 컬렉션 재사용)
        Object asi = subjectMap.get("assignedSnapshotIds");
        if (asi instanceof List<?> list) {
            for (Object id : list) entities.add(uuid(id));
        }

        var subject = new Subject(userId,
            String.valueOf(subjectMap.get("role")),
            tenantId != null ? tenantId : UUID.fromString("00000000-0000-0000-0000-000000000000"),
            entities, null);

        Map<String, Object> resourceAttrs = new HashMap<>();
        Object rAttrs = resourceMap.get("attributes");
        if (rAttrs instanceof Map<?, ?> m) {
            m.forEach((k, v) -> resourceAttrs.put(String.valueOf(k), v == null ? null : v.toString()));
        }

        // createdBy를 UUID 형태로 보정 (esg-manager-self-approval-prohibition 정책 매칭용)
        if (resourceAttrs.get("createdBy") instanceof String createdBy) {
            resourceAttrs.put("createdBy", uuid(createdBy).toString());
        }
        // entityId는 String → UUID 변환 (contains 매처에서 UUID와 String 모두 equalsCoerced로 비교되지만 일관성)
        if (resourceAttrs.get("entityId") instanceof String entityId) {
            resourceAttrs.put("entityId", uuid(entityId).toString());
        }
        if (resourceAttrs.get("snapshotId") instanceof String snapshotId) {
            resourceAttrs.put("snapshotId", uuid(snapshotId).toString());
        }

        var resource = Resource.of(
            String.valueOf(resourceMap.get("type")),
            uuid(resourceMap.get("tenantId")),
            resourceAttrs
        );

        // env.lockedTenantIds 처리: emergency-lockdown 정책이 System property에서 읽음
        if (!envMap.isEmpty()) {
            Object locked = envMap.get("lockedTenantIds");
            if (locked instanceof List<?> list) {
                String csv = String.join(",", list.stream().map(o -> uuid(o).toString()).toList());
                System.setProperty("esg.lockdown.tenants", csv);
            }
        }

        var env = new PolicyContext.Environment(Instant.now(), null, false);
        return new PolicyContext(subject, resource, action, env);
    }

    private static UUID uuid(Object v) {
        if (v == null) return null;
        String s = String.valueOf(v);
        try { return UUID.fromString(s); }
        catch (IllegalArgumentException e) {
            return UUID.nameUUIDFromBytes(s.getBytes());
        }
    }
}
