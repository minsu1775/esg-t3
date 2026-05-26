-- esg-t3 V2: KSSB 1/2, ISSB S1/S2, CSRD 의무 공시 일정 초기 데이터
-- 출처: docs/regulatory.md (esg-t2 계승)

INSERT INTO disclosure_schedules (framework, company_tier, fiscal_year_start, scope3_mandatory_year, note) VALUES
    ('KSSB_2', 'ASSET_30T_PLUS', 2028, 2031, '연결자산 30조원 이상 - 2028 FY부터 의무'),
    ('KSSB_2', 'ASSET_10T_PLUS', 2029, 2032, '연결자산 10조원 이상'),
    ('KSSB_2', 'KOSPI_ALL',      2030, 2033, '코스피 전체'),
    ('ISSB_S2', 'GLOBAL_VOLUNTARY', 2024, 2027, 'IFRS S2 - 자발 채택 권고'),
    ('KSSB_1', 'KOSPI_VOLUNTARY',   2028, NULL, 'KSSB 1 일반 지속가능성 - 자발 권고'),
    ('CSRD', 'EU_LARGE',           2025, 2026, 'CSRD - EU 진출 기업 참조');
