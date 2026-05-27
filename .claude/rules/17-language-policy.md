# 17. 한국어 정책

> **로드 조건**: 항상
> **적용**: 모든 산출물

## 한국어 기본 원칙

다음 모든 산출물은 한국어로 작성:

1. **코드 주석** (Javadoc, 인라인 주석)
2. **로그 메시지** (log.info, log.warn 등)
3. **에러 메시지** (ErrorResponse.message, 예외 메시지)
4. **UI 텍스트** (Next.js page, 알림, 폼 라벨)
5. **커밋 메시지** (Conventional Commits 형식 유지, body는 한국어)
6. **문서** (README, design.md, plan.md, runbook 등 — 코드 블록 영문 제외)
7. **테스트 이름**: 한국어 메서드명 사용 가능 (예: `스프링_컨텍스트가_정상적으로_로드된다()`)

## 영문 유지 영역

다음은 영문 그대로:
- **타입·클래스·메서드·변수명**: Java 식별자 (한국어 시도 가능하나 호환성 위험)
- **패키지명**: `ai.claudecode.esgt3.iam` 등
- **DB 컬럼명**: snake_case 영문 (Hibernate/JPA 표준)
- **메트릭·로그 키**: `esg_t3_policy_evaluation_total` 등
- **OTel span 이름**: `iam.evaluate_policy` 등

## Next.js 설정

- `<html lang="ko">` (App Router `layout.tsx`)
- `next/font`는 한국어 폰트 최적화 (Noto Sans KR 또는 Pretendard 권장 — Phase 7)

## Frontend i18n

MVP는 단일 언어(한국어). M+1에서 다국어가 필요해지면 `next-intl` 또는 `react-i18next` 도입 (현재는 도입 비용 회피).
