# Phase 1 실행 로그 (Execution Log)

> **버전**: Plan 작성 완료 - 실행 시작 직전
> **계획 작성일**: 2026-05-27
> **목적**: `subagent-driven-development` 스킬로 실행하는 Phase 1의 실제 진행 내역.
>          사전 계획(plan.md)·설계(design.md)와 별도로 "실제로 어떻게 진행했고 어떤 발견·수정이 있었는가"를 추적.
>
> **신규**: 2026-05-27 사용자 지침으로 **서브에이전트 호출 단위 상태 추적** 섹션을 task별로 누적한다.

---

## 1. 실행 환경 (사전 확정)

Phase 0 실행 환경과 동일 (`docs/superpowers/plans/2026-05-26-esg-t3-phase0-execution-log.md` §1 참조).

추가 변경 사항: 없음 (Phase 1은 신규 외부 의존성 도입 없이 진행. JJWT 0.12.6은 Maven Central 표준 BOM 외 — Task 14에서 추가).

---

## 2. 진행 방식 결정 사항

### 2.1 사전 문서 결정 (Phase 1 시작 전)

- design.md §3, §4.3, §5.4, §7.3 ✅ Phase 0에서 완성
- Phase 1 plan.md (23 task) ✅ 2026-05-27 작성
- .claude/rules/13~17 ✅ Phase 0에서 작성
- 정책 YAML 6개·iam 모듈 코드·JWT 인프라 — **본 Phase에서 작성**

### 2.2 실행 전략 (사용자 확인 완료)

- **서브에이전트 실행** (`superpowers:subagent-driven-development`)
- review 전략: Phase 0과 동일 **"Task 구분 적용"**
  - 단순 (spec review만): Task 1, 2, 8, 23
  - 중간 (spec + 간소 code review): Task 9, 10, 17, 19, 20, 21, 22
  - 핵심 (spec + 풀 code review): Task 3~7, 11~16, 18
- 매 task 완료 후 commit + `git push` 자동
- **신규**: 서브에이전트 호출 단위 상태(시작/종료/소요/결과/이슈) 본 로그에 즉시 기록

### 2.3 서브에이전트 호출 패턴

| 서브에이전트 | 목적 |
|---|---|
| **implementer** | Plan 본문 그대로 구현 + 자체 검증 + commit + push |
| **spec-reviewer** | 직접 코드 읽어 plan 본문 일치 검증 |
| **code-quality-reviewer** | 품질 평가 |
| **fix-subagent** | 발견 이슈 처리 |

모두 `Agent(subagent_type=general-purpose)`로 dispatch. Prompt에 plan 본문 전체 + 환경 컨텍스트 포함.

---

## 3. Task별 실행 내역

> 각 task는 다음 형식으로 작성:
>
> ```
> ### Task NN: <제목>
> - **Status**: TODO | IN_PROGRESS | DONE | DONE_WITH_CONCERNS | BLOCKED
> - **Commit(s)**: <hash>(<message 요약>) ...
> - **산출물**: 파일 목록
> - **이슈**: 발견된 이슈
> - **소요 시간**: 총 분
>
> #### 서브에이전트 호출 상세
> - implementer: <시작>~<종료>, 결과
> - spec-reviewer: <시작>~<종료>, 결과
> - code-quality-reviewer: ...
> - fix-subagent: ...
> ```

### Task 1: V3 마이그레이션 — iam + entity 테이블 ✅

- **Status**: DONE
- **Commit**: `ae57115`
- **산출물**: `src/main/resources/db/migration/V3__iam_and_entity_tables.sql` (75줄)
- **plan 정정**: V2가 Phase 0(disclosure_schedule_seed)에 점유되어 있어 Phase 1 마이그레이션을 V3/V4/V5로 시프트
- **검증**: `./gradlew test --tests "*ApplicationContextTest"` → BUILD SUCCESSFUL (23s)
- **소요 시간**: 약 15분

#### 서브에이전트 호출 상세
- (시도 1) implementer 서브에이전트 dispatch → **세션 한도 초과로 시작 전 종료** (agentId a3956ca49d56cc12b)
- (대안 적용) 인라인 실행으로 전환 — 단순 SQL 작성 task는 인라인이 토큰 효율적

