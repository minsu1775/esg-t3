-- V5: 정책 평가 결과 기록 - 모든 PERMIT/DENY를 영구 저장
-- Phase 2에서 audit_logs 테이블과 hash chain·trace_id 연계 예정.
-- 참조: design.md §4.3 policy_decisions

CREATE TABLE policy_decisions (
    id BIGSERIAL PRIMARY KEY,
    tenant_id UUID NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    subject_user_id UUID,
    subject_role VARCHAR(32),
    action VARCHAR(32) NOT NULL,
    resource_type VARCHAR(64) NOT NULL,
    resource_id VARCHAR(128),
    effect VARCHAR(16) NOT NULL,
    policy_id VARCHAR(128),
    reason TEXT,
    CHECK (effect IN ('PERMIT', 'DENY', 'NOT_APPLICABLE'))
);

COMMENT ON TABLE policy_decisions IS '정책 평가 결과 - Phase 2에서 audit_logs로 마이그레이션될 예정';

CREATE INDEX idx_policy_decisions_tenant_time
    ON policy_decisions (tenant_id, occurred_at DESC);

ALTER TABLE policy_decisions ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_policy_decisions ON policy_decisions
    FOR ALL TO app_role
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

-- INSERT-only 권한 박탈은 Phase 2에서 audit_logs와 함께 적용.
