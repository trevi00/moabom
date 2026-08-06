---
gsd_state_version: 1.0
milestone: v1.0
milestone_name: milestone
status: verifying
stopped_at: Completed 01-08-PLAN.md (Phase 1 완료 8/8)
last_updated: "2026-08-06T06:49:22.677Z"
last_activity: 2026-08-06
progress:
  total_phases: 8
  completed_phases: 1
  total_plans: 8
  completed_plans: 8
  percent: 100
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-08-06)

**Core value:** 행(row) 기반 홈에서 콘텐츠를 골라 HLS 어댑티브 스트리밍으로 끊김 없이 재생하고, 보던 위치에서 이어본다
**Current focus:** Phase 1 — 기반 + 가드레일

## Current Position

Phase: 1 (기반 + 가드레일) — COMPLETE (8/8 플랜)
Plan: 8 of 8
Status: Phase complete — ready for verification
Last activity: 2026-08-06 -- 01-08 완료 (master→main 전환 + 공개 저장소 trevi00/moabom push + CI 첫 런 게이트 8종 green; :test 실 실행을 3갈래로 증명, 2회차 런이 테스트 0건으로 green 임을 신규 실측, ARCH-01/ARCH-05 7회 이월분 착지)

Progress: [██████████] 100%

## Performance Metrics

**Velocity:**

- Total plans completed: 8
- Average duration: 17.9 min
- Total execution time: 2.4 hours (143 min)

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 8 / 8 | 143 min | 17.9 min |

**Recent Trend:**

- Last 8 plans: 9 · 21 · 23 · 18 · 11 · 11 · 28 · 22 min
- Trend: 01-07(28분)에 이어 01-08 이 22분으로 2위 — 두 플랜 모두 **산출물은 문서 1~3종인데 시간의 대부분이 외부 시스템 대기**에 쓰였다(01-07 = 컨테이너 기동, 01-08 = CI 런 3회 × 각 2m5s/10s + 로그 회수·분석). 01-07 이 세운 "문서 산출량과 소요 시간의 역상관" 관찰이 재확인됐다. **검증·관측 성격의 플랜은 20분대를 기준선**으로 잡을 것 — 구현 플랜(01-02 21분/37파일)과 같은 시간에 산출물은 1/30 이다. 이는 비효율이 아니라 이 phase 가 게이트의 *작동*을 값으로 요구했기 때문이다

