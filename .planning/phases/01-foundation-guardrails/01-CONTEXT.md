# Phase 1: 기반 + 가드레일 - Context

**Gathered:** 2026-08-06
**Status:** Ready for planning

<domain>
## Phase Boundary

전 스택이 한 커맨드로 기동하고, 모듈 경계·스키마 단일 진실·미디어 격리가 첫 커밋부터 자동으로 강제된다 (ARCH-01, ARCH-05 + ROADMAP 명시 가드레일 산출물 4종: ADR-0001, resilience4j 2.2.0 핀+회귀 게이트, mysql 기동 파라미터, Windows 파일명 ASCII-safe 정책).

기능(재생·카탈로그·검색 등)은 이 phase 소관이 아니다 — 골격과 규율만.

</domain>

<decisions>
## Implementation Decisions

### 스코프 게이트 (로드맵 수준 결정 — 이 세션에서 확정)
- **D-00:** 대용량 트래픽 요소(물리 MSA·DB이중화·Redis)는 **M1 후반 증설 — "진화 서사"** 로 반영한다. Phase 1~8은 그대로 유지(Modulith 모듈리스로 완성)하고, 뒤에 Phase 9~ 를 증설: 부하 주입 → 병목 실측 → Redis 캐시/세션 → DB 읽기 복제본 → 모듈 1개 물리 분리. 서킷브레이커는 기존 Phase 6 유지. **Phase 1은 이 증설을 선반영하지 않는다** (compose에 Redis/replica 없음) — Modulith 경계 검증 자체가 분리-대비 준비물이다. 로드맵 반영은 본 워크플로 종료 후 `/kha-add-phase`.

### 기동 토폴로지
- **D-01:** **프로필 이원화.** 평시 개발 = 인프라만 compose(`spring-boot-docker-compose`가 bootRun 시 자동 기동) + 앱은 bootRun. 데모/검수 = `docker compose --profile full up` 으로 앱 컨테이너(`bootBuildImage` 산출 이미지)까지 한 방 기동. **성공 기준 1("한 커맨드로 앱까지")은 full 프로필로 충족한다** — plan-phase는 SC1 검증을 full 프로필 기준으로 작성할 것.
- **D-02:** **단일 진입 오리진 규약을 P1에서 못박는다.** 브라우저는 `:3000`(Next)만 본다 — `/api/*`→앱`:8080`, `/media/*`→nginx rewrites. STACK.md §2.6의 "hls.js만 CORS로 깨지는 비대칭 버그" 실패 조건 원천 차단. 문서만이 아니라 **실동작으로 검증** (D-05의 FE 스텁이 `/api` 경유 헬스 응답을 실제로 받아야 함).
- **D-03:** **기동·리셋 UX는 docker compose 표준 커맨드 그대로.** 래퍼 스크립트/Make/Gradle 태스크 없음. README에 `up` / `down` / `down -v`(스키마 리셋) 3줄.

### 레포 구조·FE 시점
- **D-04:** **backend/ + frontend/ 모노레포.** 루트는 compose·docs(ADR)·미디어 규약·README 소유. backend/ = Gradle 프로젝트(com.moabom), frontend/ = Next.js. 후반 증설 phase의 물리 분리 시 services/ 확장을 염두에 둔 배치.
- **D-05:** **Next.js 골격을 P1에 얇게 포함.** Next 16 스캐폴딩(App Router, STACK.md 핀 준수) + rewrites 프록시 설정 + 헬스 확인 스텁 1페이지. 그 이상의 UI는 만들지 않는다 (행 UI는 P5 목업-우선 규율 대상).
- **D-06:** **full 프로필에 FE 컨테이너 포함.** 데모 모드에선 Next도 컨테이너로 떠서 "한 커맨드로 전체 서비스" 서사 완결. 평시 개발은 `npm run dev`.

### Git·CI 운영
- **D-07:** **GitHub public repo 생성 (계정 trevi00).** 포트폴리오 목적 — 미디어 격리·대용량 차단 가드레일이 첫 커밋부터 강제되므로 처음부터 공개.
- **D-08:** **main 직접 push (solo 모드).** 현 로컬 `master`→`main` 전환. Conventional Commits. PR-only 아님 — GSD의 잦은 planning/실행 커밋과 마찰 없는 흐름. CI는 push 트리거.
- **D-09:** **P1 CI는 최소 코어 게이트:** build + `ApplicationModules.verify()` + 단위 테스트 + 미디어/대용량 파일 커밋 차단 체크 + resilience4j `dependencyInsight` 회귀 게이트(2.3.0 유입 차단). 러너는 ubuntu-latest (Testcontainers 요구 — STACK.md: Windows 러너 불가). 22-스테이지 전면 채택은 하지 않고 이후 phase마다 증설 (E2E는 P2부터).
- **D-10:** **로컬 pre-commit 훅 추가 — 미디어 차단만.** 미디어 확장자/파일 크기 차단을 로컬 훅으로 (빠름, push 전 차단 — 일단 push되면 이력 세탁 비용 큼). `verify()`는 CI에만 (로컬 훅에 빌드 태우지 않음).

