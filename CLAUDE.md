# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 프로젝트 개요

ESG 공시지원 시스템 3세대(esg-t3). 운영·거버넌스 강화에 집중하는 esg-t2의 후속.

- **Group**: `ai.claudecode` | **Artifact**: `esg-t3` | **Base package**: `ai.claudecode.esgt3`
- **MVP 범위**: Multi-Entity + Scope 1/2 + AuditLog/Hash Chain + Evidence + Basic Report + VW
- **M+1**: Scope 3, 공급업체 포털, 정정·재공시 UI, iXBRL, AI/LLM
- **현재 진행**: Phase 0 (인프라 + 거버넌스 골격) 마무리 단계

## 기술 스택

| 구성요소 | 버전/선택 |
|---|---|
| Java | 25 LTS (Microsoft JDK 25.0.3+9-LTS) |
| Spring Boot | 4.0.6 |
| Spring Modulith | 2.0.0 (8개 모듈: iam, entity, audit, ghg, evidence, vw, rpt, shared + observability cross-cutting) |
| DB | PostgreSQL 18-alpine (Testcontainers) + Redis 7 |
| Build | Gradle 9.4.1 Kotlin DSL (gradlew wrapper) |
| 관측성 | OpenTelemetry SDK + Collector + Tempo + Loki + Prometheus + Grafana |
| Frontend | Next.js 16.2.6 + TypeScript strict + Tailwind v4 |
| 거버넌스 | ArchUnit 1.3.0 + Spring Modulith ModularityTest |

## 언어 정책

- **기본 언어: 한국어**
- 코드 주석, 로그 메시지, 에러 메시지, UI 텍스트, 커밋 메시지, 문서 — 모두 한국어
- `<html lang="ko">` (Next.js layout.tsx)
- 변수명·메서드명·테스트명도 한국어 사용 가능 (`스프링_컨텍스트가_정상적으로_로드된다()` 등)

## 명령어

```bash
./gradlew build
./gradlew bootRun --args='--spring.profiles.active=local'
./gradlew test
./gradlew test --tests "*ModularityTest"
./gradlew test --tests "*ArchitectureTest" --tests "*ConventionTest"
./tests/runbook-link-validation.sh

cd frontend && npm run dev
cd frontend && npm run build
```

## 환경 운영 메모

- **JAVA_HOME**: `C:\Program Files\Microsoft\jdk-25.0.3.9-hotspot\` (PATH에 java 없음 → gradlew wrapper 사용)
- **Docker bin**: `C:\Program Files\Docker\Docker\resources\bin\` (PATH에 docker 없음 → 명령 실행 전 `export PATH=...` 또는 절대 경로)
- **Node bin**: `C:\Program Files\nodejs\` (PATH에 node/npm 없음 → 동일 처리)
- **gh CLI**: `C:\Program Files\GitHub CLI\gh.exe` (`minsu1775` 로그인됨)

## 아키텍처 핵심

- **ABAC** (속성 기반 접근 제어) + RLS 이중 방어 (Phase 1 시작 시 도입)
- **AuditLog Hash Chain** + Outbox + DLQ + Trace 역추적 (Phase 2)
- **OpenTelemetry 완전 통합** (메트릭 + 분산 트레이싱 + JSON 구조화 로그)
- **Runbook-as-Code** (`docs/runbook/`, `scripts/runbook/`, 알림 자동 첨부)
- **INSERT-only** (활동 데이터·배출량·AuditLog·Snapshot DB 권한 박탈)
- **factorAt 재현성** (배출계수 시점별 조회)
- **정책값 분리** (배출계수·ABAC 정책·공시일정 YAML 외부화)

## 코딩 규칙

세부 규칙은 `.claude/rules/` (Phase 1 시작 전 정비 예정, design.md §8.4).

## 개발 워크플로우

1. 현재 Phase의 plan.md 확인 (`docs/superpowers/plans/`)
2. Phase 체크리스트 — 완료 즉시 체크
3. 실행 로그 갱신 (`docs/superpowers/plans/<phase>-execution-log.md`)
4. 이슈 발견 → `docs/fix.md` 등록 → TDD로 해결 (Phase 1~ 운영)
5. 학습 인사이트 → `docs/insight.md` 누적 (Phase 0 종료 시 L3-P0-XX 이관)

### 구현 실행 방식 (Superpowers)

- 계획 작성: `superpowers:writing-plans` 스킬 → `docs/superpowers/plans/`
- 실행: `superpowers:subagent-driven-development` (Task 구분 적용 review 전략)

## 문서 구조

```
docs/
├── regulatory.md                      # ESG 규제 (esg-t2 계승)
├── insight.md                         # 학습 인사이트 (Phase 0 종료 시 생성)
├── adr/                                # 9~13개 (Phase별 누적)
├── runbook/                            # 6개 (01~06)
├── superpowers/specs/                  # 마스터 설계
├── superpowers/plans/                  # Phase별 plan + execution log
└── (postmortems, event-catalog는 Phase 2~)
```

## P0~P3 규칙 우선순위

| 우선순위 | 규칙 | 적용 시점 |
|---|---|---|
| P0 | 테넌트 격리 (RLS + ABAC 이중) | Phase 1 |
| P0 | Audit 무결성 (Hash Chain, Immutable) | Phase 2 |
| P1 | 재현성 (factorAt, Formula Versioning) | Phase 3+ |
| P2 | 성능·확장성 | Phase 8 |
| P3 | 개발 편의성 | 항상 |