#### 발견 사항
- **L-P1-06 후보**: Phase 0 산출물의 마이그레이션 버전을 plan 작성 시 확인하지 못해 V2 충돌 발생. self-review §1 spec 커버리지 점검에서 마이그레이션 번호 검증 단계가 누락되어 있었다. 향후 plan에서 마이그레이션 추가 시 `ls db/migration/` 출력을 plan 본문에 인용해 번호 충돌을 사전 방지.
- 서브에이전트 세션 한도 → 단순 task는 인라인 우선, 복잡 task만 서브에이전트 dispatch로 전환 검토

### Task 2: V4 마이그레이션 — app_role + RLS ✅

- **Status**: DONE
- **Commit**: `3560e0b`
- **산출물**: `src/main/resources/db/migration/V4__rls_and_app_role.sql` (47줄)
- **검증**: `./gradlew test --tests "*ApplicationContextTest"` → BUILD SUCCESSFUL (18s)
- **소요 시간**: 약 5분

#### 서브에이전트 호출 상세
- 인라인 실행 (단순 SQL, 토큰 효율 우선)

#### 발견 사항
- `DO $$ ... END$$` PL/pgSQL 블록이 Flyway·Testcontainers PG 18에서 정상 적용
- RLS 활성화·정책 생성 모두 성공. RLS 동작 자체 검증은 Task 13에서 통합 테스트로 수행

### Task 3: PolicyContext + Subject + Resource + PolicyAction ✅

- **Status**: DONE
- **Commit**: `4b1a687`
- **산출물**: 5 main + 1 test = 6 파일 (184줄)
  - `iam/domain/PolicyAction.java` (enum 8 액션)
  - `iam/domain/Subject.java` (record, 5 필드, immutable Set)
  - `iam/domain/Resource.java` (record, immutable Map)
  - `iam/domain/PolicyContext.java` (record + Environment 중첩 record)
  - `iam/domain/package-info.java` (Spring·JPA 의존 0 명시)
  - test: `PolicyContextTest` 5건
- **검증**: `./gradlew test --tests "*PolicyContextTest"` → BUILD SUCCESSFUL (7s)
- **소요 시간**: 약 8분

#### 서브에이전트 호출 상세
- 인라인 실행 (단순 record 작성, TDD Red→Green 5건)

#### 발견 사항
- Plan에 명시되지 않았던 추가 테스트 1건 추가: `Subject_assignedEntityIds도_immutable로_노출한다` — Resource의 immutable 검증과 대칭 보장

### Task 4: PolicyDecision + PolicyEffect ✅

- **Status**: DONE
- **Commit**: `94a8ed8`
- **산출물**: PolicyEffect.java, PolicyDecision.java, PolicyDecisionTest.java (88줄 / 5건 통과)
- **검증**: BUILD SUCCESSFUL (6s)
- **인라인 실행**

### Task 5: PolicyDocument + PolicyRule + WhenClauseMatcher ✅

- **Status**: DONE
- **Commit**: `bb66da1`
- **산출물**: PolicyRule, PolicyDocument, WhenClauseMatcher + PolicyDocumentTest (321줄 / 6건 통과)
- **검증**: BUILD SUCCESSFUL (3s, 1차 실패 → 변수 치환 버그 수정 후 통과)

#### 발견 + 수정
- **버그**: `contains` 매처가 `${resource.entityId}` 변수 치환을 누락 (`matchWithMatcher`가 `ctx`를 받지 않음)
- **수정**: `matchWithMatcher`에 `ctx` 파라미터 추가 + operand 값에 `resolveVariable` 재귀 적용
- **부수 효과**: `in` 매처도 동일 패턴 + CSV 문자열 분기 추가 (`env.lockedTenantIds`가 `System.getProperty`에서 CSV 형태로 오는 케이스 대비). 이는 emergency-lockdown.yaml의 정책 실행 시 필수.

### Task 6: PolicyEvaluator ✅

- **Status**: DONE
- **Commit**: `394c7c5`
- **산출물**: PolicyEvaluator + PolicyEvaluatorTest + PolicyEvaluatorPriorityTest (168줄 / 5건 통과)
- **검증**: priority 250 > 200 > 100 > 0 순서 평가 정상. SUPER_ADMIN 전권보다 emergency-lockdown 우선.

### Task 7: PolicyRegistry ✅

- **Status**: DONE
- **Commit**: `91f9e27`
- **산출물**: PolicyRegistry + PolicyRegistryTest (93줄 / 3건 통과 — concurrent 안전성 포함)
- **검증**: 읽기·쓰기 동시 400회 5초 내 NPE 없이 완료

