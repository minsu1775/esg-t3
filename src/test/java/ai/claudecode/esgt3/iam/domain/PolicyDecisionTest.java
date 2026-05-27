package ai.claudecode.esgt3.iam.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyDecisionTest {

    @Test
    void permit_팩토리는_PERMIT_효과를_생성한다() {
        var d = PolicyDecision.permit("esg-manager-write-own-entity", "담당 법인 매칭");
        assertThat(d.effect()).isEqualTo(PolicyEffect.PERMIT);
        assertThat(d.policyId()).isEqualTo("esg-manager-write-own-entity");
        assertThat(d.reason()).isEqualTo("담당 법인 매칭");
    }

    @Test
    void deny_팩토리는_DENY_효과를_생성한다() {
        var d = PolicyDecision.deny("disclosed-data-immutability", "공시 완료 데이터 수정 시도");
        assertThat(d.effect()).isEqualTo(PolicyEffect.DENY);
    }

    @Test
    void notApplicable_팩토리는_정책ID_없이_생성된다() {
        var d = PolicyDecision.notApplicable();
        assertThat(d.effect()).isEqualTo(PolicyEffect.NOT_APPLICABLE);
        assertThat(d.policyId()).isNull();
    }

    @Test
    void permit_시_policyId가_null이면_예외() {
        assertThatThrownBy(() -> PolicyDecision.permit(null, "사유"))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void isAllowed는_PERMIT일_때만_true() {
        assertThat(PolicyDecision.permit("p", "r").isAllowed()).isTrue();
        assertThat(PolicyDecision.deny("p", "r").isAllowed()).isFalse();
        assertThat(PolicyDecision.notApplicable().isAllowed()).isFalse();
    }
}
