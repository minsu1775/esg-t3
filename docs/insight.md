# esg-t3 — 개발 인사이트 (Insight)

> 각 Phase 개발 과정의 교훈, 설계 결정의 이유, 예상치 못한 복잡성을 기록.
> esg-t2 insight.md의 L-0-01~L-0-16 + L-P0-01~ 학습은 plan.md의 [예방] 항목으로 흡수 + 본 문서의 L3-P0-XX로 확장.

---

## esg-t1·esg-t2 계승 교훈

esg-t2/docs/insight.md의 L-0-01~L-0-16, L-P0-01~ 모든 항목 그대로 유효. 각 Phase의 plan.md `[예방]` 체크리스트로 흡수됨.

핵심 항목 요약:
- L-0-01: @Auditable AOP 누락 방지 (Phase 2)
- L-0-04: synchronized + @Transactional 금지 → PESSIMISTIC_WRITE (Phase 2)
- L-0-06: DigestInputStream으로 SHA-256 + 업로드 I/O 1회 처리 (Phase 3)
- L-0-08: Hash Chain canonicalPayload() 단일 메서드 (Phase 2)
- L-0-09: factorAt(date) 재현성 (Phase 3)
- L-0-11: @Async + @Transactional 동일 메서드 금지 (Phase 2~)
- L-0-12: BigDecimal 전용 (Phase 3~)
- L-0-13: CSV 행별 REQUIRES_NEW (Phase 3)
- L-0-14: Append-only Repository 마커 인터페이스 (Phase 2)
- L-0-15: Scheduler zone=Asia/Seoul + @ConditionalOnProperty (Phase 2~)

상세는 esg-t2/docs/insight.md 참조.

---

## Phase 0: 인프라 + 거버넌스 골격 (esg-t3 신규 학습)

> 본 섹션은 Phase 0 실행 로그(`docs/superpowers/plans/2026-05-26-esg-t3-phase0-execution-log.md` §4)의 L3-P0-01~13 항목을 정식 누적.

### L3-P0-01: OTel BOM 사용 (Task 3)

**현상**: `io.opentelemetry.instrumentation:opentelemetry-spring-boot-starter:2.10.0-alpha` 직접 버전 핀 → Maven Central 해결 실패.

**교훈**: OTel `*-instrumentation` 산출물은 stable BOM 없이 alpha 채널만 게시. 직접 핀 대신 `opentelemetry-instrumentation-bom-alpha` 사용. esg-t3는 `2.28.1-alpha` 채택.

### L3-P0-02: EsgException null 가드 (Task 3 review)

**현상**: `errorCode`는 외부 API 응답(`ErrorResponse.error`)으로 노출되므로 null 진입 차단 필요.

**교훈**: `EsgException` 생성자에서 `Objects.requireNonNull(errorCode)` 의무.

### L3-P0-03: DomainEvent nullability 명문화 (Task 3 review)

**현상**: 마커 인터페이스 반환값(`tenantId`, `occurredAt`, `idempotencyKey`)이 null이면 Outbox 멱등성·RLS 격리가 무너짐.

**교훈**: Javadoc에 null 금지 명문화 + publisher 책임. ArchUnit은 런타임 null 검증 불가 → 단위 테스트 보강.

### L3-P0-04: Postgres 18 docker 마운트 경로 변경 (Task 4)

**현상**: `./data/postgres:/var/lib/postgresql/data` 마운트 시 Postgres 18 이미지가 `pg_ctlcluster` 호환 레이아웃을 요구하여 무한 재시작.

**교훈**: `/var/lib/postgresql/data` 대신 `/var/lib/postgresql` 마운트. 컨테이너 내부에서 `/var/lib/postgresql/18/docker` 자동 생성.

### L3-P0-05: docker-compose version 필드 obsolete (Task 4)

**교훈**: Compose v2에서 `version: "3.9"` 필드 제거. 매 실행 시 경고만 출력.