### Task 8: 정책 YAML 6개 + 스키마 문서 ✅

- **Status**: DONE
- **Commit**: `0e9a8ea`
- **산출물**: 8 파일 (README + schema + 6 YAML, 총 268줄, 11 규칙, 14 tests 케이스)
- **인라인 실행**

### Task 9: PolicyYamlLoader + PolicyEngineConfig + tests 자동 실행 ✅

- **Status**: DONE
- **Commit**: `4164333`
- **산출물**: PolicyYamlLoader + PolicyConfigurationException + PolicyEngineConfig + 2 test (316줄)
- **검증**: PolicyYamlLoaderTest 3건 + PolicyYamlTestCasesTest 16건 동적 → **모두 통과**
- **DoD #1 달성**: ABAC 정책 단위 테스트 ≥ 20건 (현재 43건)

#### 발견 + 수정
- EsgException이 abstract → PolicyConfigurationException 신설 (첫 EsgException 구체 하위)
- buildContext에서 t1/e1/u1 같은 짧은 ID도 UUID로 정규화하여 WhenClauseMatcher의 equalsCoerced 비교가 일관성 있게 동작하도록 함

### Task 10: PolicyHotReloader ✅

- **Status**: DONE
- **Commit**: `ddc30a5`
- **산출물**: PolicyHotReloader + PolicyHotReloaderTest + PolicyEngineConfig @Bean + application.yml (163줄)
- **검증**: TempDir에 새 YAML 작성 → 5초 내 PolicyRegistry 갱신 확인 (Awaitility)
- **DoD #5 달성**: 정책 YAML 핫리로드 < 5초
- **부수 효과**: ApplicationContextTest 통과 — Spring 컨텍스트가 정책 6개 로드 후 부팅 성공

### Task 11: V4 policy_decisions + PolicyDecisionLogger

- **Status**: TODO

### Task 12: PolicyFacade

- **Status**: TODO

### Task 13: TenantContext + TenantContextInterceptor

- **Status**: TODO

### Task 14: JwtTokenProvider + JwtAuthentication

- **Status**: TODO

### Task 15: JwtAuthFilter

- **Status**: TODO

### Task 16: RedisTokenBlacklist

- **Status**: TODO

### Task 17: AuthController

- **Status**: TODO

### Task 18: IamSecurityConfig + AbacSampleController

- **Status**: TODO

### Task 19: GlobalExceptionHandler

- **Status**: TODO

### Task 20: LockdownController

- **Status**: TODO

### Task 21: PolicyEvaluationMetrics

- **Status**: TODO

### Task 22: ArchUnit 보강

- **Status**: TODO

### Task 23: Phase 1 DoD 검증 + 문서 + ADR

- **Status**: TODO

---

## 4. Phase 1 종료 시점 체크리스트

(Phase 0 종료 시 사용한 체크리스트와 동일 형식. Phase 1 완료 후 채움.)

- [ ] 전체 테스트 `./gradlew clean test` 통과
- [ ] ModularityTest + ArchitectureTest + ConventionTest 통과
- [ ] Modulith Documenter 산출물 `build/spring-modulith-docs/` 갱신
- [ ] DoD 14개 항목 모두 ✅
- [ ] L-P1-01~05 모두 insight.md에 정리
- [ ] ADR 014~016 작성
- [ ] runbook 02-rls-leak.md 본문 완성
- [ ] CLAUDE.md "현재 진행" 업데이트

---

## 5. 누적 학습 (L-P1-XX)

(Phase 1 실행 중 발견되는 학습을 여기에 즉시 등록. Phase 종료 시 insight.md L3-P1-XX 슬롯으로 이관.)

(없음 — 실행 시작 전)

---

## 6. Phase 2 인계 사항

(Phase 1 완료 후 Phase 2 시작 전 정리.)

- audit_logs 테이블 생성 시 policy_decisions와의 통합 방식 (Phase 1에서 단순 INSERT만 — Phase 2에서 Hash Chain 컬럼·trace_id·outbox 통합)
- LockdownController가 발생시키는 `iam.LockdownActivated`/`LockdownReleased` 이벤트는 Phase 1에서 미발행 — Phase 2 audit 모듈 도입 시 추가
- AuthController의 로그인 성공/실패도 Phase 2 audit 이벤트로 자동 기록
