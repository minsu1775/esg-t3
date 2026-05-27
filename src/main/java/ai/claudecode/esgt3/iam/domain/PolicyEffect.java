package ai.claudecode.esgt3.iam.domain;

/**
 * 정책 평가 결과 효과. design.md §3.4.
 *
 * <p>NOT_APPLICABLE은 매칭된 정책이 없는 경우. DENY-default 원칙에 따라
 * 외부 호출 측({@code PolicyDecision.isAllowed()})에서는 false로 취급된다.
 */
public enum PolicyEffect {
    PERMIT,
    DENY,
    NOT_APPLICABLE
}
