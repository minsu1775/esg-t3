# ADR-015: JWT 자체 서명 (HMAC-SHA256) — M+1 Keycloak 전환 전략

## 상태
Accepted (2026-05-28, Phase 1)

## 결정
MVP는 **HMAC-SHA256 자체 서명** JWT 사용. Access Token 15분, Refresh Token 7일 + Redis 블랙리스트. M+1에서 Keycloak/OIDC로 전환.

## 전환 비용 최소화 패턴
- `JwtTokenProvider`를 빈으로 등록 (SecretKey single instance 캐싱, L-P1-04)
- M+1 전환: `NimbusJwtDecoder.withJwkSetUri(url)` 빈으로 교체 — 컨트롤러·필터 변경 없음
- 컨트롤러·필터는 Spring Security 표준 Authentication 인터페이스만 의존 (`JwtAuthentication`)

## 토큰 흐름
- 로그인: BCrypt 검증 → Access + Refresh 발급
- 리프레시: `parseRefreshToken`(typ=refresh 검증) → 기존 JTI 블랙리스트 → 신규 발급 (rotating refresh)
- 로그아웃: Refresh JTI를 Redis 블랙리스트 등록 (TTL 자동 만료), idempotent

## 보안 결정
- `ESG_JWT_SECRET` 미설정 또는 32B 미만 → 부팅 차단 (L-P1-05, fail-fast)
- Access Token 블랙리스트는 SHA-256 hex로 키 축약 (원문 노출 방지)
- typ=access|refresh 명시 검증 → 토큰 혼용 차단

## 사유
1. MVP는 단일 IdP 불필요 (esg-t3 자체가 IdP 역할)
2. Keycloak 운영(고가용·백업·인증서)은 Phase 8 이후
3. JJWT 0.12.6 — 표준 라이브러리, Spring Security와 독립

## 트레이드오프
- 대칭키(HMAC) → 토큰 검증에 secret 공유 필요 (단일 서비스라 무방, M+1 비대칭키 전환)
- 분산 환경에서 LockdownState의 System property 동기화는 단일 노드 한정 → M+1 Redis pub/sub
