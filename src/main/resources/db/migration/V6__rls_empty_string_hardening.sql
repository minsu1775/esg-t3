-- V6: RLS 정책 빈 문자열 방어 강화 (L-P1-10, Phase 1 Task 23에서 발견)
-- 사유: current_setting('app.current_tenant_id', true)가 빈 문자열('')을 반환하면
--       ''::uuid 캐스팅이 "invalid input syntax for type uuid" 예외를 던진다.
--       HikariCP 커넥션 풀 재사용 시 이전 세션 상태가 누출될 수 있으므로
--       NULLIF(..., '')로 빈 문자열을 NULL로 변환 → 테넌트 미설정 = 전체 차단(fail-closed).

-- users
DROP POLICY IF EXISTS tenant_isolation_users ON users;
CREATE POLICY tenant_isolation_users ON users
    FOR ALL TO app_role
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid);

-- legal_entities
DROP POLICY IF EXISTS tenant_isolation_legal_entities ON legal_entities;
CREATE POLICY tenant_isolation_legal_entities ON legal_entities
    FOR ALL TO app_role
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid);

-- entity_relationships
DROP POLICY IF EXISTS tenant_isolation_entity_relationships ON entity_relationships;
CREATE POLICY tenant_isolation_entity_relationships ON entity_relationships
    FOR ALL TO app_role
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid);

-- policy_decisions
DROP POLICY IF EXISTS tenant_isolation_policy_decisions ON policy_decisions;
CREATE POLICY tenant_isolation_policy_decisions ON policy_decisions
    FOR ALL TO app_role
    USING (tenant_id = NULLIF(current_setting('app.current_tenant_id', true), '')::uuid);
