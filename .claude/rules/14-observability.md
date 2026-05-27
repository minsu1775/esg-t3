# 14. Observability 컨벤션

> **로드 조건**: 항상
> **적용**: 모든 도메인 모듈

## OpenTelemetry Span 네이밍

`<module>.<verb_resource>` (snake_case 동사+명사):
- `iam.evaluate_policy`
- `audit.write_log`
- `ghg.calculate_scope1`
- `evidence.upload_file`
- `vw.create_snapshot`

Span attribute에 반드시 다음 포함 (해당하는 경우):
- `tenant.id`, `entity.id`, `reporting.year`, `subject.user_id`

## 메트릭 네이밍

`esg_t3_<domain>_<metric_name>_<unit>` (snake_case, 단위 접미사):
- `esg_t3_policy_evaluation_duration_seconds` (histogram)
- `esg_t3_emission_calculation_duration_seconds`
- `esg_t3_hash_chain_integrity_check_total` (counter)
- `esg_t3_audit_outbox_dlq_total` (counter)

## MDC 필수 필드

모든 로그에 자동 포함 (`logback-spring.xml`):
- `traceId`, `spanId` (OTel agent)
- `tenantId`, `userId` (TenantContextInterceptor)
- `requestId` (RequestIdFilter)

추가 비즈니스 필드 (도메인별): `entityId`, `reportingYear`, `actionType` 등.

## 금지 사항

- ❌ `System.out.println`, `printStackTrace()` (ArchUnit 차단)
- ❌ 메트릭 이름에 동적 라벨로 카디널리티 폭증 (예: tenant_id를 label로 — `tenant_count` gauge로 분리)
- ❌ 로그에 비밀번호·토큰·개인정보 평문 출력
