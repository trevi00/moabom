# 모아봄 (moabom)

## What This Is

넷플릭스 클론 — 스트리밍 서비스 본체. Netflix **테크 블로그의 공개 아키텍처 패턴**을 참조 설계로 삼아,
그들이 실제로 푼 문제(스트리밍·개인화·복원력·실험)를 축소 스케일로 재구현한다.
목적: 대규모 서비스 아키텍처의 실전 구현 포트폴리오 (Netflix 채용 트랙 지식베이스와 연계).

## Core Value

**행(row) 기반 홈에서 콘텐츠를 골라 HLS 어댑티브 스트리밍으로 끊김 없이 재생하고, 보던 위치에서 이어본다** —
브라우즈→재생→이어보기 루프가 전부 실패해도 이것만은 작동해야 한다.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] 계정·프로필 — 가구 1계정 N프로필, 키즈 프로필 (Netflix Profiles 원형)
- [ ] 카탈로그 브라우즈 — 행(row) 기반 홈, 장르행·Top10행 (Lolomo 원형)
- [ ] 타이틀 상세 + 재생 — HLS 어댑티브, 시청 위치 저장/이어보기 (Adaptive Streaming 원형)
- [ ] 검색 — 제목·장르 (Search 원형; 한국어 형태소는 [열림])
- [ ] 마이리스트 (My List 원형)
- [ ] 추천 v1 — 시청 이력·장르 기반 행 생성 (Personalized Rows 원형; 협업 필터링은 후속)
- [ ] 콘텐츠 인제스트 (관리자) — 업로드 → 트랜스코딩(ffmpeg) → 퍼블리시 (Content Pipeline 원형)

### Out of Scope

- 구독/결제 모의 — 후속 후보 (M1은 MVP 7종에 집중)
- 동시 시청 제한 — 후속 후보
- 다운로드 (오프라인 시청) — 후속 후보
- 프로필별 성인 등급 제한 — 후속 후보 (키즈 프로필 플래그는 M1 포함, 등급 엔진은 후속)
- A/B 실험 UI — 후속 후보 (실험 플래그·배정 로그 최소 구현은 참조 아키텍처에 포함)
- 협업 필터링 추천 — 추천 v1은 시청 이력·장르 기반, CF는 후속
- Netflix 로고/워드마크 사용 — 상표 회피. 레이아웃 문법만 복제, 워드마크는 "모아봄"
- 저작권 있는 콘텐츠 — 저작권-프리 영상만 (Blender 오픈무비: Sintel/Big Buck Bunny 등)
- 물리적 마이크로서비스 분리 — Spring Modulith 모듈리스로 시작, 물리 분리는 트래픽이 요구할 때 (스파이럴 트립와이어 사상)

## Context

**참조 아키텍처 매핑 (Netflix TechBlog 패턴 → 축소 이식):**

| Netflix TechBlog 패턴 | 축소 이식 |
|---|---|
| 마이크로서비스 (수백 개) | **Spring Modulith 모듈리스** — catalog / playback / profile / recommendation / ingest / experiment. 모듈 경계는 `@NamedInterface` + 의존 그래프 검증 (dual-order 선례) |
| BFF / GraphQL Federation | 단일 **BFF 레이어** (행 조립 = 서버가 완성된 lolomo JSON 반환 — 클라이언트 조립 금지) |
| Hystrix → 복원력 | **Resilience4j** — 추천 실패 시 비개인화 행 폴백, 서킷브레이커 실측 데모 |
| Open Connect CDN | 로컬 정적 HLS 세그먼트 서빙 + nginx 캐시 (개념 증명 수준) |
| 트랜스코딩 파이프라인 | ffmpeg 멀티 렌디션(1080/720/480) + HLS 패키징, 비동기 잡 큐 (Spring Batch 후보) |
| A/B 실험 (Ablaze) | 실험 플래그 + 배정 로그 + 지표 수집 최소 구현 |
| Chaos Engineering | 파이프라인의 신뢰성 테스트 스테이지(컨테이너 kill)로 축소 재현 |
| 관측 (Atlas) | Actuator + Prometheus + Grafana (파이프라인 기존 스테이지) |

