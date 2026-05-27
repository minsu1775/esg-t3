-- V3: iam 테이블(users, user_roles, user_entity_assignments) + entity 기본 테이블
-- 사유: user_entity_assignments.entity_id가 legal_entities(id)를 FK로 참조하므로 함께 생성.
--       entity 도메인 비즈니스 로직은 Phase 5에서 본격 구현.
-- 번호 사유: Phase 0에서 V1(initial_schema) + V2(disclosure_schedule_seed)을 이미 생성했으므로
--           Phase 1은 V3부터 시작. plan.md "Task 1 정정 (2026-05-27)" 참조.
-- 참조: design.md §7.3 iam, entity DDL

CREATE TABLE legal_entities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    name VARCHAR(255) NOT NULL,
    country_code CHAR(2) NOT NULL,
    business_number VARCHAR(20),
    reporting_currency CHAR(3) NOT NULL,
    fiscal_year_start_month SMALLINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (fiscal_year_start_month BETWEEN 1 AND 12)
);

COMMENT ON TABLE legal_entities IS '법인 - 테넌트 내 다법인 구조. Phase 5에서 entity 모듈 본격 구현.';

CREATE TABLE entity_relationships (
    parent_id UUID NOT NULL REFERENCES legal_entities(id),
    child_id UUID NOT NULL REFERENCES legal_entities(id),
    tenant_id UUID NOT NULL,
    ownership_ratio NUMERIC(7, 6) NOT NULL,
    consolidation_method VARCHAR(32) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE,
    PRIMARY KEY (parent_id, child_id, effective_from),
    CHECK (ownership_ratio BETWEEN 0 AND 1),
    CHECK (parent_id <> child_id),
    CHECK (consolidation_method IN ('EQUITY','OPERATIONAL_CONTROL'))
);

COMMENT ON TABLE entity_relationships IS '법인 간 지분 관계 - Phase 5 연결 GHG 계산 기반.';

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    email VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    mfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (tenant_id, email)
);

COMMENT ON TABLE users IS '사용자 - tenant_id 기반 RLS 적용(V4에서 활성화).';

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role VARCHAR(32) NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    granted_by UUID,
    PRIMARY KEY (user_id, role),
    CHECK (role IN ('SUPER_ADMIN','TENANT_ADMIN','ESG_MANAGER','ESG_VIEWER','VERIFIER','SUPPLIER'))
);

COMMENT ON TABLE user_roles IS '사용자-역할 매핑 - 6역할 enum. ABAC 평가의 subject.role 원천.';

CREATE TABLE user_entity_assignments (
    user_id UUID NOT NULL REFERENCES users(id),
    entity_id UUID NOT NULL REFERENCES legal_entities(id),
    assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, entity_id)
);

COMMENT ON TABLE user_entity_assignments IS 'ABAC subject.assignedEntityIds 원천 - ESG_MANAGER가 담당하는 법인 한정.';

CREATE INDEX idx_users_tenant_email ON users (tenant_id, email);
CREATE INDEX idx_legal_entities_tenant ON legal_entities (tenant_id);
CREATE INDEX idx_user_entity_assignments_user ON user_entity_assignments (user_id);
