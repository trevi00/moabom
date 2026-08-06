---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
status: executing
stopped_at: Completed 01-06-PLAN.md
last_updated: "2026-08-06T05:55:59.536Z"
last_activity: 2026-08-06 -- 01-06 완료 (ADR-0001/0002 + CI 코어 게이트 7종 + Dependabot; 반사실 arm 6종으로 판별력 실측, 인계 대조 커맨드의 vacuous PASS 구멍 1건 발견·차단)
progress:
  total_phases: 8
  completed_phases: 0
  total_plans: 8
  completed_plans: 6
  percent: 75
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-08-06)

**Core value:** 행(row) 기반 홈에서 콘텐츠를 골라 HLS 어댑티브 스트리밍으로 끊김 없이 재생하고, 보던 위치에서 이어본다
**Current focus:** Phase 1 — 기반 + 가드레일

## Current Position

Phase: 1 (기반 + 가드레일) — EXECUTING
Plan: 7 of 8
Status: Ready to execute
Last activity: 2026-08-06 -- 01-06 완료 (ADR-0001/0002 + CI 코어 게이트 7종 + Dependabot; 반사실 arm 6종으로 판별력 실측, 인계 대조 커맨드의 vacuous PASS 구멍 1건 발견·차단)

Progress: [████████░░] 75%

## Performance Metrics

**Velocity:**

- Total plans completed: 6
- Average duration: 15.5 min
- Total execution time: 1.6 hours (93 min)

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 6 / 8 | 93 min | 15.5 min |

**Recent Trend:**

- Last 6 plans: 9 · 21 · 23 · 18 · 11 · 11 min
- Trend: 안정 (최근 3회 태스크당 ~4~6분. 01-06 은 태스크 3개에 11분 — 산출물이 문서·설정이라 실행 검증이 짧았고, 시간의 절반이 반사실 arm 6종과 캐시 배제 재실행에 쓰였다)

*Updated after each plan completion*
| Phase 01 P01 | 9 min | 3 tasks | 5 files |
| Phase 01 P02 | 21 min | 3 tasks | 37 files |
| Phase 01 P03 | 23 min | 2 tasks | 14 files |
| Phase 01 P04 | 18 min | 3 tasks | 3 files |
| Phase 01 P05 | 11 min | 2 tasks | 5 files |
| Phase 01 P06 | 11 min | 3 tasks | 4 files |

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Roadmap 단계에서 확정된 순서 결정 (근거 전문: ROADMAP.md §Ordering Decisions):

