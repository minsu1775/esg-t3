# Phase 0 실행 로그 (Execution Log)

> **버전**: 진행 중 (Task 1~10 완료 / Task 11 시작 직전)
> **시작일**: 2026-05-26
> **목적**: `subagent-driven-development` 스킬로 실행한 Phase 0의 실제 진행 내역. 사전 계획(plan.md)·설계(design.md)와 별도로 "실제로 어떻게 진행했고 어떤 발견·수정이 있었는가"를 추적.

이 문서는 향후 Phase 1~8에서도 동일한 형태로 누적된다 (`docs/superpowers/plans/YYYY-MM-DD-esg-t3-phaseN-execution-log.md`).

---

## 1. 실행 환경 (사전 확정)

| 항목 | 값 | 비고 |
|---|---|---|
| Java | 25 LTS (Microsoft JDK 25.0.3+9-LTS) | `JAVA_HOME=C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot\` |
| Gradle | 9.4.1 (esg-t2 wrapper 복사) | 시스템 gradle 없음 |
| Docker | Desktop 29.4.3 | `C:\Program Files\Docker\Docker\resources\bin\` |
| Node | 22.16.0 | `C:\Program Files\nodejs\` |
| Git | 2.49.0 windows | core.autocrlf=true |
| Shell | Windows PowerShell + Git Bash (mingw64) | 작업은 Bash 위주 |
| gh CLI | 2.92.0, `minsu1775` 로그인 (keyring) | `C:\Program Files\GitHub CLI\gh.exe` |
| GitHub repo | https://github.com/minsu1775/esg-t3 (public) | gh repo create로 자동 생성, master 브랜치 |

PATH에 java/docker/node/gradle/gh 모두 등록되어 있지 않음 → **절대 경로 또는 gradlew wrapper** 사용.

---

## 2. 진행 방식 결정 사항

### 2.1 사전 문서 결정 (Phase 0 시작 전)

- design.md (8 섹션 + 부록) ✅ 작성 완료
- Phase 0 plan.md (22 task) ✅ 작성 완료
- regulatory.md ✅ esg-t2에서 계승
- prd.md / spec.md / ADR-010~013 / .claude/rules — **작성 안 함**, 이유: design.md와 중복(80%+), Phase 별로 자연스럽게 채워질 예정

### 2.2 실행 전략 (사용자 선택)

- **서브에이전트 실행** (`superpowers:subagent-driven-development` 스킬)
- review 전략: **"Task 구분 적용"**
  - 단순 설정/문서 task: spec review만 (Task 1, 2, 4, 5, 15, 19, 21)
  - 중간 복잡도 task: spec + 간소 code review (Task 12~14, 16~18, 20, 22)
  - 핵심 코드 task: spec + 풀 code review (Task 3, 6, 7, 8, 9, 10, 11)
- 매 task 완료 후 commit + `git push` 자동
- 한국어 정책: 모든 Javadoc/주석/커밋 메시지/UI

### 2.3 서브에이전트 호출 패턴

각 task에 대해 다음 중 일부를 dispatch:

| 서브에이전트 | 목적 | 산출물 |
|---|---|---|
| **implementer** | Plan 본문 그대로 구현 + 자체 리뷰 + commit + push | 코드, 테스트 결과, 커밋 해시 |
| **spec-reviewer** | 직접 코드를 읽어 Plan 본문 일치 검증 (implementer 보고 신뢰 ❌) | ✅ Spec compliant / ❌ Issues |
| **code-quality-reviewer** | 코드 품질 평가 (Strengths/Critical/Important/Minor) | APPROVED / APPROVED_WITH_FIXES / NEEDS_FIXES |
| **fix subagent** | implementer/reviewer 발견 이슈 처리 | 수정 + 검증 + commit + push |

서브에이전트는 모두 `Agent(subagent_type=general-purpose)`로 dispatch. Plan 본문 전체와 environmental context (디렉터리, JAVA_HOME, Docker 경로, 이전 commit 등)를 prompt에 포함.

---

## 3. Task별 실행 내역

### Task 1: Gradle 프로젝트 초기화 ✅

- **Status**: DONE
- **Commit**: `13d4668` master root commit
- **서브에이전트**: implementer(DONE) → spec-reviewer(✅)
- **산출물**: `settings.gradle.kts`, `build.gradle.kts`, `gradle.properties`, `.gitignore`, gradle wrapper 복사, `git init`
- **Plan 조정**: Plan 본문 `gradle wrapper --gradle-version 8.10`은 시스템 gradle이 없어 불가 → **esg-t2의 gradle wrapper(9.4.1) 복사**로 변경. 결과적으로 esg-t2와 동일 버전 사용.
- **검증**: `gradlew --version` → Gradle 9.4.1 / JVM 25.0.3 확인
- **소요 시간**: 약 6분 (서브에이전트 1회 + spec review)

### Task 2: 8개 모듈 패키지 뼈대 ✅

- **Status**: DONE
- **Commit**: `af2663a`
- **서브에이전트**: implementer(DONE) → spec-reviewer(✅)
- **산출물**: 8개 `package-info.java` (`iam`, `entity`, `audit`, `ghg`, `evidence`, `vw`, `rpt`, `shared`) with `@ApplicationModule(allowedDependencies = {...})`
- **검증**: 모든 allowedDependencies 배열이 design.md §2.6과 일치
- **소요 시간**: 약 4분

### Task 3: shared 기본 클래스 + OTel BOM fix + code review fix ✅

- **Status**: DONE (3 커밋)
- **Commits**: `65b4e70` → `e9edcb7` (fix) → `9a9746d` (review fix)
- **서브에이전트**: implementer(DONE_WITH_CONCERNS — compileJava 실패) → fix-subagent(OTel BOM) → spec-reviewer(✅) → code-quality-reviewer(APPROVED_WITH_FIXES) → fix-subagent(review 반영)
- **산출물**:
  - shared/exception/{package-info, EsgException}
  - shared/event/{package-info, DomainEvent}
  - shared/web/{package-info, ErrorResponse}
  - build.gradle.kts: OTel instrumentation BOM 도입
- **이슈 1 (build)**: `opentelemetry-spring-boot-starter:2.10.0-alpha` 직접 핀 → Maven Central 해결 실패 (해당 버전 없음).
  - **Fix**: BOM 사용 — `opentelemetry-instrumentation-bom-alpha:2.28.1-alpha` + starter 의존성에서 버전 핀 제거 → BUILD SUCCESSFUL.
- **이슈 2 (code review Important)**:
  - I-1: BOM 좌표 가독성 (속성/`-alpha` 접미사 분리)
  - I-2: `EsgException.errorCode` null 가드 부재 (`Objects.requireNonNull` 추가)
  - I-3: `DomainEvent` 반환값 nullability 명문화 (Javadoc + publisher의 `IllegalStateException` 책임)
  - **Fix**: 모두 적용 + Minor 2건(M-1 errorCode 컨벤션, M-3/M-5 Javadoc) 함께 처리
- **소요 시간**: 약 30분 (review iteration 포함)

### Task 4: docker-compose (PostgreSQL 18 + Redis 7) ✅

- **Status**: DONE_WITH_CONCERNS
- **Commit**: `529f538`
- **서브에이전트**: implementer(DONE_WITH_CONCERNS) → spec-reviewer(✅, 변경 정당화됨)
- **산출물**: `docker-compose.yml` (postgres + redis + healthcheck)
- **Plan 조정 1**: postgres 18 이미지가 `pg_ctlcluster` 호환 레이아웃을 요구 → 마운트 경로 `/var/lib/postgresql/data` → **`/var/lib/postgresql`**. Plan 시점에는 postgres 18 docker 이미지 변경이 반영 안 됨. 문서화된 정당화.
- **Plan 조정 2**: `version: "3.9"` 필드 제거 (Compose v2 obsolete 경고).
- **검증**: postgres healthy + `pg_isready accepting`, redis healthy + `PONG`.
- **환경 이슈**: `docker-credential-desktop` PATH 미설정으로 이미지 pull 실패 → 세션 PATH에 docker bin 추가하여 해결. **후속 task에서도 동일 처리 필요** (구조적 이슈).
- **소요 시간**: 약 22분 (Docker daemon 시작 포함)

### Task 5: Flyway V1 + V2 ✅

- **Status**: DONE
- **Commit**: `10e2ecd`
- **서브에이전트**: implementer(DONE)만 (단순 SQL → spec review 생략, 자체 리뷰 충실)
- **산출물**:
  - `V1__initial_schema.sql` — `tenants`, `disclosure_schedules`, `event_publication`
  - `V2__disclosure_schedule_seed.sql` — KSSB/IFRS/CSRD 6행 시드
- **검증**: 실제 마이그레이션 실행은 Task 7에서 통합 테스트로 검증
- **소요 시간**: 약 2분

### Task 6: Spring Boot 메인 + application.yml ✅

- **Status**: DONE
- **Commit**: `73ba5c7`
- **서브에이전트**: implementer(DONE) (compileJava 의도적으로 skip — ApplicationContextTest가 Task 7 의존)
- **산출물**: `EsgT3Application.java`, `application.yml` (5 섹션), `application-local.yml`, `application-test.yml`, `ApplicationContextTest.java`
- **검증**: spec review는 Task 7 묶음에서 진행

### Task 7: AbstractIntegrationTest (Testcontainers) ✅

- **Status**: DONE
- **Commit**: `861333c`
- **서브에이전트**: implementer(DONE — 추가 schema fix 포함) → spec-reviewer(Task 6+7 묶음, ✅)
- **산출물**:
  - `AbstractIntegrationTest.java` — PostgreSQL 18 Testcontainer + static start block + driver-class-name 오버라이드 (L-P0-04, L-P0-16)
  - **V1 schema 패치**: `completion_attempts INTEGER NOT NULL DEFAULT 0` 컬럼 추가 (esg-t3 신규 학습)
- **이슈 (esg-t3 신규 학습 — 가장 중요)**: Spring Modulith **2.0.0**의 `JpaEventPublication`이 `completionAttempts` 필드를 신규 도입했으나, esg-t2(1.x 기반) V1 스키마에는 없음. 첫 통합 테스트가 `SchemaValidation: missing column [completion_attempts]`로 실패.
  - **Fix**: V1 SQL에 컬럼 추가. `javap -p`로 Modulith 2.0.0 JAR의 실제 필드 직접 확인 후 결정.
  - **L-P0-02의 확장 학습**: "Spring Modulith 버전 업그레이드 시 event_publication 스키마 변경사항 확인" → esg-t3는 2.0.0에서 한 컬럼 추가가 더 있음을 발견.
- **검증**: `ApplicationContextTest.스프링_컨텍스트가_정상적으로_로드된다` ✅ BUILD SUCCESSFUL 11s.
- **소요 시간**: 약 18분

### Task 8: ArchUnit 아키텍처/컨벤션 테스트 ✅

- **Status**: DONE
- **Commit**: `4bea450`
- **서브에이전트**: implementer(DONE — Plan 조정 1건 포함)
- **산출물**: `ArchitectureTest.java` (7개 규칙), `ConventionTest.java` (2개 규칙) → 9/9 pass
- **Plan 조정 / esg-t3 신규 학습**: ArchUnit **1.3.0**의 `failOnEmptyShould` 기본 활성화. `.that()` 절이 0개 매칭하는 규칙은 vacuous pass가 아니라 `AssertionError`. 도메인 코드가 비어있는 Phase 0 단계에서는 4개 규칙 실패.
  - **Fix**: 해당 4개 규칙에 `.allowEmptyShould(true)` 추가. 후속에서 도메인 클래스가 들어오면 자동으로 진짜 검증 시작.
  - 영향 받은 규칙: `repository는_infra_패키지에만_존재한다`, `도메인은_JPA_어노테이션을_사용하지_않는다`, `도메인_이벤트는_DomainEvent_마커를_구현`, `컨트롤러는_controller_패키지에`.
- **소요 시간**: 약 6분

### Task 9: Spring Modulith ModularityTest ✅

- **Status**: DONE
- **Commit**: `018eb95`
- **서브에이전트**: implementer(DONE)
- **산출물**: `ModularityTest.java` (3 테스트: 모듈 수, 경계, Documenter)
- **Plan 조정 / esg-t3 신규 학습**: Modulith Documenter 산출물 디렉터리 — Plan은 `target/modulith-docs/`(Maven 기본)였으나 Gradle 빌드에서는 `build/spring-modulith-docs/`. 18개 산출물(`all-docs.adoc`, `components.puml`, 8 모듈 각각의 `.adoc`+`.puml`) 정상 생성.
- **검증**: 3/3 pass, 8개 모듈 인식 확인 (변경 이전 시점).
- **소요 시간**: 약 3분

### Task 10: Actuator + Prometheus 엔드포인트 검증 ✅

- **Status**: DONE_WITH_CONCERNS
- **Commit**: `b69e847`
- **서브에이전트**: implementer(DONE_WITH_CONCERNS — 2개 보강 fix)
- **산출물**: `ActuatorSecurityConfig.java` (Phase 0 임시), `ActuatorEndpointTest.java` (MockMvcBuilders 패턴, L-P0-01)
- **이슈 1 (esg-t3 신규 학습)**: 첫 실행 시 `/actuator/health`가 **503 반환**. 원인: `RedisReactiveHealthIndicator`가 테스트 환경에 Redis 없어서 DOWN 보고.
  - **Fix**: `application-test.yml`에 `management.health.redis.enabled: false`. 운영 환경은 영향 없음.
- **이슈 2 (design.md와의 불일치)**: Spring Modulith가 **`observability/` 패키지를 9번째 모듈로 자동 인식** (`@ApplicationModule` 없어도 직하위 패키지는 자동 모듈 — L-P0-06 확장).
  - design.md §2.4에는 "observability는 모듈 아닌 cross-cutting"으로 명시.
  - **Fix (긴급)**: `ModularityTest.모듈_8개가_등록된다()` → `모듈_9개가_등록된다()` + assertion 9L. 메서드명·주석 갱신.
  - **Phase 0 종료 시 정리 필요**: (옵션 A) observability를 `shared/observability/` 하위로 이동, 또는 (옵션 B) design.md를 "9개 모듈 + observability를 명시 모듈로 인정"으로 갱신. 후속 task 22 또는 별도 정리 단계에서 결정.
- **검증**: 전체 테스트 15 passed.
- **소요 시간**: 약 13분

### Task 11: OpenTelemetry SDK + JSON 로그 + RequestId ⏸️

- **Status**: 대기 중 (사용자가 실행 로그 작성 요청으로 일시 중단)
- **다음 단계**: implementer dispatch 재개
- **예상 산출물**: `OtelConfig.java`, `MetricsConfig.java`, `RequestIdFilter.java`, `logback-spring.xml`

### Task 12~22

미진행. 본 문서에 진행 시 누적.

---

## 4. 누적 학습 (esg-t3 신규 인사이트)

> esg-t2 L-0-01~L-0-16, L-P0-01~L-P0-06 학습은 **모두 그대로 유효**. 본 섹션은 esg-t3 Phase 0에서 **추가로** 발견한 항목.

| ID | 학습 | 근거 task |
|---|---|---|
| **L3-P0-01** | OTel `opentelemetry-spring-boot-starter`를 직접 버전 핀하지 말고 **`opentelemetry-instrumentation-bom-alpha`** 사용. 정식 stable BOM은 없고 alpha 채널이 정상. | Task 3 |
| **L3-P0-02** | `EsgException`은 abstract + `Objects.requireNonNull(errorCode)` 의무. errorCode가 외부 API 응답으로 노출되므로 null 진입 차단. | Task 3 review |
| **L3-P0-03** | `DomainEvent` 반환값은 모두 null 금지. publisher 측에서 `IllegalStateException`. ArchUnit은 런타임 null을 검증 못함 → 단위 테스트로 보강. | Task 3 review |
| **L3-P0-04** | **Postgres 18** docker 이미지: 마운트 경로가 `/var/lib/postgresql/data` 아닌 **`/var/lib/postgresql`** (pg_ctlcluster 레이아웃). | Task 4 |
| **L3-P0-05** | docker-compose v2에서 **`version` 필드 obsolete**. 제거 권장. | Task 4 |
| **L3-P0-06** | 환경 PATH에 docker bin 없을 때 `docker-credential-desktop` 호출 실패. 세션 PATH에 `C:\Program Files\Docker\Docker\resources\bin` 추가 필요. **운영 가이드에 반영**. | Task 4 |
| **L3-P0-07** | **Spring Modulith 2.0.0** `JpaEventPublication` 신규 필드 `completion_attempts`. V1 스키마에 `INTEGER NOT NULL DEFAULT 0` 의무. (L-P0-02의 2.x 확장 학습) | Task 7 |
| **L3-P0-08** | **ArchUnit 1.3.0** `failOnEmptyShould` 기본 활성. 도메인 클래스가 없는 단계에서 빈 매칭 규칙에 `.allowEmptyShould(true)` 필요. | Task 8 |
| **L3-P0-09** | Modulith Documenter 산출 디렉터리: Gradle은 **`build/spring-modulith-docs/`** (Maven `target/`이 아님). `.gitignore`의 `build/`로 자동 제외. | Task 9 |
| **L3-P0-10** | 테스트 환경에서 Redis 없으면 `/actuator/health` 503. `management.health.redis.enabled: false` (test profile). | Task 10 |
| **L3-P0-11** | Modulith 자동 모듈 인식이 `observability/` cross-cutting 패키지에도 적용 (L-P0-06 재확인). 설계 시 cross-cutting을 모듈로 받아들이거나 `shared/observability/` 하위로 위치. | Task 10 |

본 학습 항목들은 Phase 0 종료 시 `docs/insight.md`에 정식 누적 (Task 22.6).

---

## 5. 커밋 이력 (Phase 0)

```
b69e847 feat: Actuator + Prometheus 엔드포인트 + 임시 Security 설정 (Phase 1에서 ABAC로 교체)
018eb95 feat: Spring Modulith ModularityTest + Event Catalog 자동 생성
4bea450 feat: ArchUnit 아키텍처/컨벤션 자동 검증 추가
861333c feat: AbstractIntegrationTest (Testcontainers PostgreSQL 18) 추가
73ba5c7 feat: Spring Boot 메인 클래스 + application.yml 설정 추가
10e2ecd feat: Flyway V1 초기 스키마 + V2 disclosure_schedules 시드
529f538 chore: PostgreSQL 18 + Redis docker-compose 추가
9a9746d refactor: code review 반영 — EsgException null 가드, DomainEvent contract 명문화, OTel BOM 표기 정비, package-info 문서화
e9edcb7 fix: OpenTelemetry instrumentation BOM 도입 — Maven Central 해결 가능한 버전으로 변경
65b4e70 feat: shared 모듈 기본 클래스 추가 (Exception, DomainEvent, ErrorResponse)
af2663a feat: 8개 Spring Modulith 모듈 패키지 뼈대 추가
13d4668 chore: Gradle 프로젝트 초기화 (Spring Boot 4 + Java 25)
```

총 12 커밋, 모두 한국어 메시지, 모두 `master`에 push.

---

## 6. 메트릭 요약 (Task 1~10)

| 지표 | 값 |
|---|---|
| 진행률 | 10/22 (45%) |
| 누적 커밋 | 12개 |
| 누적 통과 테스트 | 15개 (`ApplicationContextTest` 1, `ActuatorEndpointTest` 2, `ArchitectureTest` 7, `ConventionTest` 2, `ModularityTest` 3) |
| BUILD 상태 | SUCCESSFUL |
| 서브에이전트 dispatch 횟수 | 약 15회 (implementer 11, spec-reviewer 5, code-quality-reviewer 1, fix-subagent 3) |
| 누적 Plan 조정 | 6건 (모두 정당화·문서화) |
| 누적 esg-t3 신규 학습 | 11건 (L3-P0-01~11) |

---

## 7. 후속 정리 항목 (Phase 0 종료 시)

- [ ] design.md §2.4 갱신: observability가 모듈로 자동 인식되는 현실 반영 (또는 코드 구조 조정)
- [ ] Plan 본문 작은 정정: target/ → build/spring-modulith-docs/, gradle 8.10 → 9.4.1, postgres 마운트 경로, BOM 도입
- [ ] insight.md에 L3-P0-01~11 정식 누적 (Task 22.6에서)
- [ ] `.gitattributes` 추가 검토 (`gradlew text eol=lf`, Windows CRLF 경고 해소)
- [ ] CLAUDE.md 갱신 시 docker-credential-desktop PATH 운영 메모 추가 (L3-P0-06)
- [ ] `.claude/rules/` 17개 정비 (Phase 1 시작 전, design.md §8.4 명시)

---

## 8. 본 문서 갱신 정책

각 task 완료 시:
1. "Task별 실행 내역" 섹션에 새 task 추가
2. 신규 학습 발견 시 "누적 학습" 섹션에 `L3-P0-XX` ID로 추가
3. "커밋 이력"에 새 커밋 추가
4. "메트릭 요약" 갱신
5. "후속 정리 항목"에 새 발견 보강

Phase 0 종료 시 본 문서 자체를 `READY_FOR_INSIGHT_MIGRATION` 상태로 마크하고, insight.md로 학습 항목 이관.
