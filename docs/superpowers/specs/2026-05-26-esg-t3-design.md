# esg-t3 — 설계 문서 (Brainstorming → Design)

> **버전**: 1.0 (전 섹션 완료, 자체 리뷰 통과, 사용자 리뷰 대기)
> **작성일**: 2026-05-26
> **상태**: `READY_FOR_USER_REVIEW`
> **레퍼런스**: `esg-t2/docs/` 전체 (PRD, Spec, Plan, ADR, Insight, ChatGPT/Gemini 리뷰)
> **선행 결정**: CLAUDE.md `언어 정책` — 모든 코드/UI/문서 한국어 기본

---

## 0. 문서 목적

본 문서는 esg-t3 프로젝트의 **브레인스토밍 → 설계 → 실행 계획** 과정을 추적 가능한 형태로 누적한다.

- **Part 1 (의사결정 로그)**: 단계별로 고려한 선택지, 각 옵션의 의미, 선택한 안과 그 이유.
- **Part 2 (설계 섹션)**: 의사결정 위에 쌓아 올린 시스템 설계. 섹션별로 사용자 승인을 받으며 누적.
- **Part 3 (다음 단계)**: 아직 결정되지 않은 항목과 작성 예정 섹션.

esg-t2가 ChatGPT/Gemini 리뷰를 받아 적용/부적용 판단을 문서화한 메타 절차를 계승하되, **esg-t3는 그 절차를 처음부터 적용**한다.

---

# Part 1. 의사결정 로그

> 각 결정은 "옵션 → 선택 → 이유 → 트레이드오프"의 4단 구성. 미래에 결정을 뒤집을 때 근거를 추적할 수 있도록 옵션 전체를 보존한다.

## D1. 업그레이드 축 (esg-t2 → esg-t3의 핵심 차별점)

**질문**: esg-t3는 esg-t2의 어떤 부분을 가장 크게 업그레이드할 것인가?

### 검토한 옵션

| ID | 옵션 | 핵심 내용 |
|---|---|---|
| D1-A | 운영·거버넌스 강화 | 무결성·재현성은 유지하되 ChatGPT/Gemini가 지적한 운영 약점 (ABAC, Event Catalog, Spring Batch, 운영 Runbook, 모듈 계약)을 처음부터 견고하게 |
| D1-B | 도메인 확장 (보고서·정성·다국적) | "문서 시스템" 보강 + iXBRL 렌더링 + CSRD/SSBJ 다국적 Regulation Engine을 MVP에 포함 |
| D1-C | AI/LLM 네이티브 ESG 플랫폼 | esg-t2가 M+1로 미룬 AI 기능을 MVP 핵심으로 (이상값 탐지, 매핑 자동화, narrative 초안, MCP 통합) |
| D1-D | 기술 스택 전환 | Spring/Java를 다른 스택으로 교체 (Kotlin/Quarkus/Node/Go 등) |

### ✅ 선택: D1-A (운영·거버넌스 강화)

### 선택 이유

1. **esg-t2의 외부 리뷰가 일관되게 지적한 영역**: ChatGPT, Gemini 모두 "ESG 도메인·규제 이해는 우수하나 운영 시스템 관점이 약하다"고 평가. 도메인 기능을 더 추가하기보다 같은 규모에서 운영 완성도를 끌어올리는 것이 학습·검증 가치가 크다.
2. **esg-t2 자체가 운영 관련 ADR을 "Phase 12로 미루기"로 결정한 상태**: ABAC, AuditLog DB 분리, Event Catalog, Spring Batch 등은 모두 esg-t2가 "조건부 적용 / Phase 12"로 미뤘다. esg-t3는 그 부채를 처음부터 갚는다.
3. **다른 옵션의 위험**:
   - D1-B(도메인 확장): MVP가 더 커져 Phase 12까지 도달 불가능 위험.
   - D1-C(AI/LLM): 외부 의존성·비용·재현성 문제로 esg-t2의 강점(공시 재현성)과 충돌 가능.
   - D1-D(기술 스택): 학습 곡선이 길어 esg-t2 경험·패턴 재사용 어려움.

### 트레이드오프 / 양보한 것

- Scope 3, 공급업체 포털, 정정·재공시 워크플로우, AI 기능, iXBRL 렌더링은 모두 M+1로 양보.
- 도메인 기능 측면에서는 esg-t2 대비 "축소"로 보일 수 있다 → MVP 범위 결정(D4)에서 이를 명시.

---

## D2. esg-t2와의 관계 (코드·문서 계승 정책)

**질문**: esg-t3는 esg-t2를 어떻게 활용할 것인가?

### 검토한 옵션

| ID | 옵션 | 핵심 내용 |
|---|---|---|
| D2-A | 완전 재구축 + 문서 계승 | 코드는 신규, esg-t2의 PRD/Spec/ADR/Insight 패턴·교훈은 참고. 운영·거버넌스 설계를 근본부터 다시 |
| D2-B | esg-t2 포크 후 점진 업그레이드 | esg-t2 코드를 복사하고 모듈별 거버넌스 강화 리팩터링 |
| D2-C | 하이브리드 (핵심 도메인만 포팅) | 검증된 도메인 로직(EmissionCalculator, HashChainCalculator 등)만 포팅, 인프라·보안·이벤트 계층은 재설계 |

### ✅ 선택: D2-A (완전 재구축 + 문서 계승)

### 선택 이유

1. **D1-A(운영·거버넌스 강화)와의 정합성**: 거버넌스를 골격에 새기려면 기존 코드의 구조적 제약(RBAC, 단일 DB, 이벤트 카탈로그 부재)을 끌고 들어가지 않는 것이 깨끗하다.
2. **학습 자산은 코드보다 문서**: esg-t2의 진짜 자산은 ADR 9건, insight.md (53KB, 교훈 L-0-01~L-0-16), fix.md(이슈 추적), `.claude/rules/` 11개 파일. 이 자산은 코드와 무관하게 계승 가능하다.
3. **D2-B의 위험**: 점진 업그레이드는 "기존 결정을 뒤집는 비용"이 점점 누적된다. ABAC 도입 시 Phase 1 컨트롤러 전체 재작성 등 발생.
4. **D2-C의 트레이드오프**: 도메인 로직은 esg-t2에서 검증됐지만, esg-t3의 ABAC·Event Catalog·Observability 패턴을 적용하려면 결국 시그니처가 바뀐다. 포팅 가치가 낮다.

### 트레이드오프 / 양보한 것

- 초기 진행 속도 느림. esg-t2 Phase 0~6B에서 만든 코드를 다시 작성.
- 단, 문서 계승으로 "어디에서 무엇이 어려웠는지"는 사전에 알 수 있어 같은 함정에 빠지지 않음.

---

## D3. 기술 스택

**질문**: esg-t2의 기술 스택을 유지할 것인가, 일부 교체할 것인가?

### 검토한 옵션

| ID | 옵션 | 핵심 내용 |
|---|---|---|
| D3-A | esg-t2와 동일 스택 유지 | Spring Boot 4.0.x + Java 25 LTS + Spring Modulith 2.0 + PostgreSQL 18 + Next.js 16 + Tailwind 4 |
| D3-B | 백엔드 일부 교체 | Spring Modulith → Hexagonal/Clean 명시 구조, 또는 Kotlin 겸용, 또는 Spring Batch/Kafka 명시 도입 |
| D3-C | 완전히 다른 스택 | Kotlin/Quarkus, Go, .NET, Node.js 등 |

### ✅ 선택: D3-A (esg-t2와 동일 스택 유지)

### 선택 이유

1. **D1-A(거버넌스 강화)에 집중하려면 스택 학습 비용을 0으로**: 새 스택은 새로운 함정을 만든다. esg-t2의 16개 교훈(L-0-01~L-0-16)이 모두 Spring Boot 4 + Java 25 + Modulith 환경에 특화돼 있다.
2. **esg-t2의 미해결 운영 항목이 모두 이 스택 안에서 해결 가능**: ABAC = Spring Security + 정책 엔진. Observability = OpenTelemetry (Spring Boot 4 내장). Event Catalog = Spring Modulith 기본 기능. Spring Batch도 같은 생태계.
3. **D3-B(부분 교체)의 트레이드오프**: 모듈 구조를 Hexagonal로 바꾸는 것은 Modulith 검증 도구(`ModularityTest`, `documenter`)와 충돌. 학습 가치는 있으나 거버넌스 도구 자체를 다시 만들어야 함.

### 트레이드오프 / 양보한 것

- Spring 외 생태계 학습 기회 포기.
- 단, Spring 자체가 Java 25 Virtual Threads, Spring AI, Spring Modulith 등 최신 기능을 흡수하는 중이라 학습 천장은 높음.

---

## D4. MVP 범위

**질문**: 운영·거버넌스 강화에 집중하기 위해 도메인 범위를 어떻게 잡을 것인가?

### 검토한 옵션

| ID | 옵션 | 핵심 내용 |
|---|---|---|
| D4-A | 좋은 MVP (esg-t2와 동일) | Multi-Entity + Scope 1/2/3(Cat 1,2,11) + VW + 정정·재공시 + Supply 포털 + KSSB 2 보고서 |
| D4-B | 집중 MVP (운영 견고성 우선) | Multi-Entity + Scope 1/2 + AuditLog + Evidence + Basic Report + VW. Scope 3·Supply·정정·재공시는 M+1 |
| D4-C | 최소 MVP (거버넌스 완전성 우선) | 단일 법인 + Scope 1만 + AuditLog + Evidence + Tenant Isolation. Multi-Entity·Scope 2는 M+1 |

### ✅ 선택: D4-B (집중 MVP)

### 선택 이유

1. **ChatGPT 권고와 일치**: ChatGPT는 esg-t2 리뷰에서 "MVP가 너무 크다, Multi-Entity + Scope1/2 + Audit + Snapshot + Evidence + Basic Report로 축소하라"고 권고. esg-t2는 ADR로 거절했지만, esg-t3는 운영 완성도를 위해 그 권고를 수용.
2. **D4-A(esg-t2와 동일)의 문제**: 도메인이 그대로면 운영 강화 항목이 겹쳐서 Phase 0~12를 다시 한 번 하는 것과 다를 바 없음. 차별점 손실.
3. **D4-C(최소 MVP)의 문제**: Multi-Entity·Scope 2가 빠지면 KSSB 2 보고서가 의미 없어짐. 다법인 연결은 ABAC 정책 평가 시나리오의 핵심 케이스이기도 함.
4. **VW는 유지**: 외부 검증인 워크플로우는 ABAC 정책 평가의 가장 까다로운 사례(읽기 전용 + 지정 스냅샷 한정). 거버넌스 검증에 필수.

### 트레이드오프 / 양보한 것

- Scope 3는 esg-t2가 핵심 차별점으로 본 영역. esg-t3는 이를 M+1로 양보.
- 단, 데이터 모델에 `scope` 컬럼은 처음부터 포함하여 M+1 활성화 비용 최소화.
- 정정·재공시는 INSERT-only 원칙은 유지(version, reason_code 컬럼 포함)하되 비교·재공시 UI/플로우는 M+1.

---

## D5. 운영·거버넌스 강화의 중점 영역

**질문**: 운영·거버넌스 강화 안에서 어떤 항목을 중점적으로 다룰 것인가? (여러 개 선택 가능)

### 검토한 옵션

| ID | 옵션 | 핵심 내용 |
|---|---|---|
| D5-A | ABAC 권한 모델 | esg-t2 RBAC 한계 극복. tenant+entity+year+sensitivity+approval_state 속성 기반 접근 제어. Spring Security @PreAuthorize + Policy Engine |
| D5-B | Executable Governance (ArchUnit) | 문서 규칙을 코드로 강제. ArchUnit + Modulith 검증 + Convention Test + Spotless/Checkstyle 자동 검사 |
| D5-C | Event Catalog + Outbox 완성 | Domain Event Catalog 문서화, Event Naming Convention, Outbox + Retry + DLQ + 멱등성 강제 |
| D5-D | Runbook + Observability | 장애 대응 Runbook, OpenTelemetry 통합, 분산 트레이싱, 알림 규칙, 복구 결정, 복구 훈련 |

### ✅ 선택: D5-A + D5-D (ABAC + Runbook + Observability)

### 선택 이유