- [Roadmap D1]: 인제스트 제품화 = Phase 8(후반, ARCHITECTURE 배치) + 스코프는 PITFALLS 기준(lease/reconciler/재시도 상한/산출물 검증을 **첫 구현에 포함**). 근거 — 콘텐츠 코퍼스가 오픈무비로 상한이 있고, 볼륨 압력은 Phase 2 배치 스크립트 재실행으로 풀린다.
- [Roadmap D2]: 검색 = Phase 7(추천 이후, 마이리스트와 묶음). 근거 — My List가 RowProvider OCP의 최소 실증체라 레지스트리 선행 필요, SRCH-05 랭킹이 RECO-04 인기도 집계 재사용, ngram vs LIKE 실측은 코퍼스 최대 시점에.
- [Roadmap]: 리서치 9-phase 스파인을 8로 압축 — 원 Phase 8(실험/카오스/QoE)이 전부 v2로 결정돼 v1 잔여(ARCH-04 관측)를 Phase 6에 흡수.
- [Roadmap]: BROW-02(행 타입 최소 세트)를 Phase 5가 소유 — 비개인화 4종 라이브 + 개인화(P6)·마이리스트(P7)가 같은 계약에 플러그인 합류, Phase 7 종료 시 6종 완성.
- [Phase 01]: 01-01: .gitignore 전역 *.ts 금지 — 경로 스코프(media/**/*.ts) + 5MB 훅 게이트로 대체 — CP-1: .ts 는 MPEG-TS 세그먼트이자 TypeScript 확장자. frontend/ 가 같은 저장소라 전역 배제는 Next.js 소스를 조용히 삼킨다. check-ignore 반사실 프로브를 acceptance 에 고정
- [Phase 01]: 01-01: ARCH-05 는 mark-complete 하지 않음 — 마지막 기여 플랜(01-08)에서 표기 — ARCH-05 를 7개 플랜이 공유하고 compose.yaml 이 아직 없다. 지금 체크하면 기동되지 않는 스택을 기동된다고 선언하는 것 (CLAUDE.md 정직 표기 우선)
- [Phase 01]: 01-02: shared 를 open module 로 선언 못함 — Modulith 1.1.12 에 type 속성 부재(실측 javap) — 버전 핀 유지 + allowedDependencies={} 일반 모듈로 착지. 소비자가 "shared" 를 명시 추가하는 방향(경계를 넓히지 않음). Phase 3+ 필독
- [Phase 01]: 01-02: ModularityTests 에 경계 명시선언 테스트 추가 — 플랜 명세 3종은 @ApplicationModule 선언 0인 상태에서도 전부 green 이라 Task 3 을 판별하지 못했다 (RESEARCH P-3 를 실행 게이트로 승격)
- [Phase 01]: 01-02: ARCH-01 mark-complete 미실행 — 요구사항 문구의 CI 게이트 절반이 Plan 06 소관이고 .github/workflows 부재. 4개 플랜(01-02/06/07/08) 공유 요구사항
- [Phase 01]: 01-03: RESEARCH A3 반증 — rewrites destination 은 next build 시점에 routes-manifest.json 으로 구워지고 런타임 env 는 무시된다 (반사실 2-arm 실측). Dockerfile 이 목적지를 ARG 로 받고 기본값을 full 프로필(app:8080/nginx:80)로 둔다 — compose 는 build.args, .env 런타임 주입은 무효
- [Phase 01]: 01-03: create-next-app 잔재 삭제(page.module.css/favicon/svg 5종/보일러플레이트 README) + AGENTS.md·CLAUDE.md 유지 — D-05 얇게 vs Next16 학습데이터 경고의 실용 가치
- [Phase 01]: 01-03: ARCH-05 mark-complete 미실행 (01-01·01-02 선례) — compose.yaml 부재, 7개 플랜 공유 요구사항
- [Phase 01]: 01-04: G3 게이트가 withCommand 누락은 못 잡는다 — 핀 3값이 mysql:8.0 이미지 기본값과 동일(mysqld --verbose --help 실측). 값 드리프트는 잡는다(반사실 2->3 = expected 2/but was 3). compose<->test 대조 게이트는 compose 가 실재하는 Plan 05/07 소관
- [Phase 01]: 01-04: TDD RED 구조적 미성립(V1/V2 선행) — 위조하지 않고 반사실 프로브 4종으로 판별력 실측 후 revert. tests=6 failures=4 -> 복원 green 10/10
- [Phase 01]: 01-04: ARCH-05 mark-complete 미실행 (선례 4회째) — compose.yaml 부재, 7개 플랜 공유 요구사항
- [Phase 01]: 01-05: web 오리진을 build.args 로 — 런타임 environment 는 무효(01-03 A3 반증 반영). 플랜 명세와 실측 충돌 시 실측 우선
- [Phase 01]: 01-05: 플래너 자기모순 4·5회째(alias·initdb 리터럴이 자기 grep acceptance 를 깸) — 리터럴만 재작성. 2-Strike 초과, 플랜 생성 단계 정적 게이트 필요
- [Phase 01]: 01-05: nginx -t 는 단독으로 판별력 0 — nginx -T 로 우리 conf 로드 확인 후 rc 판정. MSYS 경로변환이 vacuous PASS 를 만들 수 있는 구조
- [Phase 01]: 01-05: ARCH-05 mark-complete 미실행 (선례 5회째) — compose.yaml 은 생겼으나 증거가 config -q 까지. 실기동 검증은 Plan 07
- [Phase 01]: 01-06: compose<->SchemaMigrationTest 핀 대조를 CI 스텝으로 신설(01-05 HIGH 인계 해소) — 인계 커맨드의 diff-only 판정은 양쪽 동시 삭제 시 조용히 통과(ARM2 실측)라 개수 선-단언을 앞에 붙였다. 대조 게이트 = 값 일치 + 개수 하한
- [Phase 01]: 01-06: next 를 Dependabot ignore 에 넣지 않음 — npm audit high 3건의 유일 수정 경로(next@16.3.0)가 잠긴 핀 16.2.12 와 충돌하지만, 그 충돌을 보이게 두는 것이 G1(non-blocking 가시성)의 목적. 억제=가시성 0, 자동머지=핀 파손
- [Phase 01]: 01-06: CI OPEN guard 는 Modulith 1.1.12 에서 발화 불가한 전방 게이트임을 워크플로 주석에 정직 표기(01-02 javap 실측과 정합) — 게이트를 지우지 않고 1.2+ 상향 시 자동 활성되게 남김
- [Phase 01]: 01-06: ARCH-01/ARCH-05 mark-complete 미실행(선례 6회째) — 워크플로 파일은 생겼으나 CI 가 0회 실행. '게이트 도달성 확인'은 ROADMAP 이 01-08 에 배정, 실기동은 01-07
- [Phase 01]: 01-06: 플래너 자기모순 0건(5회 연속 기록 중단) — 다만 금지 리터럴이 <action> 산문에는 존재하는 near-miss. 산문을 산출물 주석으로 옮겨 적으면 acceptance 가 깨진다