### L3-P0-06: docker-credential-desktop PATH 의존 (Task 4~)

**현상**: PATH에 docker bin 없으면 이미지 pull 시 `docker-credential-desktop` 호출 실패.

**교훈**: 세션 PATH에 `C:\Program Files\Docker\Docker\resources\bin` 추가 필수. CLAUDE.md 운영 메모에 명시.

### L3-P0-07: Spring Modulith 2.0.0 completion_attempts 컬럼 (Task 7)

**현상**: `JpaEventPublication`이 2.0.0에서 `completionAttempts` 필드 신규 도입. esg-t2 V1 스키마(1.x)를 그대로 복사하면 `SchemaValidation: missing column [completion_attempts]`로 첫 통합 테스트 실패.

**교훈**: Spring Modulith 버전 업그레이드 시 `event_publication` 스키마 변경사항 `javap -p`로 직접 확인. esg-t3 V1에 `completion_attempts INTEGER NOT NULL DEFAULT 0` 포함. (esg-t2 L-P0-02의 2.x 확장)

### L3-P0-08: ArchUnit 1.3.0 failOnEmptyShould (Task 8)

**현상**: ArchUnit 1.3.0의 `failOnEmptyShould` 기본 활성. `.that()` 절이 0개 매칭 시 vacuous pass 아닌 `AssertionError`.

**교훈**: 도메인 코드 없는 골격 단계 규칙에 `.allowEmptyShould(true)` 추가. 도메인 클래스 추가 후 자동 진짜 검증.

### L3-P0-09: Modulith Documenter 산출 디렉터리 (Task 9)

**교훈**: Gradle 빌드는 `build/spring-modulith-docs/` (Maven `target/`이 아님). `.gitignore`의 `build/`로 자동 제외.

### L3-P0-10: 테스트 환경 Redis health check (Task 10)

**현상**: 테스트 환경에 Redis 없으면 `/actuator/health` 503 반환.

**교훈**: `application-test.yml`에 `management.health.redis.enabled: false`. 운영 환경 영향 없음.

### L3-P0-11: observability cross-cutting의 모듈 자동 인식 (Task 10)

**현상**: Spring Modulith가 `observability/` 패키지를 자동으로 9번째 모듈로 인식 (L-P0-06 재확인).

**교훈**: design.md §2.4는 "cross-cutting"으로 설명했으나 실제로는 모듈로 동작. 받아들이거나 `shared/observability/` 하위로 이동. esg-t3는 9개 모듈로 받아들임.

### L3-P0-12: Tempo/Loki 컨테이너 readiness 지연 (Task 12)

**교훈**: Tempo 2.7.0 / Loki 3.4.0은 `up -d` 후 readiness까지 30-40초. CI/스모크 스크립트의 wait 시간 조정 필요.

### L3-P0-13: OTel Collector loki exporter deprecated (Task 13)

**교훈**: contrib 0.115.1에서 `loki` exporter는 deprecated. 차후 `otlphttp` exporter로 교체 (Loki OTLP 직접 수신 지원).

---

## Phase 1: Identity & ABAC (esg-t3 신규 학습)

> 본 섹션은 Phase 1 실행 로그(`docs/superpowers/plans/2026-05-27-esg-t3-phase1-execution-log.md` §5)의 L-P1-01~10 항목을 정식 누적.
> Phase 1은 23개 task / 단위·통합 테스트 87건으로 완료.

### L-P1-01: Spring Modulith @NamedInterface 모듈 경계

**현상**: shared 모듈의 `exception`/`event`/`web`/`tenant` 패키지가 각각 `@NamedInterface`로 선언되어 있어, iam의 `allowedDependencies = { "shared" }`만으로는 ModularityTest 실패.

**교훈**: NamedInterface가 선언된 하위 패키지를 의존하려면 `shared::exception` 형식으로 명시해야 한다. 또한 observability를 cross-cutting으로 의존할 때는 `observability`를 allowedDependencies에 추가. 자세한 경계는 ADR-014 참조.