1. **D5-A(ABAC)**: esg-t2 RBAC의 가장 큰 한계 — "ESG_MANAGER가 너무 많은 권한". 다법인·다회계연도·다민감도 환경에서 RBAC만으로는 권한 폭증. ABAC 도입 시점에 따른 비용 차이가 매우 큼(Phase 1에서 도입 = 저비용, Phase 5+ 도입 = 모든 컨트롤러 재작성).
2. **D5-D(Runbook + Observability)**: 운영 강화의 가장 가시적 산출물. Hash Chain 오류, RLS 누락, 재계산 실패 등 esg-t2에서 발견된 모든 운영 위험에 절차적 답을 제공.
3. **D5-B, D5-C는 부수적으로 포함**:
   - **ArchUnit**: Spring Modulith의 `ModularityTest`와 한 묶음이라 추가 비용 낮음. Phase 0에 자연스럽게 포함.
   - **Event Catalog**: Spring Modulith의 `documenter` 기능을 활성화하면 자동 생성 가능. Outbox 패턴(esg-t2 계승)의 자연스러운 확장.

### 트레이드오프 / 양보한 것

- 4개 모두 동시 강화 시 Phase 0~2 부담이 매우 커짐 → 우선순위가 명확한 ABAC + Observability에 집중하고 ArchUnit/Event Catalog는 부수 항목으로.

---

## D6. 구축 접근 방식

**질문**: 거버넌스를 언제 어떻게 코드에 박을 것인가?

### 검토한 옵션

| ID | 옵션 | 핵심 내용 |
|---|---|---|
| D6-A | 거버넌스 우선 (Governance-First) | Phase 0~2에 운영·거버넌스 골격 완성. 이후 도메인 Phase는 검증된 골격 위에 쌓아 올림 |
| D6-B | 계층 병행 (Layered Parallel) | esg-t2 Phase 구조 따르되 각 Phase에 거버넌스 항목 함께 완성 |
| D6-C | 수직 슬라이스 (Vertical Slice) | UJ-01 전체를 얇게 관통한 후 슬라이스별 심화 |

### ✅ 선택: D6-A (거버넌스 우선)

### 선택 이유

1. **D1-A(운영·거버넌스 강화)와의 정합성**: 거버넌스를 사후 보강이 아니라 골격에 새겨야 일관성 유지.
2. **ABAC와 OTel은 횡단 관심사**: 도메인 코드보다 먼저 들어가야 비용이 낮음. Phase 5에 ABAC 도입하면 기존 컨트롤러를 모두 재작성해야 함.
3. **esg-t2가 운영 항목을 Phase 12로 몰아넣은 결과의 반대편**: esg-t2는 "마지막에 한 번에" 패턴, esg-t3는 "처음에 한 번에" 패턴.
4. **D6-C(수직 슬라이스)의 위험**: 첫 슬라이스의 보안·감사 무결성이 "사후 보강" 위험. esg-t2의 P0 원칙(테넌트 격리·감사 무결성)과 충돌.

### 트레이드오프 / 양보한 것

- 초반 진행 속도가 D6-B, D6-C보다 느림. "코드 0줄, 인프라 100%" 시점이 길어짐.
- 단, Phase 3 이후 도메인 구축 속도가 빨라짐 (골격이 검증돼 있으므로).

---

## D6 부수: 자연스럽게 포함되는 부차 항목

D5에서 선택되지 않았지만 D6-A 접근에서 비용이 낮아 자연스럽게 포함:

| 항목 | 이유 |
|---|---|
| ArchUnit + Modulith 자동 검증 | Phase 0에 `ModularityTest`와 한 묶음 |
| Event Catalog 자동 생성 | Phase 2에 Spring Modulith `documenter` 활성화로 자동 |
| OpenAPI DTO 사전 정의 | Gemini G-1 권고. Phase 0에 OpenAPI 어노테이션 의무화 |
| Spring Batch | Scope 3가 M+1로 미뤄지므로 Spring Batch도 함께 M+1 |

---

# Part 2. 설계 섹션

> 의사결정 위에 쌓아 올리는 설계. 섹션별로 사용자 승인 후 누적.

## 섹션 1. 비전 · 차별점 · MVP 경계

### 1.1 비전 한 줄

> **"운영·거버넌스가 가장 견고한 ESG 공시 데이터 플랫폼"** — 데이터 무결성에 더해 *누가 무엇을 언제 어떤 조건에서 접근/변경했는지*를 시스템 외부에서도 자동으로 증명할 수 있다.

### 1.2 esg-t2 대비 esg-t3 차별점 매트릭스

| 영역 | esg-t2 | esg-t3 |
|---|---|---|
| 권한 | RBAC 6역할 | **RBAC + ABAC 정책 엔진** (속성 기반: tenant·entity·year·sensitivity·approval_state) |
| 거버넌스 강제 | 문서 규칙 + 코드 리뷰 | **ArchUnit/Modulith 자동 검증 + Convention Test** (Executable Governance) |
| 관측성 | Prometheus 메트릭만 | **OpenTelemetry 완전 통합** (메트릭+분산 트레이싱+구조화 로그+알림) |
| 운영 절차 | spec.md 문구 일부 | **Runbook 시리즈** (Hash Chain 오류 / RLS 누락 / 재계산 / 백업복구 / 인시던트 SLA / 배포) |
| 이벤트 | Outbox만 명시 | **Event Catalog** + Outbox + Retry/DLQ + 멱등성 강제 |
| MVP 도메인 | Multi-Entity+Scope3+VW+Supply+정정·재공시 | **Multi-Entity+Scope1/2+VW+Evidence+Basic Report** (Scope 3·Supply·정정·재공시는 M+1) |
| 보고서 | PDF 생성 | PDF + **narrative draft 슬롯** (LLM 미사용, 수동 입력) — "문서 시스템 전환 준비" |

### 1.3 MVP 범위 (In / Out)

#### MVP In

- 다법인 등록·계층(지분율·운영통제)
- Scope 1, Scope 2 (Location-based / Market-based) 계산 — BigDecimal, factorAt 재현성
- 활동 데이터 입력 (Web UI + CSV) + Evidence 첨부 + SHA-256
- AuditLog Hash Chain + Outbox + @Auditable AOP
- **ABAC 정책 엔진** (Spring Security + YAML 정책 DSL)
- 외부 검증 워크스페이스 (Snapshot + VERIFIER + 코멘트)
- KSSB 2 보고서 PDF + narrative 슬롯
- **OpenTelemetry + Grafana 대시보드 + 알림**
- **Runbook 6종** (Hash Chain / RLS / 재계산 / 백업복구 / 인시던트 / 배포)

#### MVP Out → M+1 (도메인 확장)

- Scope 3 (Cat 1·2·11)
- 공급업체 포털 (SUPPLIER 역할 단순화 — 계정만 생성하고 비활성)
- 정정·재공시 워크플로우 (단순 INSERT-only는 유지하되 비교표 UI는 M+1)
- Formula DSL (Phase 5+에서 도입)
- iXBRL 렌더링
- AI/LLM (이상값 탐지, narrative 자동 생성)
- 다국적 Regulation Engine (CSRD/SSBJ)

#### MVP Out → Phase 8/M+1 (운영 안정화 후속)

- AuditLog DB 분리 (단일 DB 유지하되 파티셔닝만 준비)
- Spring Batch (Scope 3 도입 시점에 함께)

### 1.4 설계 원칙 (esg-t2 §11-A 계승 + 추가)

esg-t2의 8개 원칙을 모두 계승하고 다음 3개를 추가:

9. **정책 분리 (Policy Separation)**: 접근 제어 정책은 YAML로 외부화. 정책 변경에 코드 배포 불필요. 정책 평가는 PolicyEvaluator 순수 도메인 서비스.
10. **관측 가능성 우선 (Observability-First)**: 모든 도메인 작업은 OpenTelemetry Span/Metric/Log로 자동 계측. 운영팀이 코드 없이 시스템 상태를 진단 가능.
11. **운영 절차 코드화 (Runbook-as-Code)**: 운영 절차는 docs/runbook/에 마크다운 + 실행 가능한 진단/복구 스크립트로 보존. CI가 Runbook 누락을 검증.

---

## 섹션 2. 아키텍처 개요 & Spring Modulith 모듈 구조

### 2.1 최상위 패키지 & 모듈 (9개 — Spring Modulith 자동 인식 포함)

```
ai.claudecode.esgt3
├── iam/           ★ NEW: Identity & Access (Tenant·User·Role + ABAC 정책 엔진)
├── entity/        # 법인(LegalEntity) 관리 + 계층/연결 경계
├── audit/         # AuditLog + Hash Chain + Outbox (@Auditable AOP)
├── ghg/           # GHG 배출량 계산 (Scope 1/2 only; Scope 3는 M+1)
├── evidence/      ★ NEW(분리): 증빙 파일 + SHA-256 + Object Storage
├── vw/            # Verification Workspace (외부 검증인 Snapshot/Comment)
├── rpt/           # 보고서 생성 (KSSB 2 PDF + narrative 슬롯)
├── shared/        # 공통 Value Object, Event 기반 타입, Exception, Web 응답
└── observability/ # ★ Phase 0 갱신: OTel SDK 설정 등 cross-cutting (자동 모듈로 인식됨, §2.4 참조)
```

> **갱신 이력**: 초안에는 8개 모듈 + observability cross-cutting (config 패키지)로 설계했으나, Phase 0 실행 시 Spring Modulith 2.0.0이 `observability/`도 자동 모듈로 인식하여 **9개 모듈**로 확정. design 의도(cross-cutting)와 Modulith 동작이 정합화됨.

### 2.2 esg-t2 대비 모듈 구조 변경

| 변경 | 이유 |
|---|---|
| `iam/` 신설 (esg-t2는 `entity/` 안에 인증 혼재) | ABAC 정책 엔진을 위해 인증·인가 모듈 독립 — 정책 평가가 횡단이므로 명확한 경계 필요 |
| `evidence/` 분리 (esg-t2는 `ghg/` 안) | 증빙은 GHG·VW·rpt 모두가 참조 → 독립 모듈이 모듈 경계상 깨끗 |
| `supply/` 제거 | MVP Out (M+1) |
| 패키지 명 `entity` 유지 | esg-t2 컨벤션 계승 (Spring/JPA의 `@Entity`와 충돌하지 않도록 도메인 측 클래스명은 `LegalEntity*`) |

### 2.3 각 모듈 내부 표준 구조

```
moduleName/
├── api/        # 공개 인터페이스, DTO, Published Event 정의
├── domain/     # 순수 Java (Lombok 외 의존 없음) — Aggregate, VO, 도메인 서비스
├── infra/      # JPA Repository, 외부 시스템 어댑터, 메시징
├── internal/   # 패키지 비공개 구현 (다른 모듈에서 import 불가)
└── package-info.java  # @ApplicationModule + @NamedInterface 선언
```

### 2.4 횡단 관심사 (Cross-cutting)

