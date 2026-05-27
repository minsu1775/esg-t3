# esg-t3 — ESG 공시지원 시스템 (3세대)

> 운영·거버넌스가 가장 견고한 ESG 공시 데이터 플랫폼

## 빠른 시작

```bash
# 1. 인프라 기동
docker compose up -d
docker compose -f docker-compose.observability.yml up -d

# 2. 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 3. 검증
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/actuator/prometheus | head

# 4. 관측성 대시보드
# http://localhost:3000   - Grafana (admin/admin)
# http://localhost:9090   - Prometheus
# http://localhost:3200   - Tempo
# http://localhost:3100   - Loki
```

## 개발 명령

```bash
./gradlew test                                              # 전체 테스트
./gradlew test --tests "*ModularityTest"                    # 모듈 경계 검증
./gradlew test --tests "*ArchitectureTest"                  # ArchUnit
./tests/runbook-link-validation.sh                          # Runbook 링크 검증

cd frontend && npm run dev                                  # 프론트엔드 dev
cd frontend && npm run build                                # 프론트엔드 빌드
```

## 아키텍처

8개 Spring Modulith 모듈: `iam`, `entity`, `audit`, `ghg`, `evidence`, `vw`, `rpt`, `shared`
+ cross-cutting: `observability`

상세: `docs/superpowers/specs/2026-05-26-esg-t3-design.md`

## 문서

- 설계 스펙: `docs/superpowers/specs/`
- 구현 계획: `docs/superpowers/plans/`
- 실행 로그: `docs/superpowers/plans/2026-05-26-esg-t3-phase0-execution-log.md`
- 규제 레퍼런스: `docs/regulatory.md`
- Runbook: `docs/runbook/`
- ADR: `docs/adr/` (Phase별 누적 예정)

## 언어 정책

모든 코드 주석·로그·UI·문서·커밋 메시지는 한국어 기본.

## CI/CD

GitHub Actions: `.github/workflows/ci.yml` (backend-test + modularity + archunit + runbook + frontend-build)
저장소: https://github.com/minsu1775/esg-t3