### L-P1-02: AccessDeniedException → 403 명시 핸들러 필수

**현상**: `@PreAuthorize` 거부 시 GlobalExceptionHandler에 핸들러가 없으면 Spring 기본 500 반환.

**교훈**: `@ExceptionHandler(AccessDeniedException.class)` → 403, `AuthenticationException` → 401 명시 등록. esg-t2 L-P1-02 재확인.

### L-P1-03: set_config 파라미터 바인딩 (SQL Injection 방어)

**교훈**: `set_config('app.current_tenant_id', ?, true)` 파라미터 바인딩 필수. 문자열 연결 금지. ADR-016 참조.

### L-P1-04: JwtTokenProvider 빈 캐싱

**교훈**: SecretKey 객체는 빈 생성 시 1회만 초기화. 매 토큰 발행 시 `Keys.hmacShaKeyFor()` 재호출 금지 (성능).

### L-P1-05: JWT secret 미설정 시 부팅 차단

**교훈**: `ESG_JWT_SECRET` 환경변수 누락 또는 32B 미만이면 생성자에서 `IllegalStateException` → 부팅 차단 (운영 안전, fail-fast).

### L-P1-06: 마이그레이션 번호 충돌 (Task 1)

**현상**: Phase 1 plan이 V2부터 시작한다고 가정했으나 Phase 0가 이미 `V2__disclosure_schedule_seed.sql` 점유 → V3/V4/V5로 시프트.

**교훈**: plan에서 마이그레이션 추가 전 `ls db/migration/` 출력을 인용해 번호 충돌을 사전 확인. plan self-review 체크리스트에 마이그레이션 번호 점검 단계 추가.

### L-P1-07: set_config는 함수라 queryForObject 사용 (Task 11)

**현상**: `jdbcTemplate.update("SELECT set_config(...)", ...)`는 `DataIntegrityViolationException("A result was returned when none was expected")`.

**교훈**: `set_config(...)`는 SELECT 함수로 결과 행을 반환 → `jdbcTemplate.queryForObject("SELECT set_config(...)", String.class, ...)` 사용. 파라미터 바인딩은 그대로.

### L-P1-08: shared 하위 NamedInterface allowedDependencies 누적 (Task 12)

**교훈**: 도메인 모듈이 EsgException(`shared::exception`)·DomainEvent(`shared::event`)·ErrorResponse(`shared::web`)·TenantContext(`shared::tenant`)를 쓸 때마다 해당 NamedInterface를 allowedDependencies에 추가해야 함. Phase 2~ 모든 모듈 package-info에 일괄 정비 필요.

### L-P1-09: Spring Boot 4 @ServiceConnection 별도 의존 (Task 16)

**현상**: `org.springframework.boot.test.autoconfigure...AutoConfigureMockMvc`·`@ServiceConnection`이 esg-t3 의존성에 없음.

**교훈**: MockMvc는 `MockMvcBuilders.webAppContextSetup(context).apply(springSecurity())` 패턴, 컨테이너 연결은 `@DynamicPropertySource` 사용 (AbstractIntegrationTest와 일관). 신규 의존성 회피.

### L-P1-10: RLS 정책 빈 문자열 방어 (Task 23)

**현상**: `current_setting('app.current_tenant_id', true)::uuid`에서 GUC가 빈 문자열 `''`이면 `''::uuid` 캐스팅이 `invalid input syntax for type uuid` 예외. HikariCP 커넥션 풀 재사용으로 이전 세션 상태 누출.

**교훈**: RLS 정책에 `NULLIF(current_setting(...), '')::uuid` 사용 → 빈 문자열을 NULL로 변환해 "테넌트 미설정 = 전체 차단"(fail-closed). Phase 2 이후 모든 RLS 정책에 동일 패턴 적용. (V6 마이그레이션)

---

## Phase 2~8

각 Phase 종료 시 신규 학습 누적 예정.
