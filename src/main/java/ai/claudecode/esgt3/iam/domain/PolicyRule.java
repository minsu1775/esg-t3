package ai.claudecode.esgt3.iam.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 단일 ABAC 정책 규칙 - YAML 파싱 결과.
 *
 * @param id 정책 고유 식별자 ({@code <module>-<verb>-<resource>} 컨벤션)
 * @param description 한국어 한 줄 사유
 * @param effect PERMIT 또는 DENY
 * @param priority 0~250. 250: emergency-lockdown, 200: disclosed-data-immutability, 100: SoD
 * @param when 매칭 조건 - 키는 점 표기법 ({@code subject.role}, {@code resource.approvalState} 등)
 *             값은 String(평등), List(in), Map(contains/in 등 매처)
 * @param tests YAML {@code tests:} 섹션 - 단위 테스트 케이스
 */
public record PolicyRule(
    String id,
    String description,
    PolicyEffect effect,
    int priority,
    Map<String, Object> when,
    List<PolicyTestCase> tests
) {
    public PolicyRule {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(effect, "effect");
        if (effect == PolicyEffect.NOT_APPLICABLE) {
            throw new IllegalArgumentException("정책 규칙의 effect는 PERMIT 또는 DENY만 가능");
        }
        when = when == null ? Map.of() : Map.copyOf(when);
        tests = tests == null ? List.of() : List.copyOf(tests);
    }

    /**
     * YAML {@code tests:} 항목 1개.
     *
     * @param name 케이스 이름
     * @param ctx 평가 입력 (subject·resource·action 키)
     * @param expect 기대 결과 (PERMIT/DENY)
     */
    public record PolicyTestCase(String name, Map<String, Object> ctx, PolicyEffect expect) {}
}