### 스키마·시드
- **D-11:** **P1 Flyway는 title 최소 골격만 소유.** title(+장르 최소 관계) + `attribution`/`license` 컬럼. 나머지 도메인 테이블은 각 소유 phase가 V 마이그레이션 증분 추가 — 미확정 설계(에셋 매니페스트 형식 등)를 선행 추측하지 않는다.
- **D-12:** **시드 = 한글 골든 ≥5건 + Blender 실자산 메타 행.** 한글 골든("인터스텔라" 등)은 검색 함정(PITFALLS Pitfall 10) 검증용 메타데이터-only — 재생 에셋 없음. Blender 오픈무비 행은 CC-BY attribution 포함, 에셋 경로는 P2 배치 스크립트가 채움.
- **D-13:** **시드는 본선 V 마이그레이션에 포함.** 프로필 분기 없음 — 모든 환경이 데모 성격인 포트폴리오라 "시드 없는 환경"이 존재하지 않는다. 성공 기준 3 문구("Flyway만 돌려도 재현") 그대로.
- **D-14:** **Modulith `event_publication` 테이블은 P1에서 만들지 않는다.** "이벤트 테이블도 Flyway로 관리, `schema-initialization.enabled` 자동 생성 금지" **규율만** P1 ADR/문서에 못박고, 실제 DDL은 `@ApplicationModuleListener` 첫 사용 phase(P3 발행 상태머신 예상)가 추가.

### Claude's Discretion
- compose 파일 구성(단일 파일 + profiles vs override 파일), 헬스체크/`depends_on: service_healthy` 배선 상세
- ADR 문서 형식·위치(루트 docs/ 하위 권장), README 구성
- pre-commit 훅 구현 방식(플레인 git hook vs 도구), 차단 확장자/크기 임계값
- CI workflow yml 구조, 캐싱 전략
- Next.js 스캐폴딩 세부(TS 설정 등 — 버전 핀은 STACK.md 준수), FE Dockerfile 작성
- 8모듈 빈 골격의 패키지 내부 구조(ARCHITECTURE.md 규약 준수 하에), `allowedDependencies` 선언 방식
- title 스키마 컬럼 상세(제목 원제/한국어 이원화 등 — P3 요구 미선점 범위 내)

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase 정의·요구사항
- `.planning/ROADMAP.md` §Phase 1 — Goal·성공 기준 4종·비-요구사항 가드레일 산출물 4종 (누락 금지 명시)
- `.planning/REQUIREMENTS.md` — ARCH-01(Modulith 경계+verify() CI 게이트), ARCH-05(compose 전 스택 기동)

### 리서치 (구현 전 필독)
- `.planning/research/STACK.md` — **모든 버전 핀의 단일 진실** (Boot 3.2.12·Modulith 1.1.12·Gradle 8.x·Flyway+flyway-mysql·resilience4j 2.2.0·Next 16.2·hls.js 1.6.16), §2.6 동일 오리진 근거, §5 What NOT to Use, build.gradle 발췌
- `.planning/research/PITFALLS.md` — Pitfall 10(mysql ngram 파라미터+한글 시드), Pitfall 14(initdb.d 함정 → Flyway 단일 진실), P0 체크리스트(§616-617), 라이선스 컬럼 근거(§472)
- `.planning/research/ARCHITECTURE.md` — `com.moabom` 패키지-단위 모듈 규약(§114, §602 레이어 패키지 금지), 8모듈 골격
- `.planning/research/SUMMARY.md` §P0 Delivers — 첫 커밋 규율 목록

### 프로젝트 수준
- `.planning/PROJECT.md` — Out of Scope(물리 MSA 등 — D-00으로 후반 증설 확정), 리서치 인용-강제 규율, spec-notation v1 채택
- `D:/moabom/CLAUDE.md` — GSD 스택/컨벤션 요약 (본 문서와 동일 출처)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- 코드 없음 — 그린필드. 루트에 `.claude/tech-stack.yaml`(java/springboot 3.2 + mysql 8.0 + jpa) 선언 완료, planning 커밋 5개뿐.

### Established Patterns
- 하네스 22-스테이지 파이프라인(전역 stages.yaml)이 존재하나 P1 CI는 D-09의 최소 코어만 채택 — 파이프라인 전면 배선은 이후 phase에서 증설.

### Integration Points
- git: 로컬 `master` 5커밋 → D-08에 따라 `main` 전환 + remote 생성이 P1 작업에 포함
- 미디어 루트 `D:/moabom-media/`(저장소 밖) — ROADMAP 확정, P1 .gitignore/훅이 전제

</code_context>

<specifics>
## Specific Ideas

- 데모 서사: "`docker compose --profile full up` 하나로 넷플릭스 클론 전체(FE 포함)가 뜬다"가 P1의 보여주기 포인트
- 오리진 규약 검증은 문서가 아니라 실동작 — FE 스텁 페이지가 `/api` 경유로 앱 헬스를 실제 렌더해야 함
- 포트폴리오 진화 서사(D-00): "모듈리스로 시작 → 트래픽이 요구할 때 분리"가 Netflix 실제 경로의 재현임을 README/ADR에 명시할 가치

</specifics>

<deferred>
## Deferred Ideas

- **M1 후반 증설 phase (9~)**: 부하 주입 → 병목 실측 → Redis 캐시/세션 → DB 읽기 복제본 → 모듈 1개 물리 분리 — **본 워크플로 종료 후 `/kha-add-phase`로 로드맵 반영 필요** (2026-08-06 사용자 확정, "진화 서사" 모양). 각 증설 phase의 스코프 상세는 그때 논의.
- 서킷브레이커는 증설 아님 — 기존 Phase 6 (ARCH-02) 그대로.

</deferred>

---

*Phase: 01-foundation-guardrails*
*Context gathered: 2026-08-06*