**리서치 규율 [확정]**: 각 패턴은 구현 전 해당 테크 블로그 원문을 WebFetch로 확보해 설계 문서에 인용한다
(인용-강제 브리프 선례). 차용-조사 과낙관 경계 — 원문이 말한 조건과 우리 스케일의 차이를 설계 문서에 명시.

**진행 방식 [확정]**: 하네스 0→1 파이프라인 전체를 밟는다: 요구사항 명세(US/AC — **spec-notation v1 채택**:
이 프로젝트가 표기 규약의 첫 실 채택 프로젝트) → PRD → 다이어그램 → DB 설계 → API 명세 →
**목업-우선 리뷰**(행 UI 목업 → 사람 sign-off) → 뼈대 → 구현 → 검증. 구축 후 고도화는 SPIRAL 턴.

**연계 자산**: Netflix 채용 트랙 지식베이스 (기존 프로젝트) — 이 포트폴리오가 그 트랙의 실전 구현 축.
`.claude/tech-stack.yaml` 선언 완료 (java/springboot 3.2 + mysql 8.0 + jpa).

## Constraints

- **Tech stack (BE)**: Java 17 + Spring Boot 3.2 + Spring Modulith + JPA + MySQL 8.0 — 포트폴리오 표준 스택 (회사 5.7/mybatis 제약 없음; 8.0 윈도 함수·CTE 활용)
- **Tech stack (FE)**: Next.js + React + hls.js — 넷플릭스 UI 문법 (행 스크롤·호버 확장·상세 모달)
- **인프라**: docker-compose (mysql·nginx·prometheus·grafana), CI = GitHub Actions (파이프라인 스테이지)
- **테스트**: JUnit + JaCoCo + FixtureMonkey + Playwright (파이프라인 22 스테이지 그대로)
- **저장소**: `D:/moabom` — 대용량 미디어 산출물 때문에 D 드라이브 배치 (2026-08-05 사용자 확정)
- **법적**: 상표 회피 (Netflix 로고/워드마크 금지) + 저작권-프리 콘텐츠만
- **콘텐츠**: Blender 오픈무비 (Sintel, Big Buck Bunny 등) — 트랜스코딩 파이프라인 실측용 원본

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| M1 = MVP 7종 전체 (프로필·브라우즈·재생·검색·마이리스트·추천v1·인제스트) | 2026-08-05 사용자 승인 — 축소 스케일이라도 루프 전체를 관통해야 포트폴리오 가치 | — Pending |
| Spring Modulith 모듈리스 (물리 MSA 아님) | 모듈 경계 검증은 얻되 운영 복잡도 회피; 물리 분리는 트래픽이 요구할 때 (트립와이어) | ✅ P1 실증 — 8모듈 verify() green + 경계 위반 반사실 차단 (2026-08-06). 후반 증설 phase(9~)는 /kha-add-phase 대기 |
| BFF가 완성된 lolomo JSON 반환 (클라이언트 조립 금지) | Netflix BFF 패턴 축소 이식; FE-BE 계약 단순화 | — Pending |
| FE = Next.js + React + hls.js | 2026-08-05 사용자 승인 | — Pending |
| 서비스명 = 모아봄(moabom) | 2026-08-05 사용자 확정; 상표 회피 워드마크 | — Pending |
| MySQL 8.0 + JPA (회사 스택과 분리) | 신규 포트폴리오는 최신 표준 스택; 윈도 함수·CTE 활용 | ✅ P1 착지 — Flyway 단일 진실 + ngram/collation 파라미터 실효를 Testcontainers로 실측 (2026-08-06) |
| spec-notation v1 첫 실 채택 (US/AC 표기) | debate-1785889297 수렴 규약의 첫 실전 적용 프로젝트 | — Pending |
| 테크블로그 원문 인용-강제 리서치 규율 | 차용-조사 과낙관 2-Strike 경계; 원문 조건 vs 우리 스케일 차이 명시 | — Pending |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/kha-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-08-06 after Phase 1 completion — github.com/trevi00/moabom public, CI green, verification human_needed(UAT 5 이월)*
