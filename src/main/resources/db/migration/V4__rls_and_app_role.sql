-- V4: app_role 데이터베이스 롤 + RLS 정책
-- app_role: 애플리케이션 연결 전용 PostgreSQL 롤. 운영 환경에서는 이 롤로 접속하여
--           RLS·REVOKE 권한이 강제된다. 로컬/테스트는 superuser로 접속하여 RLS BYPASS.
-- 참조: design.md §7.6, .claude/rules/03-security-rls.md, esg-t2 L-P1-03

-- 1) app_role 생성 (이미 존재 시 무시)
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM pg_roles WHERE rolname = 'app_role') THEN
        CREATE ROLE app_role NOLOGIN;
    END IF;
END$$;

GRANT USAGE ON SCHEMA public TO app_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO app_role;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO app_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO app_role;
ALTER DEFAULT PRIVILEGES IN SCHEMA public
    GRANT USAGE ON SEQUENCES TO app_role;

-- 2) RLS 활성화 + 정책 — users
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_users ON users
    FOR ALL TO app_role
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

-- 3) RLS 활성화 + 정책 — legal_entities
ALTER TABLE legal_entities ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_legal_entities ON legal_entities
    FOR ALL TO app_role
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

-- 4) RLS 활성화 + 정책 — entity_relationships
ALTER TABLE entity_relationships ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation_entity_relationships ON entity_relationships
    FOR ALL TO app_role
    USING (tenant_id = current_setting('app.current_tenant_id', true)::uuid);

-- 주의:
-- - tenants/emission_factors(M+1)/disclosure_schedules는 글로벌 마스터로 RLS 제외.
-- - audit_logs/policy_decisions의 RLS는 각 테이블 생성 시점(Task 11, Phase 2)에 함께 적용.
-- - current_setting의 두 번째 인자 'true' 사용 사유:
--   application.current_tenant_id가 미설정 시 NULL 반환(예외 회피).
--   → 정책은 NULL과 비교하여 false 반환 → 모든 행 차단(보안 기본값).
-- - esg-t2 L-P1-03: 운영 코드에서 set_config 호출 시 반드시 PreparedStatement 자리표시자 '?'로
--   파라미터 바인딩(문자열 연결은 SQL Injection). TenantContextInterceptor(Task 13)에서 강제.
