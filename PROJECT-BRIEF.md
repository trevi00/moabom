# 모아봄 (moabom) — 프로젝트 브리프 (스펙 승인 게이트용)

> 표기: **[확정]** = 사용자 언급/기존 규율에서 도출, **[제안]** = 승인 필요, **[열림]** = 결정 대기.
> 승인 후 `/kha-new-project` 파이프라인(요구사항 → PRD → 설계 → 구현 22 스테이지)이 이 문서를 입력으로 삼는다.

## 1. 무엇을 만드나

**[확정]** 넷플릭스 클론 — 스트리밍 서비스 본체. 넷플릭스 **테크 블로그의 공개 아키텍처 패턴**을
참조 설계로 삼아, 그들이 실제로 푼 문제(스트리밍·개인화·복원력·실험)를 축소 스케일로 재구현한다.
목적: 대규모 서비스 아키텍처의 실전 구현 포트폴리오 (Netflix 채용 트랙 지식베이스와 연계).

**[확정]** 서비스명 **모아봄 (moabom)** — 저장소 `D:/moabom` (2026-08-05 사용자 확정.
대용량 미디어 산출물 때문에 D 드라이브 배치). 상표 회피: UI 에 Netflix 로고/워드마크
사용 불가, 레이아웃 문법만 복제 — 워드마크는 "모아봄".

## 2. MVP 슬라이스 [제안]

| # | 기능 | Netflix 원형 |
|---|---|---|
| 1 | 계정·프로필 (가구 1계정 N프로필, 키즈) | Profiles |
| 2 | 카탈로그 브라우즈 — **행(row) 기반 홈**, 장르행·Top10행 | Lolomo (List of List of Movies) |
| 3 | 타이틀 상세 + 재생 — **HLS 어댑티브**, 시청 위치 저장/이어보기 | Adaptive Streaming |
| 4 | 검색 (제목·장르, 한국어 형태소는 [열림]) | Search |
| 5 | 마이리스트 | My List |
| 6 | 추천 v1 — 시청 이력·장르 기반 행 생성 (협업 필터링은 후속) | Personalized Rows |
| 7 | 콘텐츠 인제스트 (관리자) — 업로드 → 트랜스코딩(ffmpeg) → 퍼블리시 | Content Pipeline |

**[열림]** 후속 후보: 구독/결제 모의, 동시 시청 제한, 다운로드, 프로필별 성인 등급 제한, A/B 실험 UI.
**[확정]** 콘텐츠는 저작권-프리 영상만 (Blender 오픈무비: Sintel/Big Buck Bunny 등).

## 3. 참조 아키텍처 매핑 [제안] — 테크 블로그 패턴 → 우리 축소 이식

| Netflix TechBlog 패턴 | 축소 이식 |
|---|---|
| 마이크로서비스 (수백 개) | **Spring Modulith 모듈리스** — catalog / playback / profile / recommendation / ingest / experiment. 모듈 경계는 `@NamedInterface` + 의존 그래프 검증 (dual-order 선례) — 물리 분리는 트래픽이 요구할 때 (스파이럴 트립와이어 사상) |
| BFF / GraphQL Federation | 단일 **BFF 레이어** (행 조립 = 서버가 완성된 lolomo JSON 반환 — 클라이언트 조립 금지) |
| Hystrix → 복원력 | **Resilience4j** — 추천 실패 시 비개인화 행 폴백, 서킷브레이커 실측 데모 |
| Open Connect CDN | 로컬 정적 HLS 세그먼트 서빙 + nginx 캐시 (개념 증명 수준) |
| 트랜스코딩 파이프라인 | ffmpeg 멀티 렌디션(1080/720/480) + HLS 패키징, 비동기 잡 큐 (Spring Batch 후보) |
| A/B 실험 (Ablaze) | 실험 플래그 + 배정 로그 + 지표 수집 최소 구현 |
| Chaos Engineering | 파이프라인의 신뢰성 테스트 스테이지(컨테이너 kill)로 축소 재현 |
| 관측 (Atlas) | Actuator + Prometheus + Grafana (파이프라인 기존 스테이지) |

**리서치 규율 [확정]**: 각 패턴은 구현 전 해당 테크 블로그 원문을 WebFetch 로 확보해 설계 문서에
인용한다 (인용-강제 브리프 선례). 차용-조사 과낙관 경계 — 원문이 말한 조건과 우리 스케일의 차이를
설계 문서에 명시.

## 4. 스택 [제안]

- BE: Java 17 + Spring Boot 3.2 + **Spring Modulith** + JPA + MySQL 8.0 (tech-stack.yaml 선언 완료)
- FE: Next.js + React (넷플릭스 UI 문법 — 행 스크롤·호버 확장·상세 모달), hls.js 재생
- 인프라: docker-compose (mysql·nginx·prometheus·grafana), CI = GitHub Actions (파이프라인 스테이지)
- 테스트: JUnit + JaCoCo + FixtureMonkey + Playwright (파이프라인 22 스테이지 그대로)

## 5. 진행 방식 [확정]

하네스 0→1 파이프라인 전체를 밟는다: 요구사항 명세(US/AC — **spec-notation v1 채택**: 이 프로젝트가
표기 규약의 첫 실 채택 프로젝트가 된다) → PRD → 다이어그램 → DB 설계 → API 명세 → **목업-우선
리뷰**(행 UI 목업 → 사람 sign-off) → 뼈대 → 구현 → 검증. 구축 후 고도화는 SPIRAL 턴.

## 6. 승인 기록 (2026-08-05, 사용자)

1. **[확정]** M1 = **MVP 7종 전체** (프로필·브라우즈·재생·검색·마이리스트·추천v1·인제스트).
2. **[확정]** FE = Next.js + React (+ hls.js).
3. **[확정]** 서비스명 = **모아봄(moabom)** (2026-08-05).
4. 진행: 이 브리프를 입력으로 `/kha-new-project` 파이프라인 기동 (요구사항 명세부터).
