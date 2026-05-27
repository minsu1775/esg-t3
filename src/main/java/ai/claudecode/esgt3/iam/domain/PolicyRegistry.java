package ai.claudecode.esgt3.iam.domain;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 정책 캐시 + 핫리로드 가능한 평가자 보유.
 *
 * <p>스레드 안전: {@link AtomicReference}로 평가자를 단일 원자 참조로 유지.
 * {@code replace()} 호출 시 새 평가자가 통째로 교체되며, 읽기 측은 항상 일관된 평가자를 본다.
 *
 * <p>infra의 PolicyHotReloader가 파일 변경 감지 시 {@link #replace(List)} 호출.
 */
public final class PolicyRegistry {

    private final AtomicReference<PolicyEvaluator> evaluatorRef =
        new AtomicReference<>(new PolicyEvaluator(List.of()));

    public PolicyEvaluator evaluator() {
        return evaluatorRef.get();
    }

    public void replace(List<PolicyDocument> documents) {
        Objects.requireNonNull(documents, "documents");
        evaluatorRef.set(new PolicyEvaluator(documents));
    }
}