### Pending Todos

[From .planning/todos/pending/ — ideas captured during sessions]

None yet.

### Blockers/Concerns

- **[Phase 2 착수 조건, HIGH]** Apple HLS Authoring Spec 원문을 4개 리서치 문서 전부가 확보 실패(WebFetch 미회수). ffmpeg 공식 HLS muxer 문서도 미확보. **사람이 직접 열어** 세그먼트 길이·사다리 권고 수치를 재확인해야 Phase 2 스파이크의 GOP/키프레임 수치가 근거를 갖는다.
- **[Phase 7, MEDIUM]** 한국어 검색 구현 경로 [열림]. Netflix 회원 검색 1차 원문 부재 → 넷플릭스 패턴 인용 없이 설계. ngram vs LIKE는 **측정 후 채택**(가설 채택 금지).
- **[Phase 8, MEDIUM]** Modulith 1.1.12 이벤트 재발행 프로퍼티 키 이름 미확정, `-progress pipe:1`의 `out_time_ms` 단위(ms/μs) 미확인.
- **[D1 잔여 리스크]** 인제스트 모듈이 마지막이라 `TitleAssetPublished` 이벤트 계약의 실 배선이 Phase 8까지 미검증. 완화 = Phase 1의 8모듈 골격 + `verify()`, Phase 3의 발행 상태머신 선행. 잔여 리스크 비-제로.
- **[D2 잔여 리스크]** 검색이 v1 후반까지 없음 — Phase 5~6 구간에 "검색 없는 데모"가 존재한다.
- **[버전 핀 체크포인트]** resilience4j 2.2.0 고정(2.3.0은 Boot 3 + Java 17에서 기동 실패), Boot 3.2.12 라인 최종 패치 — Phase 1에서 핀, Phase 6·8 착수 시 실측 재확인.
- **[문서 불일치, 해소됨]** REQUIREMENTS.md 헤더가 v1 요구사항 "53 total"이라 적었으나 실측 58개. 로드맵은 58개 전부 매핑, REQUIREMENTS.md Coverage 블록 정정 완료.
- **[Plan 07, HIGH]** 로컬 8080 을 무관한 서비스(EnterpriseDB, PID 5140)가 점유 중 — 백엔드 로컬 기동 전 해제하거나 포트를 옮겨야 한다 (01-03 프로브가 우연히 발견)
- **[Plan 07, HIGH · Plan 05 소관분 해소]** BACKEND_ORIGIN/MEDIA_ORIGIN 은 런타임 변수가 아니다 — rewrites 목적지는 next build 시점에 구워진다(01-03 A3 반증). ✅ 01-05 에서 compose `web` 을 `build.args` 로 배선하고 README·`.env.example` 주석도 정정. 잔여 = Plan 07 이 full 프로필 실기동으로 `curl :3000/api/health` 200 을 확인하는 것
- **[Plan 08+, MEDIUM · Plan 06 소관분 해소]** npm audit high 3건(postcss XSS/경로순회, sharp libvips CVE) — 수정 경로가 next@16.3.0 뿐이라 잠긴 핀 16.2.12 와 충돌. ✅ 01-06 에서 **의도적으로 ignore 하지 않기로 결정** — 억제하면 G1 가시성이 0 이 되고 자동머지하면 핀이 깨진다. 근거를 `dependabot.yml` 주석 + ADR-0001 §3 양쪽에 기록. 잔여 = repo push 후 Dependabot 이 실제로 PR 을 열면 **사람이 핀 vs CVE 를 판단**하는 것
- **[해소됨 — 01-06]** compose mysql `command:` 3종 ↔ SchemaMigrationTest `withCommand` 문자 일치(CP-10)의 **자동 게이트 부재**. ✅ 01-06 이 CI 스텝 `mysql 기동 파라미터 대조 (G3 / CP-10)` 로 승격. 인계받은 diff-only 판정은 **양쪽 동시 삭제 시 조용히 통과**함을 반사실 ARM2 로 실측해, 값 비교 앞에 **개수 선-단언(3종)** 을 배치했다. 잔여 = 이 스텝이 실제로 도는지는 CI 첫 런(01-08) 관측 대상
- **[Plan 08, MEDIUM]** ubuntu CI 첫 런이 Flyway 한글 인코딩의 실검증 지점이다 — 로컬 Windows 는 green(01-06 에서 캐시 배제 실행으로 10/10 재확인)이지만 CI 미실행. `koreanTitlesRoundTrip` 이 붉어지면 `spring.flyway.encoding: UTF-8` 추가로 대응(시드 ASCII 화 회피 금지). 또한 `./gradlew build` 가 **Docker 를 요구**한다(Testcontainers) — ci.yml 은 `ubuntu-latest` 로 고정됨
- [Plan 07, HIGH] Git Bash 로컬 검증엔 MSYS_NO_PATHCONV=1 필수 — '/' 로 시작하는 grep 패턴과 docker run -v 의 컨테이너측 경로가 C:/Program Files/Git/... 로 변환된다. 없으면 조용히 통과하는 vacuous PASS 경로 존재(nginx -t 가 우리 conf 를 안 보고도 rc=0). ubuntu CI 는 무영향
- [Plan 07, MEDIUM] MEDIA_ROOT(D:/moabom-media) 바인드는 파싱만 확인됨 — Docker Desktop 의 D: 드라이브 파일 공유 설정에 따라 실기동에서 마운트 실패 가능. 캐시 헤더(m3u8/세그먼트) 실검증은 실 파일이 없어 P2 소관
- [Plan 08, MEDIUM] CI 2회차 이후 런에서 `:test` 가 build cache 로 서빙될 가능성 — 로컬에서 `./gradlew build` 가 UP-TO-DATE, `cleanTest test` 가 FROM-CACHE 로 두 번 연속 BUILD SUCCESSFUL 을 내면서 테스트를 한 줄도 실행하지 않았다(01-06 실측). gradle/actions/setup-gradle 이 Gradle user home 을 복원하므로 CI 도 동형일 수 있다(미실측). 첫 런은 캐시가 비어 반드시 실행되므로 전제는 유지되나, **초록 체크마크가 아니라 로그에서 `:test` 실행과 tests= 수를 확인**할 것

## Session Continuity

Last session: 2026-08-06T05:55:23.354Z
Stopped at: Completed 01-06-PLAN.md
Resume file: None

Next: `/kha-execute-phase 1` (01-07-PLAN.md — 반사실 프로브 V-SC2d/V-SC4d + full 프로필 실기동 + 가정 A2/A3/A5/A6 해소 + 사람 검수, wave 4)