- **observability/** — **Spring Modulith가 자동 모듈로 인식** (L3-P0-11). 최상위 앱 패키지 직하위 패키지는 `@ApplicationModule` 없어도 모듈로 카운트됨(L-P0-06). 따라서 **실제 모듈 수는 9개**(iam, entity, audit, ghg, evidence, vw, rpt, shared, observability). OpenTelemetry SDK 설정, Tracer/Meter, Custom Metric, Log MDC, ActuatorSecurityConfig(Phase 0 임시) 등을 담는다. Phase 1+ 진입 시 `@ApplicationModule(allowedDependencies = {})` 명시 권장.
- **governance/** (테스트 패키지 `src/test/java/.../governance`): ArchUnit 테스트, ModularityTest, Convention Test, Event Catalog 검증
- **docs/runbook/** (문서): 운영 절차 파일들

> **Phase 0 산출물 반영**: `ModularityTest.모듈_9개가_등록된다()` — observability 포함 9개 카운트.

### 2.5 데이터·이벤트 흐름

```
                ┌──────────────────────────────────────┐
                │            iam (ABAC 정책 엔진)         │
                │   Tenant·User·Role + PolicyEvaluator  │
                └─────────────┬────────────────────────┘
                              │ @PreAuthorize("@policy.allow(...)")
                              ▼
   ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
   │  entity  │───▶│   ghg    │───▶│   rpt    │───▶│    vw    │
   │LegalEntity│   │ Emission │    │Disclosure│    │ Snapshot │
   └──────────┘    └──────────┘    └──────────┘    └──────────┘
        │              │                │                │
        │              └────┐           │                │
        │                   ▼           │                │
        │              ┌──────────┐     │                │
        └─────────────▶│ evidence │◀────┘                │
                       │  File    │                       │
                       └────┬─────┘                       │
                            │                              │
                            ▼                              ▼
                  ┌───────────────────────────────────────────┐
                  │   audit (@Auditable AOP + Hash Chain)     │
                  │   모든 도메인 이벤트 수신 → AuditLog INSERT  │
                  └───────────────────────────────────────────┘
                                     │
                                     ▼
                  ┌───────────────────────────────────────────┐
                  │   observability (OTel: span·metric·log)    │
                  └───────────────────────────────────────────┘
```

### 2.6 모듈 간 의존 규칙 (ArchUnit으로 강제)

- **하향 의존만 허용**: shared ← (iam, entity, audit, evidence, ghg, vw, rpt). audit·observability은 모든 모듈이 의존 가능.
- **iam은 다른 도메인 모듈을 모름** (역의존 금지)
- **모듈 간 동기 호출**: `module.api.SomeFacade` 인터페이스만 import 가능. `module.domain`/`module.internal` import → ArchUnit 실패
- **모듈 간 비동기**: `ApplicationEventPublisher` + Spring Modulith `@ApplicationModuleListener`
- **JPA Repository는 `infra/` 외부 노출 금지**: 다른 모듈은 Repository를 직접 호출하지 못함

### 2.7 Spring Modulith 검증

- `ModularityTest`: 모듈 경계 자동 검증 (esg-t2 계승, 8개 모듈 카운트)
- **Event Catalog 자동 생성**: Spring Modulith의 `documenter.writeDocumentation()` 활용 — 모듈 간 이벤트 흐름 .puml 자동 생성 → CI에서 catalog 갱신 검증

---

## 섹션 3. ABAC 권한 모델

### 3.1 ABAC 도입 배경 — esg-t2 RBAC의 구체적 한계

| 한계 | 사례 |
|---|---|
| ESG_MANAGER 권한 폭증 | 한 사용자가 테넌트의 모든 법인·모든 회계연도·모든 활동 데이터에 접근 가능 |
| 자기 승인 방지 불가 | 본인이 작성한 데이터를 본인이 승인하는 SoD(분리 제약) 위반을 코드로 막을 수 없음 |
| 시점·승인 상태 무관 | 이미 외부 공시된 데이터(approval_state=DISCLOSED)도 ESG_MANAGER가 수정 가능 |
| VERIFIER 격리 어색 | esg-t2는 "지정 스냅샷 외 접근 불가"를 코드로 분산 구현. 정책 변경 시 매번 컨트롤러 수정 |

### 3.2 ABAC 핵심 개념 (XACML 단순화)

```
PolicyContext = (Subject, Resource, Action, Environment)

Subject  : userId, role, tenantId, assignedEntityIds, departmentId
Resource : type, tenantId, entityId, reportingYear, sensitivity, approvalState, createdBy
Action   : READ | WRITE | APPROVE | VERIFY | EXPORT | DELETE(거의 사용 안 함)
Environment : timestamp, requestIp, mfaVerified
```

### 3.3 정책 표현 방식 선택

| 옵션 | 장점 | 단점 |
|---|---|---|
| A. SpEL만 사용 | 의존성 0, Spring 기본 | 정책 분산, 추적 어려움 |
| B. OPA + Rego | 강력, 업계 표준 | 외부 데몬, Rego 학습, 운영 복잡 |
| **C. 자체 YAML 정책 DSL** ✅ | 정책값 분리 원칙 일치, Formula DSL 패턴 재사용, 도메인 친화 | 정책 엔진 자체 구현 필요 |

**선택 이유**: esg-t2의 Formula DSL 패턴을 정책에도 동일 적용. 정책=데이터=설정 외부화의 일관성.

### 3.4 핵심 컴포넌트

```
iam.domain
├── PolicyContext       (Subject, Resource, Action, Environment VO)
├── PolicyDecision      (PERMIT | DENY | NOT_APPLICABLE + reason + policyId)
├── PolicyDocument      (YAML 파싱 결과 VO)
├── PolicyEvaluator     (순수 도메인 서비스 — DB 의존 없음)
└── PolicyRegistry      (메모리 정책 캐시 + 핫리로드)

iam.infra
├── PolicyYamlLoader    (resources/policies/*.yaml → PolicyDocument)
├── PolicyDecisionLogger (모든 PERMIT/DENY를 audit 모듈로 발행)
└── PolicyHotReloader   (파일 변경 감지 → 캐시 갱신)

iam.api
├── PolicyFacade        (다른 모듈에서 사용: policy.allow(ctx))
└── @AbacCheck          (메서드 어노테이션 — 컨트롤러에 부착)
```

### 3.5 정책 YAML 예시 (실제 사용 사례)

```yaml
# policies/iam/esg-manager.yaml
policies:
  - id: esg-manager-write-own-entity
    description: ESG_MANAGER는 담당 법인의 DRAFT/REJECTED 활동 데이터만 입력·수정
    effect: PERMIT
    when:
      subject.role: ESG_MANAGER
      subject.tenantId: "${resource.tenantId}"
      subject.assignedEntityIds: { contains: "${resource.entityId}" }
      action: [WRITE, READ]
      resource.type: ActivityData
      resource.approvalState: [DRAFT, REJECTED]

  - id: esg-manager-self-approval-prohibition
    description: 자기 작성 데이터 자기 승인 금지 (Segregation of Duties)
    effect: DENY
    priority: 100  # 더 높은 우선순위로 PERMIT 정책 위에서 검사
    when:
      subject.role: ESG_MANAGER
      action: APPROVE
      resource.createdBy: "${subject.userId}"

  - id: verifier-snapshot-only
    description: VERIFIER는 지정된 Snapshot만 읽기 + 코멘트
    effect: PERMIT
    when:
      subject.role: VERIFIER
      subject.assignedSnapshotIds: { contains: "${resource.snapshotId}" }
      action: [READ, COMMENT, SIGN]
      resource.type: VerificationSnapshot

  - id: disclosed-data-immutability
    description: 공시 완료된 데이터는 누구도 WRITE 불가 (SUPER_ADMIN도)
    effect: DENY
    priority: 200  # 가장 높은 우선순위
    when:
      action: [WRITE, DELETE]
      resource.approvalState: DISCLOSED

  - id: emergency-lockdown
    description: Hash Chain 오류·RLS 누락 등 비상 시 특정 tenant WRITE 차단
    effect: DENY
    priority: 250  # 최고 우선순위 (어떤 정책보다 위)
    when:
      action: [WRITE, DELETE, APPROVE]
      resource.tenantId: { in: "${env.lockedTenantIds}" }  # 시스템 속성에서 동적 로드
```

**`emergency-lockdown` 활성화/해제 절차**:
- 활성화: `AuditIntegrityScheduler`가 불일치 발견 시 자동, 또는 SUPER_ADMIN이 `POST /api/v1/admin/lockdown/{tenantId}` 호출
- 해제: Runbook 01·02 절차 완료 후 SUPER_ADMIN이 `DELETE /api/v1/admin/lockdown/{tenantId}` 호출 (감사 사유 reason 필수)
- 활성화·해제 모두 AuditLog 의무 기록 (`iam.LockdownActivated`, `iam.LockdownReleased` 이벤트)

### 3.6 6개 역할 + ABAC 속성 매핑

| 역할 | RBAC 기본 권한 | ABAC 추가 제약 |
|---|---|---|
| SUPER_ADMIN | 전역 관리 | 데이터 WRITE는 차단(read-only). 정책 변경은 별도 정책 파일로 |
| TENANT_ADMIN | 테넌트 내 관리 | tenantId 일치 + 본인 테넌트 사용자만 |
| ESG_MANAGER | 데이터 입력·승인 | tenantId + 담당 entityId + DRAFT/REJECTED만 수정 + 자기 승인 금지 |
| ESG_VIEWER | 읽기 전용 | tenantId 일치 + sensitivity ≤ INTERNAL (CONFIDENTIAL 차단) |
| VERIFIER | Snapshot 열람·코멘트 | 지정 snapshotId 외 차단 (RLS + ABAC 이중) |
| SUPPLIER | (MVP 비활성) | 계정 모델만 존재, 정책은 M+1 |

### 3.7 평가 흐름

```
HTTP 요청
   │
   ▼
[1] JwtAuthFilter
      → JwtAuthentication 추출 (userId, role, tenantId, assignedEntityIds, ...)
   │
   ▼
[2] TenantContextInterceptor  ← esg-t2 계승
      → PostgreSQL: SELECT set_config('app.current_tenant_id', ?, true)
      → RLS 자동 적용 (1차 방어선)
   │
   ▼
[3] Controller 메서드 @PreAuthorize("@policy.allow(authentication, 'WRITE', #req)")
      → PolicyFacade.allow(...) 호출
   │
   ▼
[4] PolicyEvaluator.evaluate(PolicyContext)
      → PolicyRegistry에서 매칭 정책 조회 (priority 내림차순)
      → 첫 DENY 발견 시 즉시 DENY 반환
      → 모든 정책 통과 후 PERMIT 정책 매칭 시 PERMIT
      → 매칭 없으면 NOT_APPLICABLE → 기본 정책에 따라 DENY
   │
   ▼
[5] PolicyDecisionLogger → audit 모듈로 이벤트 발행
      → AuditLog INSERT (정책 평가도 Hash Chain에 포함)
   │
   ▼
[6] PERMIT이면 컨트롤러 본문 실행. DENY면 AccessDeniedException → 403
```

### 3.8 RLS와의 관계 (이중 방어)

- **RLS (1차)**: DB 레벨에서 `tenant_id` 격리. esg-t2 계승. 애플리케이션 코드 버그로도 우회 불가.
- **ABAC (2차)**: 같은 테넌트 내 더 세밀한 조건 (entityId, year, sensitivity, approvalState). 정책 외부화로 변경 비용 낮음.
- **둘 다 통과해야 데이터 접근 가능**. 한 쪽이 실패해도 데이터 누출 0.

### 3.9 성능 & 운영

- **정책 캐싱**: 메모리 캐시. 파일 변경 시 핫리로드 (Spring `FileSystemWatcher`).
- **PolicyEvaluator는 순수**: DB 접근 없음. Subject·Resource 속성은 호출 측이 prefetch (N+1 방지).
- **정책 평가 메트릭**: OTel Span으로 매 평가 추적. p95 < 1ms 목표.
- **정책 변경 워크플로우**:
  1. 정책 YAML 수정 → PR.
  2. CI에서 정책 단위 테스트 통과 검증.
  3. SUPER_ADMIN 승인 → 배포.
  4. 정책 변경 자체도 AuditLog 기록 (정책=감사 대상).

### 3.10 정책 단위 테스트 (TDD)

```java
@Test
void esg_manager는_자기_작성_데이터를_승인할_수_없다() {
    var ctx = PolicyContext.builder()
        .subject(Subject.of("user-1", ESG_MANAGER, "tenant-1"))
        .resource(Resource.activityData("tenant-1", "entity-1").createdBy("user-1"))
        .action(APPROVE)
        .build();
    
    var decision = policyEvaluator.evaluate(ctx);
    
    assertThat(decision.effect()).isEqualTo(DENY);
    assertThat(decision.policyId()).isEqualTo("esg-manager-self-approval-prohibition");
}
```

정책 파일에 `tests:` 섹션을 두고 esg-t2의 Formula DSL `test_cases` 패턴 재사용 가능.

### 3.11 esg-t2 대비 변경

| esg-t2 | esg-t3 |
|---|---|
| `@PreAuthorize("hasRole('TENANT_ADMIN')")` 분산 | `@PreAuthorize("@policy.allow(authentication, #action, #resource)")` 중앙화 |
| `SnapshotSecurity.canAccess()` Bean 분산 | 모든 권한 결정이 PolicyEvaluator 단일 진입점 |
| RBAC만 코드, 정책 변경 시 재배포 | 정책 YAML로 외부화, 핫리로드 |
| 정책 평가가 AuditLog에 기록되지 않음 | 모든 PERMIT/DENY가 AuditLog에 기록 |

---

## 섹션 4. AuditLog & Hash Chain

### 4.1 esg-t2에서 계승하는 것 (변경 없음)

| 항목 | esg-t2 패턴 | esg-t3 유지 |
|---|---|---|
| @Auditable AOP | `AuditAspect` Around advice — Phase 2에 완성 후 모든 데이터 변경에 적용 | ✅ 그대로 |
| Outbox Pattern | audit_logs + outbox_events 같은 트랜잭션 INSERT, 비동기 폴러가 이벤트 발행 | ✅ 그대로 (확장됨) |
| Hash Chain | SHA-256, PESSIMISTIC_WRITE 락, 테넌트별 독립 체인 | ✅ 그대로 |
| canonicalPayload() | 저장/검증 경로가 동일 직렬화 메서드를 공유 (L-0-08 교훈) | ✅ 그대로 |
| AuditIntegrityScheduler | 매일 새벽 2시(Asia/Seoul) Hash Chain 전수 검증 | ✅ 그대로 |
| Append-only Repository | `Repository<T,ID>` 마커 인터페이스 — delete* 미노출 (L-0-14 교훈) | ✅ 그대로 |

### 4.2 esg-t3 추가/강화 (5가지)

**강화 1: PolicyDecision도 AuditLog 대상**
- esg-t2는 데이터 변경만 감사. esg-t3는 권한 평가 결과(PERMIT/DENY)도 감사
- DENY는 보안 사건 → 분석 가능
- 정책 자체 변경(YAML 갱신)도 AuditLog (정책=데이터)

**강화 2: OpenTelemetry 통합 — Trace 역추적**
- `audit_logs` 테이블에 `trace_id`, `span_id` 컬럼 추가
- AuditAspect가 현재 OTel Span에서 자동 추출
- 운영자가 Grafana/Tempo에서 trace_id로 전체 요청 흐름 역추적

**강화 3: Outbox Retry/DLQ/멱등성**

```
outbox_events.status: PENDING → PROCESSING → PUBLISHED
                                          ↘ FAILED(retry_count++) → next_retry_at(지수 백오프)
                                                                  ↘ retry_count > 5: DLQ
```

- `FOR UPDATE SKIP LOCKED`로 멀티 인스턴스 안전
- 멱등성: `processed_events` 테이블 + (tenant_id, idempotency_key) UNIQUE
- DLQ 진입 시 `audit.OutboxDlqEntered` 이벤트 → 자동 알림 + Runbook 링크

**강화 4: Hash Chain 오류 자동 대응**
1. `AuditIntegrityScheduler` 불일치 발견 → `audit.HashChainMismatchDetected` 이벤트
2. 알림 모듈이 PagerDuty/Slack 발송 + Runbook URL 포함
3. **자동 격리 정책**: `emergency-lockdown` ABAC 정책이 해당 tenant_id WRITE 차단
4. Runbook 진단 스크립트: `./scripts/audit-diagnose.sh <tenant_id>` — 원인이 직렬화 불일치인지 직접 조작인지 분류

**강화 5: AuditLog 파티셔닝 준비**

```sql
CREATE TABLE audit_logs (
  ...
  occurred_at TIMESTAMPTZ NOT NULL
) PARTITION BY RANGE (occurred_at);

CREATE TABLE audit_logs_2026 PARTITION OF audit_logs
  FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
```

- MVP에서는 연 단위 파티션
- M+1에서 월 단위 파티션 + 자동 생성 스케줄러 + 콜드 스토리지 이전
- **단일 DB 유지** (ChatGPT의 "DB 분리" 권고는 부적용 — 단, 파티셔닝으로 동일 효과 일부 달성)

### 4.3 핵심 테이블 스키마

```sql
-- 감사 로그 (테넌트별 Hash Chain)
CREATE TABLE audit_logs (
  id BIGSERIAL,
  tenant_id UUID NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  actor_user_id UUID,
  actor_role VARCHAR(32),
  event_type VARCHAR(64) NOT NULL,
  resource_type VARCHAR(64),
  resource_id VARCHAR(128),
  payload_json JSONB NOT NULL,
  prev_hash BYTEA,
  current_hash BYTEA NOT NULL,
  policy_decision_id BIGINT,
  trace_id VARCHAR(64),
  span_id VARCHAR(32),
  PRIMARY KEY (id, occurred_at)
) PARTITION BY RANGE (occurred_at);

ALTER TABLE audit_logs ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON audit_logs USING
  (tenant_id = current_setting('app.current_tenant_id')::uuid);

CREATE TABLE outbox_events (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  payload_json JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
  retry_count INT NOT NULL DEFAULT 0,
  last_error TEXT,
  next_retry_at TIMESTAMPTZ,
  CHECK (status IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'DLQ'))
);

CREATE TABLE processed_events (
  tenant_id UUID NOT NULL,
  idempotency_key VARCHAR(128) NOT NULL,
  processed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (tenant_id, idempotency_key)
);

CREATE TABLE policy_decisions (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  occurred_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  subject_user_id UUID,
  action VARCHAR(32),
  resource_type VARCHAR(64),
  resource_id VARCHAR(128),
  effect VARCHAR(16) NOT NULL,
  policy_id VARCHAR(128),
  reason TEXT,
  CHECK (effect IN ('PERMIT', 'DENY', 'NOT_APPLICABLE'))
);
```

### 4.4 Event Catalog (Spring Modulith documenter 활용)

```
docs/event-catalog/
├── README.md                       # 이벤트 카탈로그 개요
├── _generated/                     # CI가 매 빌드마다 생성
│   ├── modulith-events.puml
│   └── modulith-events.adoc
└── conventions.md                  # 네이밍 컨벤션, 멱등성 규칙
```

**네이밍 컨벤션**: `<module>.<Resource><PastTenseVerb>` (예: `entity.LegalEntityCreated`, `iam.PolicyDenied`)

**이벤트 클래스 표준**:

```java
public record LegalEntityCreated(
    String tenantId,
    UUID entityId,
    Instant occurredAt,
    String idempotencyKey   // 모든 이벤트 의무 필드
) implements DomainEvent {}
```

`DomainEvent` 마커 인터페이스 + ArchUnit 테스트로 의무화.

### 4.5 운영 시나리오 — Hash Chain 오류 발생

```
[02:00 KST] AuditIntegrityScheduler 실행
  ↓
[02:03] tenant-7 의 audit_logs[id=12345] hash mismatch 감지
  ↓
[02:03:01] audit.HashChainMismatchDetected 이벤트 발행
  ↓
[02:03:02] 알림 모듈 → Slack #esg-ops + PagerDuty Critical
  ↓ 알림 본문에 포함:
  │   - Tenant ID: 7
  │   - AuditLog ID: 12345
  │   - Runbook: docs/runbook/01-hash-chain-mismatch.md
  │   - 진단 명령: ./scripts/audit-diagnose.sh 7
  ↓
[02:03:02] emergency-lockdown 정책 활성화 — tenant-7 WRITE 차단
  ↓
[운영자 개입]
  - ./scripts/audit-diagnose.sh 7 실행
  - 원인 분류:
    - (A) 직렬화 불일치 (버그): canonicalPayload 재실행 → 해시 재생성 → SUPER_ADMIN 승인
    - (B) DB 직접 조작 (위변조): 백업으로 복원 → 감사 기관 통보
  - Runbook의 결정 트리 따라 처리
```

### 4.6 esg-t2 대비 차이 요약

| 영역 | esg-t2 | esg-t3 |
|---|---|---|
| 감사 대상 | 데이터 변경 | 데이터 변경 + 정책 평가 + 정책 변경 |
| Trace 역추적 | 불가 | OTel trace_id/span_id 통합 |
| Outbox 실패 | 단순 재시도 | Retry → 지수 백오프 → DLQ → 알림 |
| 멱등성 | 어노테이션 기반 | DB 레벨 UNIQUE 강제 |
| Hash Chain 오류 | 로그만 | 자동 알림 + 자동 격리 정책 + Runbook 링크 |
| 테이블 구조 | 단일 테이블 | 파티션 테이블 (연 단위 시작) |
| Event Catalog | 없음 | CI 자동 생성 + 네이밍 컨벤션 강제 |

---

## 섹션 5. Observability 설계

### 5.1 esg-t2 vs esg-t3 관측성

| 영역 | esg-t2 | esg-t3 |
|---|---|---|
| 메트릭 | Prometheus만 | **OTel SDK + Prometheus exporter** (OTel 우선) |
| 트레이싱 | 없음 | **OTel + Tempo** (분산 트레이싱) |
| 로그 | 평문 + Logback | **JSON 구조화 + Loki** + MDC 표준화 |
| 알림 | 없음 | **Grafana Alerting** + PagerDuty/Slack |
| MDC | tenantId만 | traceId, spanId, tenantId, userId, requestId |
| AuditLog 연계 | 없음 | trace_id가 audit_logs에 저장 (역추적) |
| 대시보드 | 없음 | Grafana JSON 코드 관리 (PR로 변경) |

### 5.2 기술 스택

```
┌─────────────────────────────────────────────────────┐
│                  esg-t3 Application                  │
│  ┌────────────────────────────────────────────┐    │
│  │   OpenTelemetry SDK (자동 + 수동 계측)        │    │
│  └────────────────────────────────────────────┘    │
│           │ OTLP gRPC                                │
└───────────┼──────────────────────────────────────────┘
            ▼
   ┌─────────────────────────┐
   │   OTel Collector         │
   │   (수집·필터·변환·라우팅)   │
   └────┬──────────┬─────┬────┘
        │          │     │
        ▼          ▼     ▼
   ┌────────┐  ┌──────┐ ┌─────────┐
   │  Tempo │  │ Loki │ │Prometheus│
   │ (Trace)│  │ (Log)│ │ (Metric) │
   └────┬───┘  └───┬──┘ └────┬─────┘
        └──────────┴────────┘
                  │
                  ▼
            ┌──────────┐
            │ Grafana  │ ── Alerting → PagerDuty / Slack
            └──────────┘
```

로컬 dev 환경: docker-compose에 OTel Collector + Tempo + Loki + Prometheus + Grafana 포함.

### 5.3 OpenTelemetry 자동 계측 (Spring Boot 4 내장)

- HTTP Server (Spring MVC) — 모든 요청에 span
- JDBC — Hibernate 쿼리 span
- HTTP Client — 외부 호출 span
- Spring Modulith Event — 이벤트 발행/수신 span

### 5.4 수동 계측 (핵심 도메인 작업)

| 모듈 | Span 이름 | Attribute |
|---|---|---|
| iam | `iam.evaluate_policy` | tenant.id, subject.user_id, action, resource.type, effect |
| audit | `audit.write_log` | tenant.id, event_type, hash_chain.prev_hash_id |
| audit | `audit.verify_chain` | tenant.id, log_count, mismatch_count |
| ghg | `ghg.calculate_scope1` | tenant.id, entity.id, reporting.year, factor_count |
| ghg | `ghg.calculate_scope2` | tenant.id, entity.id, reporting.year, method |
| evidence | `evidence.upload_file` | tenant.id, file_size_bytes, sha256 |
| vw | `vw.create_snapshot` | tenant.id, report.id, data_points_count |
| rpt | `rpt.render_pdf` | tenant.id, report.id, page_count |

Span 네이밍 컨벤션: `<module>.<verb_resource>` (snake_case). ArchUnit으로 강제.

### 5.5 커스텀 메트릭

```
# 비즈니스 메트릭
esg_t3_emission_calculation_duration_seconds{scope, method, status}    histogram
esg_t3_policy_evaluation_duration_seconds{effect, role}                histogram
esg_t3_policy_evaluation_total{effect, role, policy_id}                counter
esg_t3_audit_outbox_lag_seconds{tenant_id}                             gauge
esg_t3_audit_outbox_dlq_total{event_type}                              counter
esg_t3_hash_chain_integrity_check_total{result}                        counter
esg_t3_evidence_upload_bytes_total{tenant_id}                          counter
esg_t3_active_tenants_total                                            gauge
esg_t3_snapshot_creation_duration_seconds                              histogram
esg_t3_report_generation_duration_seconds{format}                      histogram

# 시스템 메트릭 (Spring Boot Actuator 자동)
jvm_memory_used_bytes
hikaricp_connections_active
http_server_requests_seconds
```

### 5.6 구조화 로그 (JSON + MDC)

```json
{
  "@timestamp": "2026-05-26T14:23:45.123Z",
  "level": "INFO",
  "logger": "ai.claudecode.esgt3.ghg.domain.EmissionCalculator",
  "thread": "http-nio-8080-exec-3",
  "message": "Scope 1 calculation completed",
  "traceId": "5b8aa5a2d2c872e8321cf37308d69df2",
  "spanId": "051581bf3cb55c13",
  "tenantId": "7c9e6679-...",
  "userId": "user-42",
  "requestId": "req-a1b2",
  "entityId": "entity-3",
  "reportingYear": 2026,
  "emissionTons": "1234.56"
}
```

**MDC 필드 (의무)**: `traceId`, `spanId`, `tenantId`, `userId`, `requestId`.

**MDC 주입 책임**:
- `traceId`/`spanId`: OTel agent 자동
- `tenantId`/`userId`: `TenantContextInterceptor` (esg-t2 계승)
- `requestId`: `RequestIdFilter` (신규, X-Request-Id 헤더 → 없으면 UUID 생성)

ArchUnit 강제: `LoggerFactory.getLogger()`만 허용. `System.out`, `printStackTrace` 금지.

### 5.7 알림 규칙

| 알림 | 조건 | 채널 | Runbook |
|---|---|---|---|
| **CRIT: Hash Chain 불일치** | `hash_chain_integrity_check_total{result="mismatch"} > 0` | PagerDuty + Slack | `runbook/01-hash-chain-mismatch.md` |
| **CRIT: Outbox DLQ 진입** | `rate(audit_outbox_dlq_total[5m]) > 0` | PagerDuty + Slack | `runbook/02-outbox-dlq.md` |
| **CRIT: AuditLog 누락 의심** | `audit_outbox_lag_seconds > 60` (5분 지속) | PagerDuty + Slack | `runbook/03-audit-lag.md` |
| WARN: 정책 DENY 급증 | `denied_ratio > 0.1` (5분 평균) | Slack | `runbook/04-policy-denial-spike.md` |
| WARN: API 응답 P95 | `http_server_requests_seconds_p95 > 0.5s` | Slack | — |
| WARN: 보고서 생성 지연 | `report_generation_duration_p95 > 1.5s` | Slack | — |
| WARN: JWT 인증 실패 급증 | `rate(jwt_failures[1m]) > 100` | Slack | `runbook/05-auth-attack.md` |
| WARN: DB 연결 풀 임계 | `hikaricp_connections_active / max > 0.95` | Slack | — |

알림 규칙은 코드로 관리: `infra/observability/alerts/*.yml`.

### 5.8 Grafana 대시보드 (5개)

| 대시보드 | 내용 | 주 사용자 |
|---|---|---|
| `01-platform-overview` | 활성 테넌트, API 요청량, 에러율, 응답 시간 | 전체 |
| `02-tenant-detail` | 테넌트 단위 활동 데이터 입력, 보고서 생성, AuditLog 추이 | 운영자 |
| `03-ghg-calculation` | Scope 1/2 계산 처리량, 배출계수 적중률, 재계산 빈도 | ESG/도메인 |
| `04-audit-integrity` | Hash Chain 검증 결과, Outbox 상태, 정책 평가 통계 | 보안/감사 |
| `05-system-health` | JVM, DB 연결 풀, GC, OTel Collector 자체 상태 | 인프라 |

모든 대시보드 JSON은 `infra/observability/dashboards/`에 코드로 관리. PR로 변경 추적.

### 5.9 데이터 보존

| 시그널 | 보존 기간 | 저장소 |
|---|---|---|
| 메트릭 | 30일 (고해상도) + 1년 (다운샘플링) | Prometheus / Mimir |
| 트레이싱 | 7일 | Tempo (비용 절감) |
| 로그 | 30일 | Loki |
| **AuditLog** | **10년 이상** | PostgreSQL (Observability와 별개) |

AuditLog는 관측 시그널과 분리하여 법적 보존 의무를 충족.

### 5.10 SLO / SLI

| SLI | 측정 | SLO 목표 |
|---|---|---|
| API 가용성 | `1 - 5xx 비율` | 99.5% / 30일 |
| API 응답시간 P95 | `http_server_requests_seconds_p95` | ≤ 500ms |
| 보고서 생성 P95 | `report_generation_duration_seconds_p95` | ≤ 1500ms |
| Hash Chain 검증 완료율 | 일 1회 스케줄 성공 | 100% |
| AuditLog 누락 | `audit_outbox_dlq_total` | 0건/월 |

SLO 위반 시 자동 알림 + 분기 1회 SLO 보고서 생성.

### 5.11 에러 응답에 traceId 노출

```json
{
  "error": "POLICY_DENIED",
  "message": "공시 완료된 데이터는 수정할 수 없습니다.",
  "traceId": "5b8aa5a2d2c872e8321cf37308d69df2",
  "timestamp": "2026-05-26T14:23:45Z"
}
```

사용자가 지원 문의 시 traceId만 전달하면 운영자가 Tempo에서 전체 요청 흐름 + AuditLog 역추적 가능.

### 5.12 관측성 모듈 패키지 구조

```
ai.claudecode.esgt3.observability  (모듈이 아닌 config 패키지)
├── OtelConfig                  # OTel SDK 빈 구성
├── MetricsConfig                # 커스텀 메트릭 등록
├── LoggingConfig                # JSON 인코더, MDC 필터
├── RequestIdFilter              # X-Request-Id 처리
└── SlowQueryDetector            # JDBC slow query → Span event

infra/observability/
├── docker-compose.observability.yml   # 로컬 OTel 스택
├── dashboards/                        # Grafana JSON
├── alerts/                            # Prometheus alerts
└── otel-collector-config.yaml         # OTel Collector 라우팅
```

---

## 섹션 6. Runbook 6종 구조

### 6.1 Runbook-as-Code 원칙

1. **마크다운 절차서** (`docs/runbook/*.md`) — 사람이 따라하는 결정 트리
2. **진단·복구 스크립트** (`scripts/runbook/*.sh`) — 마크다운에서 직접 호출
3. **알림 자동 첨부** — Alertmanager가 알림 본문에 Runbook URL 추가
4. **분기 1회 Game Day** — 의도적 사고를 일으켜 Runbook 실행 훈련
5. **CI 검증** — 알림 규칙에 명시된 Runbook 파일이 실제 존재하는지 빌드 시 확인

### 6.2 Runbook 표준 구조 (모든 Runbook 동일)

- 트리거, 심각도, RTO, 마지막 훈련 일자, 소유자
- 영향 평가 (Blast Radius)
- 진단 (Diagnosis) — 즉시 확인 명령 + 관련 메트릭/로그
- 결정 트리 (분기 조건)
- 복구 절차 (단계별 명령)
- 사후 조치 (포스트모템, AuditLog 재검증)
- 에스컬레이션 (시간 임계별)
- 관련 자료 (ADR, 과거 사례)

### 6.3 6종 Runbook 개요

| # | 파일 | 트리거 | 심각도 | RTO |
|---|---|---|---|---|
| 01 | `01-hash-chain-mismatch.md` | `hash_chain_integrity_check_total{result="mismatch"} > 0` | P1 | 4시간 |
| 02 | `02-rls-leak.md` | tenant 간 데이터 노출 의심 | P1 | 즉시 |
| 03 | `03-recalculation-failure.md` | `emission_calculation_failures_total` 임계 초과 | P2 | 24시간 |
| 04 | `04-backup-recovery.md` | 백업 손실·DR 발동 | P1 | 4시간 (esg-t2 §5.2 계승) |
| 05 | `05-incident-sla.md` | 모든 인시던트의 메타 Runbook | — | — |
| 06 | `06-deployment.md` | 배포 절차 + 롤백 | — | 15분 |

### 6.4 각 Runbook 핵심 요약

**Runbook 01 — Hash Chain 무결성 오류**
- 트리거: 매일 02:00 KST 스케줄러 또는 정책 평가 시 hash mismatch
- 자동 격리: `emergency-lockdown` ABAC 정책 즉시 활성화 (해당 tenant_id WRITE 차단)
- 결정 트리: 직렬화 불일치(버그) vs DB 직접 조작(위변조)
- 자동 복구 (직렬화): `canonicalPayload()` 재실행 → 해시 재생성 → SUPER_ADMIN 승인
- 수동 복구 (조작): 백업 복원 + 법무·감사 통보 + 규제 기관 보고

**Runbook 02 — RLS 누락/유출 의심**
- 트리거: 감사·외부 보고로 발견 (자동 탐지 어려움)
- 즉시 조치: 해당 tenant_id 모든 세션 강제 만료 + 신규 로그인 차단
- 진단: TenantContextInterceptor 실행 로그, `app.current_tenant_id` SET 누락 SQL 탐색, 의심 trace_id 추적
- 복구: 누출 범위 파악 → 사용자 통보 → 정책 재검토 → 정기 침투 테스트
- 재발 방지: ArchUnit 규칙 추가 (Repository 메서드에 RLS 가정 테스트)

**Runbook 03 — 재계산 실패**
- 트리거: 배출계수 갱신 후 재계산 잡 실패, 또는 정정 워크플로우(M+1) 오류
- 진단: 어느 활동 데이터·어느 계수·어느 산식 버전에서 실패했는지 식별
- 결정 트리: 데이터 / 계수 / 산식 / 인프라
- 부분 재계산: 영향 받은 활동 데이터만 (전체 재실행 피함)
- 무결성: 재계산 결과를 AuditLog 기록, 이전 결과 INSERT-only 보존

**Runbook 04 — 백업·복구 (DR)**
- esg-t2 §5.2 계승: RTO 4시간, RPO 1시간, 일 4회 스냅샷
- 복구 우선순위: AuditLog → Snapshot → ActivityData → 사용자·정책
- Hash Chain 재검증: 복구 직후 전 테넌트 재검증 의무 (검증 통과 후에만 WRITE 재개)
- 분기 1회 Game Day로 실제 시연

**Runbook 05 — 인시던트 SLA (메타 Runbook)**
- P1: 데이터 무결성·보안 — 15분 인지, 1시간 1차 대응, 4시간 복구
- P2: 핵심 기능 중단 — 30분 인지, 2시간 1차 대응, 24시간 복구
- P3: 일부 기능 장애 — 1시간 인지, 4시간 1차 대응, 1주 복구
- P4: 경미한 이슈 — 1일 인지, 1주 복구
- 커뮤니케이션 템플릿: 초기 알림 / 상태 업데이트 / 해결 보고
- P1·P2 포스트모템 의무 (`docs/postmortems/`)

**Runbook 06 — 배포 절차**
- 사전 체크: ArchUnit, ModularityTest, Hash Chain 검증, 정책 단위 테스트, DB 마이그레이션 dry-run
- 단계: Flyway → Blue-Green 배포 → 카나리 5% → 100% → 이전 버전 24시간 보존
- 롤백 트리거: 5xx 1% 이상, AuditLog 누락, Hash Chain 불일치
- 배포 자체가 AuditLog 대상: `infra.DeploymentApplied` 이벤트

### 6.5 스크립트 디렉터리 구조

```
scripts/runbook/
├── 01-diagnose-hash-chain.sh
├── 01-recover-canonical.sh
├── 02-revoke-tenant-sessions.sh
├── 02-rls-trace-analyze.sh
├── 03-partial-recalculate.sh
├── 04-restore-from-backup.sh
├── 04-verify-hash-chain-all.sh
├── 05-create-incident-ticket.sh
├── 06-deploy-canary.sh
└── lib/
    ├── common.sh
    ├── psql-helpers.sh
    └── audit-helpers.sh
```

스크립트 표준: bash + `set -euo pipefail`, 모든 변경에 `--dry-run`, 실행 결과를 AuditLog 기록(`runbook.ScriptExecuted`), 권한 확인.

### 6.6 Game Day (운영 단계 분기 1회 훈련)

**시점**:
- **Phase 8 (개발 종료 직전)**: 4개 시나리오 중 1개 선택 시연 (운영 투입 전 검증)
- **운영 시작 후**: 매 분기 1회씩 4개 시나리오를 순환 실행 (Q1→Q2→Q3→Q4→Q1...)

| 분기 | 시나리오 | 측정 |
|---|---|---|
| Q1 | Hash Chain 위변조 (DB 직접 UPDATE) | 탐지 시간, 복구 시간, 통보 정확성 |
| Q2 | tenant 간 RLS 누락 (정책 일시 제거) | 탐지 가능 여부, 영향 범위 파악 |
| Q3 | 재계산 실패 (잘못된 배출계수 주입) | 부분 재계산 정확성, 무결성 보장 |
| Q4 | DR 시뮬레이션 (DB 전체 손실) | RTO 4시간 달성 여부 |

Game Day 후 의무: Runbook 갱신, 스크립트 개선, 포스트모템 작성.

### 6.7 알림과 Runbook 연결

```yaml
# infra/observability/alerts/audit.yml
groups:
  - name: audit
    rules:
      - alert: HashChainMismatch
        expr: esg_t3_hash_chain_integrity_check_total{result="mismatch"} > 0
        for: 0m
        labels:
          severity: critical
          team: esg-ops
          runbook: "https://github.com/.../docs/runbook/01-hash-chain-mismatch.md"
        annotations:
          summary: "Hash Chain 무결성 오류 — tenant_id={{ $labels.tenant_id }}"
          description: |
            AuditLog Hash Chain 검증 실패. 즉시 Runbook 01 참조.
            진단 명령: ./scripts/runbook/01-diagnose-hash-chain.sh {{ $labels.tenant_id }}
            대시보드: https://grafana.../d/04-audit-integrity
```

CI 강제: `tests/runbook-link-validation.sh`이 모든 알림의 runbook URL이 실제 파일을 가리키는지 검증. 누락 시 빌드 실패.

### 6.8 esg-t2 대비 차이

| 영역 | esg-t2 | esg-t3 |
|---|---|---|
| 운영 절차 위치 | spec.md 일부 문장 | `docs/runbook/` 6개 독립 파일 |
| 절차 형식 | 산문 | 트리거·진단·결정 트리·복구·사후 표준 구조 |
| 실행 도구 | 수동 | 진단·복구 스크립트 포함 |
| 알림 연계 | 알림 자체 없음 | 알림 본문에 Runbook URL 자동 첨부 |
| 훈련 | 분기 1회 백업 복구만 | 분기 1회 Game Day (4가지 시나리오 순환) |
| CI 검증 | 없음 | Runbook 링크 유효성, 스크립트 신택스 검증 |

---

## 섹션 7. 데이터 모델 핵심 ERD

### 7.1 설계 원칙 (esg-t2 §11-A 계승 + 강화)

| 원칙 | 적용 |
|---|---|
| **INSERT-only (Append-only)** | activity_data, emission_records, audit_logs, snapshots는 UPDATE/DELETE 금지 |
| **BigDecimal 전용** | 모든 수치 컬럼은 `NUMERIC(20, 6)` |
| **RLS 의무** | 모든 도메인 테이블에 `tenant_id UUID NOT NULL` + RLS 정책 |
| **factorAt 재현성** | `emission_factors`에 `effective_from / effective_to` |
| **M+1 컬럼 슬롯** | scope, version, reason_code, narrative_id, supplier_id 등을 MVP에 사전 포함 |
| **파티셔닝 준비** | audit_logs는 RANGE 파티션, 다른 큰 테이블은 컬럼만 준비 |

### 7.2 모듈별 핵심 엔티티

```
iam:    tenants, users, user_roles, user_entity_assignments, policy_documents, policy_decisions
entity: legal_entities, entity_relationships
audit:  audit_logs (파티션), outbox_events, processed_events
ghg:    emission_factors, activity_data, emission_records, calculation_runs
evidence: evidence_files, evidence_links
vw:     verification_snapshots, verifier_assignments, verification_comments, verification_signatures
rpt:    disclosure_reports, report_sections, report_narratives
메타:   disclosure_schedules
```

### 7.3 핵심 테이블 DDL (M+1 슬롯 표시 ★)

#### iam.users + user_entity_assignments (ABAC 핵심)

```sql
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

CREATE TABLE user_roles (
  user_id UUID REFERENCES users(id),
  role VARCHAR(32) NOT NULL,
  granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  granted_by UUID,
  PRIMARY KEY (user_id, role),
  CHECK (role IN ('SUPER_ADMIN','TENANT_ADMIN','ESG_MANAGER','ESG_VIEWER','VERIFIER','SUPPLIER'))
);

-- ABAC: ESG_MANAGER가 담당하는 법인 한정
CREATE TABLE user_entity_assignments (
  user_id UUID REFERENCES users(id),
  entity_id UUID REFERENCES legal_entities(id),
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (user_id, entity_id)
);

ALTER TABLE users ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON users
  USING (tenant_id = current_setting('app.current_tenant_id')::uuid);
```

#### entity.legal_entities + entity_relationships

```sql
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

CREATE TABLE entity_relationships (
  parent_id UUID REFERENCES legal_entities(id),
  child_id UUID REFERENCES legal_entities(id),
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
```

#### ghg.emission_factors (factorAt 재현성)

```sql
CREATE TABLE emission_factors (
  id BIGSERIAL PRIMARY KEY,
  standard VARCHAR(32) NOT NULL,            -- KEEI, DEFRA, IPCC_AR6
  category_code VARCHAR(64) NOT NULL,
  unit VARCHAR(16) NOT NULL,
  factor_value NUMERIC(20, 10) NOT NULL,
  gwp_version VARCHAR(16),
  effective_from DATE NOT NULL,
  effective_to DATE,
  source_document TEXT,
  loaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  loaded_file_hash CHAR(64) NOT NULL,
  UNIQUE (standard, category_code, effective_from)
);

CREATE INDEX idx_emission_factors_lookup
  ON emission_factors (standard, category_code, effective_from DESC);
```

#### ghg.activity_data (★ M+1 슬롯 포함)

```sql
CREATE TABLE activity_data (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  entity_id UUID NOT NULL REFERENCES legal_entities(id),
  reporting_year SMALLINT NOT NULL,
  reporting_month SMALLINT,
  scope SMALLINT NOT NULL,                  -- ★ M+1: 1/2/3 (MVP는 1,2만 사용)
  category_code VARCHAR(64) NOT NULL,
  activity_value NUMERIC(20, 6) NOT NULL,
  activity_unit VARCHAR(16) NOT NULL,
  normalized_value NUMERIC(20, 6) NOT NULL,
  normalized_unit VARCHAR(16) NOT NULL,
  approval_state VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  version INT NOT NULL DEFAULT 1,           -- ★ M+1: 정정 시 증가
  previous_version_id BIGINT,                -- ★ M+1: 정정 이력 체인
  reason_code VARCHAR(64),                   -- ★ M+1: 정정 사유
  supplier_id UUID,                          -- ★ M+1: Scope 3 Cat.1 공급업체
  data_quality VARCHAR(32) NOT NULL DEFAULT 'AVERAGE_DATA',  -- ★ M+1 활용
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by UUID NOT NULL REFERENCES users(id),
  CHECK (scope IN (1, 2, 3)),
  CHECK (approval_state IN ('DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'DISCLOSED')),
  CHECK (version >= 1)
);

ALTER TABLE activity_data ENABLE ROW LEVEL SECURITY;
CREATE POLICY tenant_isolation ON activity_data
  USING (tenant_id = current_setting('app.current_tenant_id')::uuid);

REVOKE UPDATE, DELETE ON activity_data FROM app_role;
```

#### ghg.emission_records + calculation_runs (재현성)

```sql
CREATE TABLE calculation_runs (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  triggered_by UUID NOT NULL REFERENCES users(id),
  triggered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  reporting_year SMALLINT NOT NULL,
  formula_version VARCHAR(32) NOT NULL,
  factor_set_version VARCHAR(32) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'RUNNING',
  CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE emission_records (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  entity_id UUID NOT NULL,
  activity_data_id BIGINT NOT NULL REFERENCES activity_data(id),
  calculation_run_id BIGINT NOT NULL REFERENCES calculation_runs(id),
  emission_factor_id BIGINT NOT NULL REFERENCES emission_factors(id),
  scope SMALLINT NOT NULL,
  method VARCHAR(32),
  co2e_kg NUMERIC(20, 6) NOT NULL,
  calculated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (scope IN (1, 2, 3))
);

REVOKE UPDATE, DELETE ON emission_records FROM app_role;
```

#### evidence.evidence_files + evidence_links

```sql
CREATE TABLE evidence_files (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  storage_key VARCHAR(512) NOT NULL,
  size_bytes BIGINT NOT NULL,
  mime_type VARCHAR(128) NOT NULL,
  sha256 CHAR(64) NOT NULL,
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  uploaded_by UUID NOT NULL REFERENCES users(id),
  UNIQUE (tenant_id, sha256)
);

CREATE TABLE evidence_links (
  evidence_id BIGINT REFERENCES evidence_files(id),
  resource_type VARCHAR(64) NOT NULL,
  resource_id VARCHAR(128) NOT NULL,
  linked_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  linked_by UUID NOT NULL REFERENCES users(id),
  PRIMARY KEY (evidence_id, resource_type, resource_id)
);
```

#### vw.verification_snapshots

```sql
CREATE TABLE verification_snapshots (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id UUID NOT NULL,
  disclosure_report_id BIGINT NOT NULL REFERENCES disclosure_reports(id),
  snapshot_hash CHAR(64) NOT NULL,
  data_payload JSONB NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by UUID NOT NULL REFERENCES users(id)
);

REVOKE UPDATE, DELETE ON verification_snapshots FROM app_role;

CREATE TABLE verifier_assignments (
  snapshot_id UUID REFERENCES verification_snapshots(id),
  verifier_user_id UUID REFERENCES users(id),
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (snapshot_id, verifier_user_id)
);
```

#### rpt.disclosure_reports + report_narratives (★ narrative 슬롯)

```sql
CREATE TABLE disclosure_reports (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  reporting_year SMALLINT NOT NULL,
  framework VARCHAR(32) NOT NULL,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  generated_at TIMESTAMPTZ,
  approved_at TIMESTAMPTZ,
  disclosed_at TIMESTAMPTZ,
  pdf_storage_key VARCHAR(512),
  CHECK (status IN ('DRAFT','REVIEW','APPROVED','DISCLOSED'))
);

CREATE TABLE report_sections (
  id BIGSERIAL PRIMARY KEY,
  report_id BIGINT REFERENCES disclosure_reports(id),
  section_code VARCHAR(64) NOT NULL,
  display_order INT NOT NULL,
  numeric_payload JSONB,
  narrative_id BIGINT,                         -- ★ M+1: narrative 자동 생성 시 채움
  UNIQUE (report_id, section_code)
);

CREATE TABLE report_narratives (
  id BIGSERIAL PRIMARY KEY,
  tenant_id UUID NOT NULL,
  body_md TEXT NOT NULL,
  version INT NOT NULL DEFAULT 1,
  source VARCHAR(32) NOT NULL DEFAULT 'MANUAL',  -- MANUAL / LLM_DRAFT / VERIFIED
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by UUID NOT NULL REFERENCES users(id),
  CHECK (source IN ('MANUAL','LLM_DRAFT','VERIFIED'))
);
```

### 7.4 M+1 활성화 시 추가 비용 (사전 슬롯 효과)

| M+1 기능 | MVP 슬롯 | 활성화 비용 |
|---|---|---|
| Scope 3 (Cat 1·2·11) | `activity_data.scope`, `supplier_id`, `data_quality` | 신규 카테고리 계산 로직만, 스키마 변경 0 |
| 정정·재공시 워크플로우 | `version`, `previous_version_id`, `reason_code` | 정정 UI + 비교표 추가, 스키마 변경 0 |
| LLM narrative 생성 | `report_narratives.source = 'LLM_DRAFT'` | LLM 어댑터 + 검증 워크플로우 추가, 스키마 변경 0 |
| 공급업체 포털 | `users.role = 'SUPPLIER'`, `supplier_id` | 공급업체 컨트롤러 + 정책 활성화, 스키마 변경 0 |
| Formula DSL | `calculation_runs.formula_version` | YAML 로더 + 평가기 추가, 스키마 변경 0 |

### 7.5 인덱스 전략

```sql
CREATE INDEX idx_activity_data_lookup
  ON activity_data (tenant_id, entity_id, reporting_year, scope);

CREATE INDEX idx_emission_records_run
  ON emission_records (calculation_run_id);

CREATE INDEX idx_audit_logs_resource
  ON audit_logs (tenant_id, resource_type, resource_id, occurred_at DESC);

CREATE INDEX idx_outbox_pending
  ON outbox_events (status, next_retry_at)
  WHERE status IN ('PENDING', 'FAILED');

CREATE INDEX idx_evidence_sha
  ON evidence_files (tenant_id, sha256);
```

### 7.6 RLS 정책 적용

| 테이블 | RLS | 비고 |
|---|---|---|
| tenants | ❌ | SUPER_ADMIN만 |
| users, legal_entities, activity_data, emission_records, evidence_files, audit_logs, outbox_events, policy_decisions, verification_snapshots, disclosure_reports | ✅ | tenant_id |
| emission_factors | ❌ | 글로벌 마스터 |
| disclosure_schedules | ❌ | 글로벌 메타 |

### 7.7 esg-t2 대비 차이

| 영역 | esg-t2 | esg-t3 |
|---|---|---|
| ABAC 속성 저장 | user_roles만 | + user_entity_assignments + verifier_assignments |
| 정책 평가 이력 | 없음 | policy_decisions (Hash Chain 연계) |
| narrative 슬롯 | 없음 | report_narratives (source 컬럼으로 LLM 슬롯) |
| 정정 슬롯 | Phase 6B에서 도입 | MVP 컬럼 슬롯 사전 포함 |
| 파티셔닝 | 없음 | audit_logs RANGE (연 단위) |
| Evidence 모듈 | ghg 안에 포함 | 독립 테이블·모듈 |
| 글로벌 마스터 분리 | 일부 | emission_factors + disclosure_schedules 명시적 RLS 제외 |

---

## 섹션 8. Phase 분할 · DoD · 문서 구조 · 계승 매트릭스

### 8.1 Phase 구조 (D6-A 거버넌스 우선)

```
Phase 0: 인프라 + 거버넌스 골격          (3주)
Phase 1: Identity & ABAC                 (3주)
Phase 2: AuditLog & Hash Chain           (2주)
Phase 3: Evidence + 배출계수 + Scope 1   (3주)
Phase 4: Scope 2 (Location/Market)       (2주)
Phase 5: Multi-Entity 연결                (2주)
Phase 6: VW + Snapshot + Basic Report    (3주)
Phase 7: Frontend (입력·대시·VW)          (4주)
Phase 8: 성능·보안·관측성 마무리·Game Day  (2주)
────────────────────────────────────────
                              총 24주
```

**esg-t2 대비 변화**:
- Phase 0이 esg-t2(1주)보다 길음 (인프라 + 거버넌스 골격)
- Phase 1에 **ABAC 추가** — iam 모듈 강화
- Scope 3·Supply·정정·재공시 Phase 제거 (M+1)
- Phase 8에 **Game Day** 의무

### 8.2 각 Phase 목표 & DoD

#### Phase 0: 인프라 + 거버넌스 골격 (3주)

**목표**: 코드 한 줄 없이도 CI/CD, DB, 모듈 뼈대, 거버넌스 도구가 동작하는 상태

**산출물**:
- Spring Boot 4 + Java 25 Gradle 프로젝트
- 8개 모듈 패키지 + `@ApplicationModule` 선언
- PostgreSQL 18 + Redis docker-compose
- Flyway V1 (tenants + disclosure_schedules)
- Testcontainers `AbstractIntegrationTest`
- ArchUnit + ModularityTest
- OpenTelemetry SDK + Collector + Tempo + Loki + Prometheus + Grafana docker-compose
- 5개 Grafana 대시보드 JSON (빈 패널)
- 알림 규칙 yml
- `docs/runbook/` 6개 파일 셸
- `scripts/runbook/lib/common.sh`
- GitHub Actions CI (5개 검증)
- Next.js 16 프로젝트 초기화

**DoD 체크리스트**:
- [ ] `*ModularityTest` 통과 (8개 모듈)
- [ ] `*ArchitectureTest` 통과 (의존 규칙)
- [ ] `/actuator/health` + `/actuator/prometheus` 정상
- [ ] OTel Collector → Tempo/Loki/Prometheus 스모크 트레이스 검증
- [ ] Grafana 5개 대시보드 로드
- [ ] `tests/runbook-link-validation.sh` 통과
- [ ] Modulith documenter가 `target/modulith-docs/` 생성
- [ ] CI 5개 검증 통과
- [ ] **[Gemini G-1]** springdoc-openapi 설정 — `generateOpenApiDocs` 빌드 태스크가 OpenAPI 3.1 산출. 각 도메인 Phase 착수 전 API 응답 DTO를 OpenAPI 어노테이션으로 사전 정의 의무

#### Phase 1: Identity & ABAC (3주)

**목표**: 이후 모든 컨트롤러가 ABAC 정책으로 보호됨

**산출물**:
- Flyway V2: tenants, users, user_roles, user_entity_assignments
- iam 도메인 + infra + api
- 정책 YAML 6개 (6역할 정책)
- JWT (Access 15분, Refresh 7일) + Redis 블랙리스트
- TenantContextInterceptor (RLS `set_config`)
- `@PreAuthorize("@policy.allow(...)")` 표준 패턴
- emergency-lockdown 정책

**DoD 체크리스트**:
- [ ] ABAC 정책 단위 테스트 ≥ 20건
- [ ] SoD 정책 (자기 승인 금지) 통과
- [ ] priority 200 정책 (공시 완료 데이터 불변) — SUPER_ADMIN도 차단
- [ ] VERIFIER 미지정 snapshot 접근 시 ABAC + RLS 둘 다 차단
- [ ] 정책 YAML 핫리로드 < 5초
- [ ] `policy_evaluation_total{effect,role}` 메트릭 노출
- [ ] [예방] `@NamedInterface` 모듈 경계 (L-P1-01)
- [ ] [예방] `set_config()` 파라미터 바인딩 (L-P1-03)
- [ ] [예방] JwtTokenProvider 캐싱 (L-P1-04)

#### Phase 2: AuditLog & Hash Chain (2주)

**목표**: 모든 데이터 변경에 자동 AuditLog + Hash Chain + OTel 통합

**산출물**:
- Flyway V3: audit_logs (파티션), outbox_events, processed_events, policy_decisions
- `@Auditable` AOP + AuditAspect
- HashChainCalculator + `canonicalPayload()`
- PESSIMISTIC_WRITE + Outbox + Retry/DLQ
- processed_events 멱등성 (FOR UPDATE SKIP LOCKED)
- AuditIntegrityScheduler (02:00 KST)
- HashChainMismatchDetected → Runbook 01 자동 트리거
- Event Catalog 자동 생성
- Runbook 01 본문 완성 + 진단·복구 스크립트

**DoD 체크리스트**:
- [ ] `@Auditable` 메서드 → AuditLog 자동 INSERT
- [ ] Hash Chain 위변조 탐지 단위 테스트
- [ ] Outbox DLQ 진입 시 알림 + Runbook 링크 발송
- [ ] trace_id/span_id 자동 저장
- [ ] PolicyDecision도 audit_logs 기록
- [ ] [예방] canonicalPayload 동일 직렬화 (L-0-08)
- [ ] [예방] synchronized + @Transactional 0건 (L-0-04)
- [ ] [예방] @Async + @Transactional 같은 메서드 0건 (L-0-11)
- [ ] [예방] Repository 마커 인터페이스 (L-0-14)
- [ ] [예방] Scheduler zone=Asia/Seoul + @ConditionalOnProperty (L-0-15)
- [ ] Game Day Q1 (Hash Chain 위변조) 시뮬레이션 통과

#### Phase 3: Evidence + 배출계수 + Scope 1 (3주)

**목표**: 활동 데이터 입력 → 증빙 첨부 → Scope 1 계산 완전 동작

**산출물**:
- Flyway V4~V6: emission_factors, activity_data, emission_records, calculation_runs, evidence_files, evidence_links
- 배출계수 YAML 로더 (item-level upsert)
- `EmissionFactorResolver.resolveAt(category, date)`
- EmissionCalculator (BigDecimal)
- UnitConverter
- evidence 모듈 + DigestInputStream
- CSV 업로드 (REQUIRES_NEW)
- data_quality_score

**DoD 체크리스트**:
- [ ] YAML 2회 로드 시 중복 없음 (L-0-02)
- [ ] 계수 갱신 후 과거 공시 재계산 동일 결과 (L-0-09)
- [ ] CSV 100행 중 5행 오류 → 95행 성공 (L-0-13)
- [ ] 동일 SHA-256 중복 업로드 거부
- [ ] [예방] double 사용 0건 (L-0-12)
- [ ] [예방] H2 예약어 컬럼 0건 (L-0-07)
- [ ] OTel Span `ghg.calculate_scope1`, `evidence.upload_file` 검증

#### Phase 4: Scope 2 (2주)

**목표**: 전력 활동에서 Location-based / Market-based 이중 계산

**DoD 체크리스트**:
- [ ] Location/Market 두 결과 산출
- [ ] 전력망 변경 → Location 변화, Market 불변
- [ ] AR5 vs AR6 GWP 차이 검증

#### Phase 5: Multi-Entity 연결 (2주)

**목표**: 3법인 계층 → 연결 GHG 자동 합산

**DoD 체크리스트**:
- [ ] 3법인 (부모 100%, 자식 60%, 손자 30%) 연결 결과
- [ ] 순환 참조 시도 거부
- [ ] 운영통제 vs 지분율 결과 차이 검증

#### Phase 6: VW + Snapshot + Basic Report (3주)

**목표**: 검증인이 시스템 내에서 공시 데이터 검증 가능

**산출물**:
- verification_snapshots + Merkle 루트
- verifier_assignments (ABAC 연계)
- VW 코멘트 + 서명
- KSSB 2 보고서 PDF (Apache PDFBox)
- report_sections + report_narratives (수동 입력)

**DoD 체크리스트**:
- [ ] Snapshot UPDATE/DELETE 시도 거부 (DB 권한)
- [ ] VERIFIER 지정 snapshot만 접근 (ABAC + RLS)
- [ ] PDF 생성 P95 ≤ 1500ms (5법인)
- [ ] KSSB 2 필수 공시 항목 100% 커버

#### Phase 7: Frontend (4주)

**목표**: ESG_MANAGER가 UJ-01 전 과정 수행 가능

**산출물** (Next.js 16, App Router, Tailwind 4, 한국어):
- 로그인 + JWT 갱신
- 활동 데이터 입력 + CSV
- Evidence 첨부
- GHG 대시보드
- 보고서 미리보기 + PDF
- VW UI (검증인용)
- 운영자 대시보드 (Grafana 임베드)

**DoD 체크리스트**:
- [ ] UJ-01 시나리오 처음~끝 통과 (Playwright)
- [ ] Lighthouse Accessibility ≥ 90
- [ ] 한국어 UI 100%
- [ ] traceId 에러 응답 노출

#### Phase 8: 성능·보안·관측성·Game Day (2주)

**목표**: 운영 투입 가능 상태 검증

**산출물**:
- OWASP ZAP 자동 스캔
- 부하 테스트 (k6 또는 Gatling)
- AuditLog 파티션 자동 생성 스케줄러 (월별)
- Game Day 4분기 시나리오 중 1회 실행
- 사용자/운영 매뉴얼
- 백업 복구 훈련

**DoD 체크리스트**:
- [ ] OWASP ZAP Critical/High 0건
- [ ] API P95 ≤ 500ms, 보고서 P95 ≤ 1500ms 부하 시
- [ ] Game Day 시나리오 1개 시연
- [ ] 백업 복구 RTO ≤ 4시간
- [ ] SLO 위반 알림 검증
- [ ] Runbook 진단 스크립트 dry-run 통과

### 8.3 esg-t3 문서 구조 (전체)

```
esg-t3/
├── CLAUDE.md                            # 운영 헌장 (~150줄)
├── README.md
├── docs/
│   ├── regulatory.md                    # ESG 규제 (esg-t2 계승)
│   ├── prd.md
│   ├── spec.md
│   ├── plan.md
│   ├── task.md
│   ├── code-review.md
│   ├── fix.md
│   ├── insight.md
│   ├── openapi.yml
│   ├── adr/
│   │   ├── ADR-001-spring-modulith.md           (계승)
│   │   ├── ADR-002-auditable-aop.md             (계승+확장)
│   │   ├── ADR-004-verification-workspace.md    (계승)
│   │   ├── ADR-005-multi-entity-consolidation.md (계승)
│   │   ├── ADR-006-postgresql-rls.md            (계승+강화)
│   │   ├── ADR-007-append-only.md               (계승+강화)
│   │   ├── ADR-008-hash-chain-disaster-recovery.md (계승+확장)
│   │   ├── ADR-010-abac-policy-engine.md        ★ NEW
│   │   ├── ADR-011-opentelemetry-stack.md       ★ NEW
│   │   ├── ADR-012-runbook-as-code.md           ★ NEW
│   │   └── ADR-013-mvp-scope-narrowing.md       ★ NEW
│   ├── runbook/                          ★ NEW
│   │   ├── 01-hash-chain-mismatch.md
│   │   ├── 02-rls-leak.md
│   │   ├── 03-recalculation-failure.md
│   │   ├── 04-backup-recovery.md
│   │   ├── 05-incident-sla.md
│   │   └── 06-deployment.md
│   ├── postmortems/                      ★ NEW
│   ├── event-catalog/                    ★ NEW
│   │   ├── README.md
│   │   ├── conventions.md
│   │   └── _generated/
│   ├── superpowers/
│   │   ├── specs/
│   │   └── plans/
│   ├── codexreviews/
│   └── geminireviews/
├── infra/
│   └── observability/
│       ├── docker-compose.observability.yml
│       ├── dashboards/
│       ├── alerts/
│       └── otel-collector-config.yaml
├── scripts/
│   └── runbook/
├── policies/                             ★ NEW (ABAC YAML)
│   ├── iam/
│   ├── ghg/
│   └── audit/
├── src/main/resources/
│   ├── emission-factors/
│   └── db/migration/
├── src/main/java/ai/claudecode/esgt3/
│   ├── iam/  entity/  audit/  ghg/  evidence/  vw/  rpt/  shared/
│   └── observability/                    # config 패키지
├── src/test/java/
│   ├── governance/                       # ArchUnit, ModularityTest
│   └── ...
├── frontend/                             # Next.js 16
├── build.gradle.kts
├── settings.gradle.kts
├── docker-compose.yml
└── .claude/
    ├── rules/                            # 18개 (00~17 인덱스)
    └── settings.local.json
```

### 8.4 `.claude/rules/` 구조 (esg-t2 13개 계승 + esg-t3 신규 5개 = 총 18개, 00~17)

| 파일 | 내용 | 출처 |
|---|---|---|
| 00-priority.md | P0~P3 우선순위 | esg-t2 계승 |
| 01-domain-architecture.md | Domain≠Entity, 검증 우선 | esg-t2 계승 |
| 02-testing.md | TDD, TestContainers | esg-t2 계승 |
| 03-security-rls.md | RLS, JWT, Webhook 서명 | esg-t2 계승 |
| 04-api-design.md | OpenAPI, GlobalExceptionHandler | esg-t2 계승 |
| 05-async-concurrency.md | @Async+@Transactional 분리 | esg-t2 계승 |
| 06-emission-calculation.md | BigDecimal, factorAt | esg-t2 계승 |
| 07-formula-dsl.md | (M+1 보존) | esg-t2 계승 |
| 08-persistence.md | Flyway, Append-only, N+1 방지 | esg-t2 계승 |
| 09-scheduler.md | zone=Asia/Seoul | esg-t2 계승 |
| 10-evidence-files.md | DigestInputStream, SHA-256 | esg-t2 계승 |
| 11-modulith-events.md | 모듈 경계, @Auditable | esg-t2 계승 |
| 12-change-principles.md | 변경 전 확인 | esg-t2 계승 |
| **13-abac-policy.md** | ABAC 정책 작성 규칙, priority, 멱등성 | ★ NEW |
| **14-observability.md** | Span 네이밍, MDC 필드, 메트릭 컨벤션 | ★ NEW |
| **15-runbook-as-code.md** | Runbook 표준 구조, 스크립트 규칙 | ★ NEW |
| **16-event-catalog.md** | 이벤트 네이밍, idempotencyKey 의무 | ★ NEW |
| **17-language-policy.md** | 한국어 기본 (UI·주석·문서) | ★ NEW |

### 8.5 esg-t2 → esg-t3 교훈 계승 매트릭스

| 교훈 | 처리 |
|---|---|
| L-0-01 ~ L-0-16 (esg-t1 계승) | **전부 esg-t3에 계승**. `docs/insight.md`에 동일 항목 + esg-t3 적용 위치 명시 |
| L-P0-01~06 (Phase 0 학습) | esg-t3 Phase 0 DoD에 예방 체크리스트로 포함 |
| L-P1-01~05 (Phase 1 학습) | esg-t3 Phase 1 DoD에 포함 + ABAC 관련 신규 항목 추가 |
| L-P2-01~ (Phase 2 학습) | esg-t3 Phase 2 DoD에 포함 |

### 8.6 esg-t2 ADR 계승 매트릭스

| ADR | 계승 여부 | 비고 |
|---|---|---|
| ADR-001 Spring Modulith | ✅ 계승 | 스택 동일 |
| ADR-002 @Auditable AOP | ✅ 계승 + 확장 | PolicyDecision도 감사 |
| ADR-003 Scope 3 엔진 | ❌ 보류 | M+1로 미뤄짐 |
| ADR-004 Verification Workspace | ✅ 계승 | ABAC로 격리 강화 |
| ADR-005 Multi-Entity 연결 | ✅ 계승 | |
| ADR-006 PostgreSQL RLS | ✅ 계승 + 강화 | ABAC와 이중 방어 명시 |
| ADR-007 Append-only | ✅ 계승 + 강화 | DB REVOKE 권한 박탈 |
| ADR-008 Hash Chain DR | ✅ 계승 + 확장 | Runbook 01 + 자동 격리 |
| ADR-009 Legacy 마이그레이션 | ❌ 보류 | 신규 시스템 |
| **ADR-010 ABAC 정책 엔진** | ★ NEW | |
| **ADR-011 OpenTelemetry 스택** | ★ NEW | |
| **ADR-012 Runbook-as-Code** | ★ NEW | |
| **ADR-013 MVP 범위 축소 근거** | ★ NEW | Scope 3 등 양보 근거 |

### 8.7 다음 단계 (브레인스토밍 종료 후)

1. 본 design.md 사용자 리뷰 & 승인
2. Spec 자체 리뷰 (placeholder, 모순, 모호함, 스코프 점검)
3. 사용자 최종 승인
4. `writing-plans` 스킬 호출 → Phase 0부터 태스크 단위로 plan.md 작성
5. plan.md → task.md 변환 (esg-t2 체크리스트 패턴 적용)
6. CLAUDE.md 갱신 (esg-t3 운영 헌장)
7. prd.md, spec.md, regulatory.md, insight.md 셸 생성
8. Phase 0 착수

---

# Part 3. 모든 섹션 완료

| 섹션 | 주제 | 상태 |
|---|---|---|
| 1 | 비전 · 차별점 · MVP 경계 | ✅ 완료 |
| 2 | 아키텍처 개요 · Modulith 모듈 구조 | ✅ 완료 |
| 3 | ABAC 권한 모델 | ✅ 완료 |
| 4 | AuditLog & Hash Chain | ✅ 완료 |
| 5 | Observability 설계 | ✅ 완료 |
| 6 | Runbook 6종 구조 | ✅ 완료 |
| 7 | 데이터 모델 핵심 ERD | ✅ 완료 |
| 8 | Phase 분할 · DoD · 문서 구조 · 계승 매트릭스 | ✅ 완료 |

**문서 상태**: `READY_FOR_USER_REVIEW` (자체 리뷰 완료)

---

## 부록 C. 자체 리뷰 결과 (2026-05-26)

브레인스토밍 스킬 절차에 따른 4개 차원 자체 리뷰 결과.

### C.1 Placeholder 스캔

- 헤더 "0.1 (작성 중)" → 1.0 (READY_FOR_USER_REVIEW)로 갱신
- 본문 내 TBD·TODO·작성 중 문구 0건 확인

### C.2 내부 일관성

검토 항목 및 결과:

| 항목 | 결과 |
|---|---|
| 8개 모듈 (섹션 2 vs 섹션 7 vs 섹션 8 docs 구조) | ✅ 일치 |
| VW MVP 포함 (섹션 1 vs 섹션 2 모듈 vs 섹션 7 테이블) | ✅ 일치 |
| narrative 슬롯 처리 (섹션 1 vs 섹션 7 report_narratives) | ✅ 일치 |
| 알림 규칙 경로 `infra/observability/alerts/` (섹션 5, 6, 8) | ✅ 일치 |
| ABAC 6역할 (섹션 3 vs 섹션 7 users CHECK 제약) | ✅ 일치 |
| Hash Chain 자동 격리 → `emergency-lockdown` 정책 (섹션 3, 4, 6) | ✅ 일치 |

### C.3 스코프 체크

- 24주 일정, 9 Phase로 분할 → 단일 구현 계획에 다소 크지만, **Phase 0를 첫 plan으로 작성하고 이후 Phase는 별도 plan으로 분할**하는 것이 적절. writing-plans 단계에서 결정.
- 모듈 8개, 테이블 20여 개, 정책 6개, Runbook 6개, 대시보드 5개 → 분량은 크지만 명확히 경계지어짐.

### C.4 모호함 체크 및 수정

| 발견 | 수정 |
|---|---|
| OpenAPI DTO 사전 정의 의무가 D6 부수에만 있고 Phase DoD에 누락 | Phase 0 DoD에 Gemini G-1 항목 명시 |
| `emergency-lockdown` 정책 활성화/해제 절차 미명시 | 섹션 3.5 YAML 예시에 정책 추가 + 활성화/해제 절차 별도 설명 |
| Game Day "분기 1회"가 개발 중 분기인지 운영 후 분기인지 모호 | 섹션 6.6에 Phase 8 1회 시연 + 운영 시작 후 분기 순환 명시 |
| (검토됐으나 수정 불필요) AuditLog 파티션 — MVP 연 단위, M+1 월 단위 자동 생성 | 섹션 4.2와 8.2 Phase 8 DoD에 명시되어 있어 일관됨 |
| (검토됐으나 수정 불필요) SUPPLIER 역할 — MVP 비활성이지만 user_roles CHECK 포함 | 섹션 1 MVP Out + 섹션 3.6 + 섹션 7 CHECK 제약 일관됨 |

### C.5 자체 리뷰 후 문서 상태

- 본 문서는 단일 구현 계획에 직접 들어가기보다, **Phase 0~8 각각의 plan.md를 파생시키는 마스터 설계**로 위치한다.
- 사용자 리뷰 통과 후 `writing-plans` 스킬로 Phase 0 plan.md를 먼저 작성하고, 그 결과로 Phase 0 착수.
| 5 | Observability 설계 (OTel, 메트릭, 트레이싱, 알림) | 대기 |
| 6 | Runbook 6종 구조 (Hash Chain / RLS / 재계산 / 백업 / 인시던트 / 배포) | 대기 |
| 7 | 데이터 모델 핵심 ERD (M+1 확장 슬롯 포함) | 대기 |
| 8 | Phase 분할 (Phase 0~8) + 각 Phase의 DoD | 대기 |
| 9 | 문서 구조 (esg-t3의 docs/ 디렉터리 전체 설계) | 대기 |
| 10 | esg-t2 → esg-t3 계승 매트릭스 (16개 교훈 + 9개 ADR 처리 방침) | 대기 |

각 섹션이 완료되면 본 문서에 누적된다. 모든 섹션이 완료되고 사용자 승인이 끝나면:

1. 본 문서를 **최종 설계 스펙**으로 확정 (버전 1.0).
2. `superpowers:writing-plans` 스킬로 **구현 계획**(plan.md) 생성.
3. CLAUDE.md를 esg-t3 설계에 맞게 업데이트.

---

## 부록 A. 참고 자료

- `esg-t2/docs/prd.md` — 제품 요구사항 (계승)
- `esg-t2/docs/spec.md` — 기술 명세 (참고)
- `esg-t2/docs/insight.md` — 16개 교훈 (전면 계승)
- `esg-t2/docs/adr/` — ADR 9건 (esg-t3에서 재평가 후 일부 계승)
- `esg-t2/docs/codexreviews/` — ChatGPT 리뷰 + 적용 분석
- `esg-t2/docs/geminireviews/` — Gemini 리뷰 + 적용 분석
- `esg-t2/.claude/rules/` — 12개 규칙 파일 (esg-t3에서 ABAC/Observability 룰 추가 예정)

## 부록 B. 결정 변경 시 절차

본 문서의 의사결정 항목(D1~D6)은 코드 작성 시작 전까지 자유롭게 변경 가능하다. 코드 작성 시작 후 변경 시:

1. 변경 사유를 본 문서에 `D{N} 개정` 섹션으로 추가 (기존 결정 보존).
2. 영향받는 설계 섹션을 함께 갱신.
3. 영향받는 Phase 태스크를 plan.md / task.md에 반영.

이는 esg-t2의 ADR 절차 (`docs/adr/`)와 동일한 원칙: **결정을 지우지 말고 누적한다.**
