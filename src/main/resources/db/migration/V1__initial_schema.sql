-- esg-t3 V1: 테넌트 + 공시 일정 메타테이블 + Spring Modulith event_publication
-- 핵심 도메인 테이블은 Phase 1~6에서 추가된다.

-- 테넌트
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    business_number VARCHAR(20),
    country_code CHAR(2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE (business_number)
);

COMMENT ON TABLE tenants IS '테넌트 - 멀티테넌트 환경의 최상위 격리 단위. RLS 정책의 기준.';

-- 공시 일정 메타테이블 (의무화 일정 — 코드 하드코딩 금지)
CREATE TABLE disclosure_schedules (
    id BIGSERIAL PRIMARY KEY,
    framework VARCHAR(32) NOT NULL,            -- KSSB_1, KSSB_2, ISSB_S1, ISSB_S2, CSRD
    company_tier VARCHAR(64) NOT NULL,         -- ASSET_30T_PLUS, ASSET_10T_PLUS, KOSPI_ALL
    fiscal_year_start INT NOT NULL,            -- 최초 의무 적용 회계연도
    scope3_mandatory_year INT,                 -- Scope 3 의무 시작 연도 (NULL 가능)
    note TEXT,
    UNIQUE (framework, company_tier)
);

COMMENT ON TABLE disclosure_schedules IS 'KSSB/IFRS 의무 공시 일정 메타 - regulatory.md 검토 후 수동 갱신';

-- Spring Modulith 2.0 event_publication (esg-t2 학습 L-P0-02)
CREATE TABLE event_publication (
    id UUID NOT NULL,
    listener_id VARCHAR(512) NOT NULL,
    event_type VARCHAR(512) NOT NULL,
    serialized_event TEXT NOT NULL,
    publication_date TIMESTAMP WITH TIME ZONE NOT NULL,
    completion_date TIMESTAMP WITH TIME ZONE,
    status VARCHAR(36),
    last_resubmission_date TIMESTAMP WITH TIME ZONE,
    PRIMARY KEY (id)
);

CREATE INDEX event_publication_serialized_event_hash_listener_id_idx
    ON event_publication (listener_id, event_type);