*Updated after each plan completion*
| Phase 01 P01 | 9 min | 3 tasks | 5 files |
| Phase 01 P02 | 21 min | 3 tasks | 37 files |
| Phase 01 P03 | 23 min | 2 tasks | 14 files |
| Phase 01 P04 | 18 min | 3 tasks | 3 files |
| Phase 01 P05 | 11 min | 2 tasks | 5 files |
| Phase 01 P06 | 11 min | 3 tasks | 4 files |
| Phase 01 P07 | 28 min | 3 tasks | 5 files |
| Phase 01 P08 | 22 min | 2 tasks | 1 files |

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
- [Phase 01]: 01-07: A2 반증 -> scrape job 2분리. 측정은 8080 점유로 오염됐으나 분리는 충돌과 독립적으로 옳다 (컨테이너 경로=호스트 포트 무의존 결정론적, dev=host-gateway 유일 경로 — 애초에 다른 토폴로지). 포트 충돌은 원인이 아니라 드러내 준 계기
- [Phase 01]: 01-07: 호스트 8080 이중 LISTEN = (c) 현 상태 수용 + Phase 2 이월. httpd.exe(5140)와 Docker 가 동시 LISTEN 성공(Windows SO_REUSEADDR), 출처에 따라 응답자 갈림(localhost=Spring / 컨테이너 gateway=Apache). Linux 면 바인드 실패로 즉시 드러났을 조건
- [Phase 01]: 01-07: 빌더 다이제스트는 기록이지 핀이 아님 — build.gradle 은 여전히 :latest. 재현성 미확보를 ADR 6 에 명시. 핀 승격 조건 = 날짜에 따라 빌드가 갈리는 사고 1회
- [Phase 01]: 01-07: ARCH-01/ARCH-05 mark-complete 미실행 (선례 7회째) — CI 0회 실행 + 문서대로의 한 커맨드 기동이 이 머신에서 미충족(grafana 3001 바인드 실패, 오버레이 시에만 exit 0)
- [Phase 01]: 01-08: push protection 을 플랜 명세 순서(push 이후)가 아니라 첫 push 이전에 활성화 — 그대로 하면 43커밋 일괄 push 가 GitHub 측 방어 없이 통과해 must_haves 첫 문장(가드레일이 첫 push 이전에 존재)과 어긋난다. 멱등 PATCH 라 비용 0
- [Phase 01]: 01-08: ARCH-01/ARCH-05 mark-complete — 7회 이월 착지. ARCH-01 은 CI 실행 관측으로 조건 충족. ARCH-05 는 caveat 명기 후 체크(6서비스 실기동은 01-07 증명, 잔여 3001 점유는 무관 컨테이너에 의한 환경 조건이지 산출물 결함 아님 — 리뷰어 반대 가능 지점이라 근거를 SUMMARY 에 병기)
- [Phase 01]: 01-08: CI 캐시 구멍을 발견했으나 ci.yml 미수정 — D-09 가 P1 CI 범위를 잠갔고 해소안 3종이 CI 시간·판별력 트레이드오프를 가진다((b) 개수단언은 단독으로는 무효). 게이트 정책 변경은 '확인+기록' 태스크 범위 밖 → 대가 표와 함께 Phase 2 operator 결정으로 이월
- [Phase 01]: 01-08: 플래너 자기모순 6회차 재발(01-06 의 0건 기록 중단) — 플랜 로그 템플릿의 결론 자리표시자가 자기 acceptance(해당 영단어 0회)를 깬다. 실측값으로 치환 + 한국어 서술로 회피. 플랜 생성 단계 정적 게이트 필요성 재확인

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
- **[대체됨 — 01-07]** ~~로컬 8080 을 무관한 서비스(EnterpriseDB, PID 5140)가 점유 중~~ → 01-07 이 정체를 실측 확정(`httpd.exe`, PID 5140)하고 **증상이 예상과 달랐음**을 발견: 바인드가 실패하지 않고 Docker 와 **동시 LISTEN 에 성공**했다. 아래 `[Phase 2 착수 전, HIGH]` 항목으로 대체.
- **[해소됨 — 01-07]** BACKEND_ORIGIN/MEDIA_ORIGIN 은 런타임 변수가 아니다 — rewrites 목적지는 next build 시점에 구워진다(01-03 A3 반증). ✅ 01-05 에서 compose `web` 을 `build.args` 로 배선. ✅ **01-07 이 full 프로필 실기동으로 `curl :3000/api/health` → `{"status":"UP","app":"moabom","ts":...}` 200 을 확인** — 대체 구조(D-06)가 실동작함이 증명됐다. 잔여 없음.
- **[Plan 08+, MEDIUM · Plan 06 소관분 해소]** npm audit high 3건(postcss XSS/경로순회, sharp libvips CVE) — 수정 경로가 next@16.3.0 뿐이라 잠긴 핀 16.2.12 와 충돌. ✅ 01-06 에서 **의도적으로 ignore 하지 않기로 결정** — 억제하면 G1 가시성이 0 이 되고 자동머지하면 핀이 깨진다. 근거를 `dependabot.yml` 주석 + ADR-0001 §3 양쪽에 기록. 잔여 = repo push 후 Dependabot 이 실제로 PR 을 열면 **사람이 핀 vs CVE 를 판단**하는 것
- **[해소됨 — 01-06]** compose mysql `command:` 3종 ↔ SchemaMigrationTest `withCommand` 문자 일치(CP-10)의 **자동 게이트 부재**. ✅ 01-06 이 CI 스텝 `mysql 기동 파라미터 대조 (G3 / CP-10)` 로 승격. 인계받은 diff-only 판정은 **양쪽 동시 삭제 시 조용히 통과**함을 반사실 ARM2 로 실측해, 값 비교 앞에 **개수 선-단언(3종)** 을 배치했다. 잔여 = 이 스텝이 실제로 도는지는 CI 첫 런(01-08) 관측 대상
- **[Plan 08, MEDIUM]** ubuntu CI 첫 런이 Flyway 한글 인코딩의 실검증 지점이다 — 로컬 Windows 는 green(01-06 에서 캐시 배제 실행으로 10/10 재확인)이지만 CI 미실행. `koreanTitlesRoundTrip` 이 붉어지면 `spring.flyway.encoding: UTF-8` 추가로 대응(시드 ASCII 화 회피 금지). 또한 `./gradlew build` 가 **Docker 를 요구**한다(Testcontainers) — ci.yml 은 `ubuntu-latest` 로 고정됨
- **[갱신됨 — 01-07, MEDIUM]** Git Bash 검증엔 `MSYS_NO_PATHCONV=1` 필수 — '/' 로 시작하는 grep 패턴과 docker `-v` 컨테이너측 경로가 변환된다. ⚠️ **01-07 이 역방향 함정도 실측**: `MSYS_NO_PATHCONV=1` 을 켠 상태에서 `docker compose -f /c/Users/...` 를 주면 이번엔 변환이 **안 돼서** docker 가 `D:\c\Users\...` 로 해석해 실패한다. 규칙 = **컨테이너측 경로엔 켜고, 호스트 경로 인자는 Windows 형식(`C:/...`)으로 준다**. ubuntu CI 는 무영향
- **[부분 해소 — 01-07, MEDIUM]** MEDIA_ROOT(D:/moabom-media) 바인드 — ✅ **실기동에서 nginx 가 정상 기동했다**(D: 드라이브 파일 공유 문제 없음, `/healthz` → `ok`). 잔여 = 캐시 헤더(m3u8/세그먼트) 실검증은 실 미디어 파일이 없어 여전히 P2 소관
- [Plan 08, MEDIUM] CI 2회차 이후 런에서 `:test` 가 build cache 로 서빙될 가능성 — 로컬에서 `./gradlew build` 가 UP-TO-DATE, `cleanTest test` 가 FROM-CACHE 로 두 번 연속 BUILD SUCCESSFUL 을 내면서 테스트를 한 줄도 실행하지 않았다(01-06 실측). gradle/actions/setup-gradle 이 Gradle user home 을 복원하므로 CI 도 동형일 수 있다(미실측). 첫 런은 캐시가 비어 반드시 실행되므로 전제는 유지되나, **초록 체크마크가 아니라 로그에서 `:test` 실행과 tests= 수를 확인**할 것
- [Phase 2 착수 전, HIGH] 호스트 8080 정리 — operator 결정 필요(httpd.exe PID 5140 정지 or 앱 포트 이동). 01-07 결정 = (c) 현 상태 수용 이월. Docker 와 httpd 가 동시 LISTEN 에 성공해 출처별로 응답자가 갈린다(localhost=Spring / 컨테이너 gateway=Apache) — dev 모드(bootRun)와 host-gateway 스크레이프가 이 문제를 그대로 받는다
- [operator, MEDIUM] 01-HUMAN-UAT.md U-1~U-5 미확인 (status: partial). T3 승인은 HTTP 대리 검증 근거였고 육안 렌더 3종 + 문서 설득력 판단 2종은 사람 몫으로 남았다
- [Phase 3, MEDIUM] app 의 0.0.0.0:8080 발행이 근거를 잃음 — A2 대안으로 메트릭이 compose 네트워크로 들어오므로 LAN 노출을 감수할 이유가 사라졌다(ADR-0001 7/9). 루프백 조임은 dev 워크플로·README 동반 변경이라 보류
- [Phase 6, LOW] Grafana 대시보드는 moabom-app-container 와 moabom-app-host 두 job 을 합집합으로 질의해야 한다 — 어느 모드든 하나는 항상 DOWN 인 것이 설계다
- [Phase 2 착수 전, HIGH · 01-08 신규 실측] CI 2회차 이후 런이 테스트 0건으로 green — run 31078140031 이 build cache 를 복원해 ':test FROM-CACHE' + '4 executed, 3 from cache' + 10s(1회차 1m34s). 01-06 의 추정이 CI 층 관측으로 확정됐다. 첫 런의 도달성 증거는 유효(캐시 비어 있었음)하나 이후 초록은 실행 증거가 아니다. T-1-39 완화가 첫 런 1회만 덮는다는 한계 표면화. 해소안 (a)테스트만 캐시배제 (b)개수 하한 단언 — 단독 무효, XML 도 함께 복원됨 (c)수용. 권장 (a)+(b), operator 결정
- [operator, MEDIUM · 01-08] LICENSE 파일 부재 — 공개 포트폴리오 저장소에 라이선스가 없으면 기본 all-rights-reserved 로 해석돼 리뷰어 감점 요인. 플랜 명세에 없어 임의 추가하지 않았다(라이선스 선택은 사용자 결정). README 는 상표 회피·CC-BY attribution 을 이미 다루므로 저작권 리스크 자체는 낮음
- [Phase 2 착수 전, HIGH · 01-08 실측 재확인] ffmpeg/ffprobe 가 PATH 에 없다 (which 둘 다 미발견, ffmpeg -version → command not found). STACK 지목 버전 8.0.3. ingest 의 ProcessBuilder 어댑터 전제라 P2 HLS 스파이크가 이것 없이 시작 불가

## Session Continuity

Last session: 2026-08-06T06:48:39.501Z
Stopped at: Completed 01-08-PLAN.md (Phase 1 완료 8/8)
Resume file: None

Next: `/kha-execute-phase 1` (01-08-PLAN.md — master→main 전환 + GitHub public repo 생성/push + **CI 첫 런 게이트 도달성 확인**, wave 5 / Phase 1 마지막)

01-08 이 Phase 1 의 마지막 플랜이며, 여기서 처음으로 CI 가 실제로 돈다. 그때 확인할 것:

- ARCH-01 / ARCH-05 mark-complete (7회 미룬 것 — 01-08 이 마지막 기여 플랜)
- 게이트 7종의 **실 발화** (초록 체크마크가 아니라 로그의 `:test` 실행 + `tests=` 수)
- 미디어 히스토리 스캔의 첫 발화 (로컬 훅의 실 boundary)
