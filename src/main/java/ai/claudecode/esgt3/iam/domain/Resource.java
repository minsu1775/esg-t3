package ai.claudecode.esgt3.iam.domain;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * ABAC 자원 - 보호 대상 리소스의 속성 묶음.
 *
 * <p>{@code attributes}는 정책 YAML의 {@code resource.*} 매칭 키와 대응한다.
 * 예: {@code approvalState}, {@code sensitivity}, {@code createdBy}.
 *
 * <p>호출 측이 prefetch한 결과로 채워야 한다 (PolicyEvaluator는 DB 접근 안 함).
 */
public record Resource(
    String type,
    UUID tenantId,
    Map<String, Object> attributes
) {
    public Resource {
        Objects.requireNonNull(type, "type");
        if (type.isBlank()) {
            throw new IllegalArgumentException("type은 비어 있을 수 없습니다");
        }
        attributes = attributes == null ? Map.of() : Collections.unmodifiableMap(attributes);
    }

    /**
     * tenantId·attributes가 없을 때 사용하는 정적 팩토리.
     */
    public static Resource of(String type, UUID tenantId, Map<String, Object> attributes) {
        return new Resource(type, tenantId, attributes);
    }
}
