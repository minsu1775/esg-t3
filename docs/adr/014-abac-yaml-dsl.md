# ADR-014: ABAC 정책 YAML 자체 DSL 채택

## 상태
Accepted (2026-05-28, Phase 1)

## 배경
esg-t2 RBAC의 한계 — ESG_MANAGER 권한 폭증, 자기 승인(SoD) 방지 불가, 공시 완료 데이터 불변성 강제 불가, VERIFIER 격리의 분산 구현. 다법인·다회계연도·다민감도 환경에서 RBAC만으로 권한 관리가 불가능.

## 결정
OPA(Rego) 대신 **자체 YAML 정책 DSL** 채택. `resources/policies/iam/*.yaml`에 정책을 데이터로 외부화하고, `PolicyEvaluator`(순수 도메인 서비스)가 priority 내림차순으로 평가한다.

## 사유
1. esg-t2 Formula DSL 패턴 재사용 — 정책=데이터=설정 외부화의 일관성
2. 외부 데몬(OPA) 없이 인메모리 평가 → p95 < 1ms 목표, 운영 단순
3. 도메인 친화 — 한국어 description, ESG 용어 표현 가능
4. 운영 인력의 Rego 학습 곡선 회피

## 구현 핵심
- `PolicyContext = (Subject, Resource, Action, Environment)` — 순수 record
- priority 표준: 250(emergency-lockdown) > 200(disclosed-data-immutability) > 100(SoD) > 0(일반)
- DENY-default: 매칭 정책 없으면 NOT_APPLICABLE → 호출 측에서 거부
- 변수 치환(`${resource.tenantId}`) + contains/in 매처
- 모든 PERMIT/DENY는 `policy_decisions` 테이블 INSERT (Phase 2에서 Hash Chain 연계)
- 정책 YAML의 `tests:` 섹션을 `@TestFactory`로 동적 실행 → 정책 자체가 테스트를 동반

## 모듈 경계 (L-P1-01/08)
- `PolicyEvaluator`는 `iam.domain`에만 거주 (ArchUnit 강제)
- `iam.domain`은 Spring·JPA 의존 0
- 외부 모듈은 `iam.api`(@NamedInterface) PolicyFacade만 사용

## 트레이드오프
- 정책 엔진 자체 구현 부담 → PolicyEvaluator + WhenClauseMatcher로 최소 기능만 한정
- 매처는 ABAC에 필요한 평등/in/contains/변수치환만 지원

## 대안
- OPA: 강력하지만 외부 데몬, Rego 학습, 운영 복잡
- SpEL만(esg-t2): 정책 분산, 추적 어려움 — esg-t2의 한계 그대로 답습
