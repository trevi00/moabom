---
phase: 01-foundation-guardrails
plan: 08
subsystem: infra
tags: [github, public-repo, default-branch, secret-scanning, push-protection, ci-reachability, vacuous-pass, build-cache, gate-liveness]

# Dependency graph
requires:
  - phase: 01-01
    provides: ".gitignore / .gitattributes(eol=lf) / .githooks/pre-commit — 첫 push 에 포함돼야 하는 가드레일 실체"
  - phase: 01-02
    provides: "8모듈 package-info + ModularityTests — CI 모듈수·verify() 게이트의 피검사체"
  - phase: 01-04
    provides: "SchemaMigrationTest(Testcontainers) — ubuntu 첫 런에서 한글 인코딩이 실검증된 지점"
  - phase: 01-06
    provides: ".github/workflows/ci.yml 게이트 8종 + '초록은 실행 증거가 아니다' 인계 블로커"
  - phase: 01-07
    provides: "01-COUNTERFACTUAL-LOG.md — 로컬 층 반사실 증명 (본 플랜의 CI 층 대조 상대)"
provides:
  - "공개 저장소 github.com/trevi00/moabom (public, default branch main, secret scanning + push protection enabled, branch protection 없음)"
  - "01-CI-GATE-LOG.md — CI 첫 런 게이트 8종 스텝별 conclusion + :test 실 실행 증거 3갈래 + fetch-depth 실적용 확인"
  - "2회차 런이 테스트 0건으로 green 임을 CI 층에서 실측 (01-06 이 남긴 추정의 관측적 해소)"
  - "V-CP5 확정: Windows→ubuntu 전이에서 eol/exec bit/한글 인코딩 3종 무사고"
  - "ARCH-01 / ARCH-05 mark-complete (7회 이월분 착지)"
affects: [02-media-playback, 03-auth-events, 06-observability, 08-ingest]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "push 이전에 CI 게이트를 로컬에서 그대로 재현한다 — 되돌릴 수 없는 경계 앞의 비용은 항상 더 싸다"
    - "게이트 도달성은 conclusion 이 아니라 태스크 회계(`N executed`)와 부작용 로그(컨텍스트 생명주기)로 판정한다"
    - "예측을 기록했으면 같은 플랜 안에서 관측으로 닫는다 — 다음 push 가 이미 그 실험이다"
    - "되돌릴 수 없는 방어는 경계를 넘기 *전에* 켠다 (push protection 을 첫 push 앞으로)"

key-files:
  created:
    - .planning/phases/01-foundation-guardrails/01-CI-GATE-LOG.md
  modified: []

key-decisions:
  - "push protection 을 플랜 명세 순서(push 이후)가 아니라 **첫 push 이전**에 활성화 — 플랜 자신의 핵심 명제('가드레일이 첫 push 이전에 존재')와의 정합. 비용 0, 멱등"
  - "ARCH-01 mark-complete — CI 가 실제로 실행돼 verify() 가 돌았다. 7회 이월의 조건이 실측으로 충족"
  - "ARCH-05 mark-complete (caveat 명기) — compose 토폴로지 6서비스 실기동은 01-07 이 증명했고 CI 가 clean ubuntu 에서 config 유효성을 상시 확인한다. 잔여(호스트 3001 점유)는 무관 컨테이너에 의한 **환경 조건**이지 산출물 결함이 아님"
  - "CI 캐시 구멍을 발견했으나 ci.yml 을 고치지 않음 — D-09 가 P1 CI 범위를 잠갔고 해소안 3종이 CI 시간·설계 트레이드오프를 가진다. 대가 표와 함께 Phase 2 operator 결정으로 이월"
  - "CI 층 반사실(의도적 위반 push)은 수행하지 않음 — 공개 이력에 위반 커밋을 남기지 않기 위해. 로컬 층에서 같은 술어를 증명했다는 것이 근거이며, 이것이 논증이지 관측이 아님을 로그에 명기"

patterns-established:
  - "vacuous-PASS 판정 3갈래: 태스크 회계 라인 + 캐시 접미사 부재 + 실 부작용 로그(Hikari/JPA 생명주기)"
  - "게이트의 전제도 검사한다 — `fetch-depth: 0` 이 실제 `git fetch` 에 `--depth` 부재로 나타나는지까지 확인"
  - "green 이 무의미한 게이트는 그렇다고 적는다 (OPEN guard = 오늘 발화 불가한 전방 게이트)"

requirements-completed: [ARCH-01, ARCH-05]

# Metrics
duration: 17min
completed: 2026-08-06
---

# Phase 1 Plan 08: 공개 저장소 + CI 첫 런 게이트 도달성 Summary

**로컬 `master` 를 `main` 으로 전환하고 가드레일이 이미 작동하는 상태로 `trevi00/moabom` 을 공개 저장소로 만들어 push 했으며, CI 첫 런에서 게이트 8종이 전부 실행돼 green 임을 스텝별로 기록했다 — 그 과정에서 "초록 체크마크는 실행 증거가 아니다"라는 01-06 의 경고를 3갈래 증거로 닫았고, 곧이어 2회차 런이 테스트를 0건 실행하고도 green 이라는 사실을 CI 층에서 처음으로 실측했다.**

## Performance

- **Duration:** 17 min
- **Started:** 2026-08-06T06:30:25Z
- **Completed:** 2026-08-06T06:47:00Z
- **Tasks:** 2 / 2
- **Files created:** 1 (213 insertions, `git diff --shortstat d45e800..HEAD` 실측)

## Accomplishments

- **D-07 착지 — 공개 저장소가 가드레일과 함께 태어났다.** `trevi00/moabom` 이 `public` 으로 존재하고 `.gitignore`·`.gitattributes`·`.githooks/pre-commit` 이 **첫 push 에 포함**됐다(원격 contents API 로 3종 각각 확인). 43 커밋 전체가 한 번에 공개됐고, 그 시점에 이미 4중 방어가 작동 중이었다.
- **D-08 착지** — 기본 브랜치 `main`, push 트리거 CI 가 실제로 발화(event=`push`, headBranch=`main`), branch protection 미설정(404 확인). solo 모드 결정이 그대로 집행됐다.
- **push protection 을 첫 push 앞으로 당겼다** — 아래 §Deviations 1. 플랜은 push 이후 설정을 명세했으나, 그러면 "가드레일이 첫 push 이전에 존재한다"는 이 플랜 자신의 핵심 명제가 GitHub 측 방어에 대해서는 거짓이 된다.
- **CI 첫 런 green, 게이트 8종 전부 실행** — 스텝 16개 전부 `success`, 붉은 스텝 0. 게이트는 플랜이 예상한 7종 + 01-06 이 신설한 mysql 핀 대조 1종 = 8종이다.
- **"초록은 실행 증거가 아니다"를 3갈래로 닫았다** — 01-06 이 로컬에서 이중 vacuous PASS(`UP-TO-DATE` → `FROM-CACHE`)를 실측하고 CI 층 확인을 이 플랜에 인계했다. (1) `7 actionable tasks: 7 executed`, (2) `> Task :test` 에 캐시 접미사 부재, (3) `HikariPool-1` 열림/닫힘 + JPA EntityManagerFactory 종료 로그 = 실 DB 커넥션 = Testcontainers 기동. 시간축도 정합(테스트에 약 39초).
- **게이트의 *전제*까지 검사했다** — 01-06 arm 6b 가 "얕은 클론이면 미디어 스캔이 조용히 no-op"임을 실측했으므로, 첫 런의 checkout 원문에서 `git fetch` 에 **`--depth` 플래그가 없음**을 확인했다. 스텝 1 은 43 커밋 전체를 봤다.
- **V-CP5 확정 + 인계 블로커 2건 해소** — `gradlew` 가 인덱스 `i/lf` + 모드 `100755` 로 커밋돼 있어(push 전 선-확인) ubuntu 에서 `sh\r`·`Permission denied` 둘 다 발생하지 않았다. **`koreanTitlesRoundTrip` 도 통과**해 `spring.flyway.encoding` 대응이 불필요했다(01-04→01-06 인계 블로커 해소, 시드 ASCII 화 회피 규율 유지).
- **RESEARCH A7 이 CI 층에서 재확인** — `dependencyInsight` 출력에 `2.2.0` + `Selection reasons: - Forced` 가 그대로 나타나 `resolutionStrategy.force` 가 G2 게이트 패턴과 무충돌임이 러너에서도 확인됐다.
- **[신규 발견, HIGH] 2회차 런이 테스트 0건으로 green** — 아래 §Issues 1. 01-06 이 "가능성(미실측)"으로 남긴 것을 **관측으로 승격**했다.
- **ARCH-01 / ARCH-05 착지** — 7회 연속 이월된 두 요구사항을 이번에 체크했다(§요구사항 상태에 근거와 caveat).

## Task Commits

1. **Task 1: main 전환 + 공개 저장소 생성 + push + 시크릿 스캐닝** — **커밋 없음** (의도적). 산출물이 전부 원격 측 상태(저장소 생성·가시성·보안 설정·브랜치 전환)라 워킹트리 변경이 0이다. 플랜의 `<files>` 도 `-` 다. 증거는 §검증 실측의 12종 API 확인.
2. **Task 2: CI 첫 런 게이트 도달성 확인 + 기록** — `1601c29` (docs) + `c2f3aec` (docs, 2회차 실측 추가)

## Files Created

- `.planning/phases/01-foundation-guardrails/01-CI-GATE-LOG.md` (213줄) — Run 메타 / 스텝별 conclusion 표 8종 / resilience4j 출력 원문 / **실행 증거 3갈래** / 증명하지 못하는 것(정직) / `fetch-depth` 전제 확인 / V-CP5 / 두 층 방어 대조 + 저장소 측 방어 표 / 잔여 6종

## 검증 실측

### Task 1 acceptance (플랜 명시 criterion 전수 + 추가 2종)

| # | 검사 | 실측 |
|---|---|---|
| 1 | `git rev-parse --abbrev-ref HEAD` | `main` ✅ |
| 2 | `git remote get-url origin` | `https://github.com/trevi00/moabom.git` ✅ |
| 3 | `git status --porcelain` | 빈 출력 ✅ |
| 4 | `git rev-parse HEAD` == `origin/main` | 양쪽 `d45e800…` ✅ |
| 5 | visibility | `PUBLIC` ✅ |
| 6 | defaultBranchRef | `main` ✅ |
| 7 | secret_scanning | `enabled` ✅ |
| 8 | push_protection | `enabled` ✅ |
| 9 | 원격 `.gitignore` | `.gitignore` ✅ |
| 10 | 원격 `.githooks/pre-commit` | `pre-commit` ✅ |
| 11 | 원격 `.gitattributes` (추가) | `.gitattributes` ✅ |
| 12 | branch protection | `404 Branch not protected` ✅ (D-08) |
| — | 플랜 `<verify>` | `PUBLIC_REPO_OK` rc=0 |

**push 이전 로컬 재현 3종 (Step 0)** — 전부 빈 출력: 5MB 초과 blob 0 / 미디어 확장자 추가 이력 0 / shared 외 `Type.OPEN` 0. 부수 확인: gitignore sanity 3/3, 모듈 n=8·m=8, mysql 핀 3종 문자 일치. **시크릿 스팟체크**도 함께 했다 — 시크릿성 파일명(`.env`/`*.pem`/`*.key`/`id_rsa`/credentials) 이력 0건, 고위험 리터럴(AWS 키·PRIVATE KEY 블록·`ghp_` 토큰) 0건, 추적 파일 103개.

### Task 2 acceptance (플랜 명시 criterion 전수)

| # | 검사 | 실측 |
|---|---|---|
| 1 | 최근 런 conclusion | `success` ✅ |
| 2 | 최근 런 headBranch | `main` ✅ |
| 3 | 로그 파일 존재 | ✅ |
| 4 | 스텝 이름 7종 매칭 줄 ≥ 7 | **8** ✅ |
| 5 | 붉은 결론 표기 0 | **0** ✅ |
| 6 | Run URL 존재 | ✅ |
| 7 | `V-CP5` 섹션 | ✅ |
| 8 | 두 층 대조 + `boundary` 표기 | ✅ |
| 9 | success 스텝 수 ≥ 9 | **16** ✅ |
| — | must_haves `contains: resilience4j pin gate` | 존재 ✅ |
| — | 플랜 `<verify>` | `CI_GATE_REACHABLE` rc=0 (편집 후 재실행도 rc=0) |

### CI 첫 런 — 게이트 8종 스텝별 결론

Run [31077663272](https://github.com/trevi00/moabom/actions/runs/31077663272) · push/main · `success` · 2m5s · commit `d45e800`

| 스텝 | conclusion | 구간 |
|---|---|---|
| media guard (SC4) | success | 06:33:15 |
| gitignore sanity (CP-1 회귀 / V-SC4c) | success | 06:33:15 |
| modulith OPEN guard (SC2 / V-SC2b) | success | 06:33:15 |
| module declaration coverage (SC2 / V-SC2c) | success | 06:33:15 |
| mysql 기동 파라미터 대조 (G3 / CP-10) | success | 06:33:15 |
| build + test (verify() + SchemaMigrationTest) | success | 06:33:18 → 06:34:52 |
| resilience4j pin gate (G2 / V-G2a) | success | 06:34:52 → 06:35:01 |
| compose config validity (ARCH-05 / V-SC1a) | success | 06:35:01 |

## Decisions Made

1. **push protection 을 첫 push 이전으로 이동** (§Deviations 1). 플랜 Step 4 를 Step 3 앞으로 당겼을 뿐 내용은 그대로다.
2. **ARCH-01 mark-complete** — 요구사항 문구의 "`ApplicationModules.verify()` CI 게이트"가 성립하려면 CI 가 실행돼야 했고, 이번에 실행돼 실 테스트까지 돌았다. 7회 이월의 조건이 실측으로 충족됐다.
3. **ARCH-05 mark-complete, caveat 명기** — §요구사항 상태 참조. 이 판단은 리뷰어가 반대할 수 있는 지점이므로 근거와 잔여를 같은 자리에 적었다.
4. **CI 캐시 구멍을 발견했으나 ci.yml 을 수정하지 않음** — D-09 가 P1 CI 범위를 잠갔고, 해소안 3종이 각각 CI 시간·판별력의 트레이드오프를 가진다(그중 (b)는 단독으로는 이 구멍을 못 막는다). 게이트 정책 변경은 이 태스크("확인 + 기록")의 범위를 넘고 되돌리기 쉬운 결정이 아니므로, **대가 표와 함께 Phase 2 operator 결정으로 이월**했다.
5. **CI 층 반사실 미수행** — 공개 이력에 의도적 위반 커밋을 남기지 않는다는 플랜 명시 근거를 따랐다. 다만 그 결과 CI 층의 **참 양성은 0회 관측**이며, 우리가 가진 것은 8종의 거짓 음성 부재뿐임을 로그에 명기했다.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] push protection 활성화를 첫 push *이전*으로 이동**
- **Found during:** Task 1 (Step 3 직전)
- **Issue:** 플랜은 Step 3(push) → Step 4(secret scanning + push protection) 순서를 명세한다. 그대로 하면 **첫 push 이 GitHub 측 방어 없이 통과**한다. 이는 이 플랜 자신의 must_haves 첫 문장("가드레일이 첫 push 이전에 존재한 상태로 공개 저장소가 만들어진다")과 네 번째 문장("public repo 에 secret scanning + push protection 이 켜져 있다")이 시간 순서에서 어긋나는 지점이다. 43 커밋을 한 번에 올리는 push 라 노출량도 가장 크다.
- **Fix:** Step 4 의 `gh api -X PATCH` 를 Step 3 앞에서 실행. 커맨드·페이로드는 플랜 그대로이고 순서만 바꿨다(멱등이라 재실행 위험 없음).
- **Verification:** push **이전** 시점에 `public` / `main` / `enabled` / `enabled` 확인 → 그 상태에서 push. push 이후 재확인도 동일.
- **Committed in:** — (원격 측 설정, 워킹트리 변경 없음)

**2. [Rule 2 - Missing Critical] 플랜 로그 템플릿의 자기모순 회피 — 붉은 결론 리터럴**
- **Found during:** Task 2
- **Issue:** 플랜이 제시한 로그 템플릿 본문에 결론 자리표시자가 `success|<붉은 결론 영단어>` 형태로 들어 있는데, **같은 플랜의 acceptance 는 그 영단어의 출현 수가 0 이어야 한다**고 요구한다. 템플릿을 그대로 옮겨 적으면 자기 acceptance 가 깨진다. 01-01~01-05 에서 5회 나온 "플래너 자기모순" 패턴의 **6회차**이며, 01-06 은 0건(near-miss)이었으므로 재발이다.
- **Fix:** 해당 자리를 실측값 `success` 로 치환하고, 문서 전체에서 그 영단어를 쓰지 않고 한국어("붉어짐"/"붉은 스텝")로 서술.
- **Verification:** `grep -ci` 카운트 **0** (초안·편집 후 2회 확인)
- **Committed in:** `1601c29`

**3. [Rule 2 - Missing Critical] 게이트 로그를 예측이 아니라 관측으로 닫음 — 2회차 런 실측 추가**
- **Found during:** Task 2 (초판 작성 직후)
- **Issue:** 초판은 build cache 저장 로그를 근거로 "다음 런에서 `:test` 가 캐시 서빙될 **조건이 갖춰졌다**"까지만 적었다. 그런데 이 플랜의 문서 커밋을 push 하는 순간 그 실험이 자동으로 수행된다 — 예측을 남기고 관측을 버리는 것은 이 phase 의 규율("게이트 가치는 라벨이 아니라 결과로")과 어긋난다.
- **Fix:** 2회차 런 [31078140031](https://github.com/trevi00/moabom/actions/runs/31078140031) 의 로그를 읽어 `Restored cache entry …` / `:test FROM-CACHE` / `4 executed, 3 from cache` / `10s` 를 원문 인용하고, 잔여 항목을 **[HIGH · 실측 확정]** 으로 승격 + 해소안 3종 대가 표 추가.
- **Verification:** acceptance 전수 재실행 rc=0 (붉은 결론 리터럴 0 유지)
- **Committed in:** `c2f3aec`

---

**Total deviations:** 3 auto-fixed (3× Rule 2)
**Impact on plan:** acceptance 를 하나도 깨지 않았고(전수 재실행 확인), 산출물 1종·D-07/D-08 명세도 그대로다. 1번은 순서만 바꿨고 2·3번은 문서 품질 보강이다. 스코프 크리프 없음 — 특히 발견한 CI 결함에 대해 `ci.yml` 을 건드리지 않았다(결정 4).

## Issues Encountered

**1. [HIGH · 신규] 2회차 CI 런은 테스트를 0건 실행하고 green 이다**

첫 런의 post 스텝이 `Saved cache entry with key gradle-build-cache-v2-daae706698ad081b77dd835d8d020023` 로 Gradle build cache 를 GitHub Actions 캐시에 저장했고, 이 플랜의 문서 커밋 `1601c29` 로 발화한 2회차 런이 같은 키를 복원해:

```
Restored cache entry with key gradle-build-cache-v2-daae706698ad081b77dd835d8d020023 in 982ms
> Task :testClasses UP-TO-DATE
> Task :test FROM-CACHE
BUILD SUCCESSFUL in 10s
7 actionable tasks: 4 executed, 3 from cache
```

1m34s → 10s. **초록이면서 테스트가 한 줄도 돌지 않았다.** 01-06 이 로컬에서 본 이중 vacuous PASS 가 CI 층에서 그대로 재현됐고, 이번엔 추정이 아니라 관측이다.

- 첫 런의 무오염성은 훼손되지 않는다(캐시가 비어 있었다) — 이 SUMMARY 의 도달성 주장은 그대로 유효하다.
- 소스가 바뀌면 캐시 키가 달라져 재실행되므로 "코드 변경 커밋은 실행된다"는 성립한다. 문제는 **그 판정을 사람이 매번 로그로 해야 한다**는 것이다.
- 이 플랜 위협 등록의 **T-1-39("게이트가 존재하지만 실행되지 않는 상태로 방치")의 완화가 첫 런 1회만 덮는다**는 사실이 드러났다. 완화 설계 자체의 한계다.
- 해소안 (a) 테스트만 캐시 배제 / (b) 개수 단언 스텝 / (c) 현 상태 수용 — 대가 표는 `01-CI-GATE-LOG.md` §잔여. **(b)는 단독으로는 무효**다(FROM-CACHE 여도 test-results XML 이 함께 복원된다). 권장 (a)+(b) 병행, **Phase 2 operator 결정**.

**2. 테스트 개수(`tests=`)는 CI 로그에서 얻을 수 없다**

Gradle 은 전부 통과하면 개수를 출력하지 않는다. 따라서 이번 증거는 "`:test` 가 실행됐고 붉은 테스트가 0개"까지이며 "10/10 이 실행됐다"는 **로컬 실측(01-06)** 의 수치다. 다만 Hikari/JPA 컨텍스트 로그가 최소 1개 이상의 `@SpringBootTest` 실행을 증명하므로 "0건 수집"은 배제된다. 개수 하한 단언은 이슈 1의 (b) 와 함께 P2 후보.

**3. `Deprecated Gradle features … incompatible with Gradle 9.0` 경고** — STACK 이 이미 Gradle 9.x 를 지원 목록 밖으로 판정했으므로 현 핀(8.14.5)과 무충돌. Boot 상향 시 동반 처리 항목으로만 기록.

**4. ARCH-05 의 환경 잔여가 여전히 살아 있다 (실측)** — `docker ps` 로 `exoplanet-grafana` 가 `0.0.0.0:3001->3000/tcp` 를 점유 중이고 `netstat` 상 8080 도 PID 5140(`httpd.exe`)이 LISTEN 중임을 재확인했다. 01-07 의 (c) 이월 결정이 유효한 상태 그대로다.

## Known Stubs

없음. 이 플랜의 산출물은 문서 1종이며 자리표시자·미배선 컴포넌트가 없다.

관련 항목으로, 01-06 이 남긴 ADR-0001 §6 빌더 다이제스트 자리표시자는 **01-07 이 이미 채웠다**(다이제스트 3종 기록). 미해소 자리표시자는 phase 전체에 없다.

## 요구사항 상태 — 7회 이월 후 착지

| 요구사항 | 판정 | 근거 / caveat |
|---|---|---|
| **ARCH-01** — Modulith 모듈 경계 + `ApplicationModules.verify()` CI 게이트 | **Complete** | 8모듈 선언(01-02) + `verify()` 를 도는 CI 게이트가 존재하고(01-06) **실제로 실행돼 green**(본 플랜). 이월 사유였던 "CI 0회 실행"이 소멸했다. 보조 게이트(모듈수 8/8, OPEN guard)도 함께 실행. caveat: OPEN guard 는 Modulith 1.1.12 에서 발화 불가한 전방 게이트라 그 green 은 무의미하다 — 경계 보증의 실체는 `verify()` 와 모듈수 게이트다 |
| **ARCH-05** — docker-compose 한 번으로 전 스택 기동 | **Complete (caveat)** | compose 토폴로지가 완비됐고(01-05) full 프로필 **6서비스 실기동으로 mysql·nginx·prometheus·grafana 4종 전부 기동이 증명됐다**(01-07). CI 가 clean ubuntu 에서 `--profile full config -q` 를 상시 확인한다(본 플랜 스텝 8). **caveat:** 이 머신에서는 무관 컨테이너(`exoplanet-grafana`)가 3001 을 점유해 문서 그대로의 한 커맨드가 exit 1 이고, 01-07 은 저장소 밖 오버레이로 우회해 검증했다. 이는 **호스트 환경 조건이지 산출물 결함이 아니라고 판단**해 체크했다 — 리뷰어가 이 판단에 반대할 수 있으므로 근거를 여기 남긴다. 3001 해소 후 오버레이 없는 재확인은 operator 항목 |

## Phase 2 착수 조건 인수인계

**(a) ffmpeg 가 PATH 에 없다 — 착수 전 설치 필요.** 실측: `which ffmpeg` / `which ffprobe` 둘 다 미발견, `ffmpeg -version` → `command not found`. STACK 은 **8.0.3**(또는 7.1.5 LTS 성격)을 지목한다. `ingest` 의 `FfmpegCommand`/`FfprobeCommand` 어댑터(ProcessBuilder)가 전제하는 바이너리이므로 P2 HLS 스파이크가 이것 없이는 시작되지 않는다.

**(b) Apple HLS Authoring Spec 원문을 사람이 직접 열어야 한다.** 4개 리서치 문서 전부가 WebFetch 회수에 실패했고 ffmpeg 공식 HLS muxer 문서도 미확보다. **세그먼트 길이·비트레이트 사다리 권고 수치**가 근거 없이 정해지면 P2 스파이크의 GOP/키프레임 정렬이 추측이 된다(STACK §2.4: `-hls_time` 과 GOP 미정렬 = ABR 전환 끊김). STATE 블로커 HIGH 유지.

**(c) 호스트 8080 이중 LISTEN + 3001 점유 (01-07 이월).** `httpd.exe`(PID 5140)와 Docker 가 동시 LISTEN 에 성공해 출처별로 응답자가 갈린다. dev 모드(`bootRun`)와 host-gateway 스크레이프가 이 문제를 그대로 받는다. operator 결정 필요.

**(d) CI 캐시 구멍 (본 플랜 신규, HIGH).** §Issues 1. Phase 2 의 첫 CI 증설 시 함께 처리하는 것이 자연스럽다.

## P1 델타 이월 목록

P1 은 골격만 만들었고 아래는 **의도적으로 넣지 않은** 의존성·기능이다. 각 소유 phase 가 추가한다.

| 항목 | 소유 | 비고 |
|---|---|---|
| `spring-modulith-starter-jpa` + `event_publication` V 마이그레이션 | **P3** | D-14 소유권은 ADR-0002 에 기록됨. Modulith 이벤트 영속화의 전제 |
| `spring-boot-starter-security` + `spring-session-jdbc` | **P3** | STACK §2.6 (HttpOnly 세션 쿠키 + 동일 오리진). JWT localStorage 금지 규율 동반 |
| `springdoc-openapi-starter-webmvc-ui:2.3.0` | **P3~P5** | 2.3.0 고정(Boot 3.2 대응). 3.x 는 Boot 4 전용이라 금지 |
| `title` FULLTEXT 인덱스 (`WITH PARSER ngram`) | **P7** | SRCH-02 규율 — ngram vs LIKE 실측 전 인덱스 선점 금지. 대상 컬럼(`title_ko`/`title_en`/`synopsis`)은 P1 에 이미 존재해 재작업 없음 |
| FE 빌드 CI 게이트 | **P2 증설 후보** | D-09 가 P1 CI 를 코어 게이트로 한정 |
| 테스트 개수 하한 단언 + 캐시 배제 | **P2 증설 후보** | §Issues 1·2 |

## `/kha-add-phase` 필요

**D-00 의 후반 증설 phase 가 아직 ROADMAP 에 없다.** D-00 은 대용량 트래픽 요소를 "진화 서사"로 뒤에 붙이기로 확정했다 — **Phase 9~ : 부하 주입 → 병목 실측 → Redis 캐시/세션 → DB 읽기 복제본 → 모듈 1개 물리 분리**. 현재 ROADMAP 은 Phase 8 까지만 있다. Phase 1 이 이것을 선반영하지 않은 것은 의도대로다(compose 에 Redis/replica 없음, Modulith 경계 검증 자체가 분리-대비 준비물). CONTEXT D-00 이 지정한 반영 시점은 "본 워크플로 종료 후"이므로 **지금이 그 시점**이다.

## 잔여 리스크 (정량, 비-제로)

- **알려진 결함 N=4**: (1) 2회차 이후 CI 런의 `:test` 캐시 서빙 = 초록의 실행 증거력 상실(HIGH, 실측 확정), (2) 테스트 개수 단언 부재, (3) OPEN guard 가 현 핀에서 발화 불가(전방 게이트, 의도적), (4) ARCH-05 의 문서대로 한 커맨드 기동이 이 머신에서 미충족(환경 조건).
- **자동 회귀 커버**: CI 게이트 8종이 push 시점에 실행됨을 **1/1 런에서 관측**. 게이트 판별력은 로컬 반사실 6/6 arm(01-06) + 로컬 프로브 2건(01-07)으로 실측했으나, **CI 층에서의 참 양성은 0회 관측**이다 — 거짓 음성이 없다는 것만 안다.
- **어떤 게이트도 잡지 않는 것**: 호스트 포트 점유, EOL 도래, 신규 CVE 등장, ASCII-safe 파일명 정책, UTF-8 9지점, nginx 캐시 헤더, 그리고 **워크플로 파일 자체의 삭제/약화**(이건 공개 diff 리뷰가 유일한 방어다).
- **LICENSE 파일 부재** — 이 플랜의 명세에 없어 추가하지 않았다(플랜 우선). 공개 포트폴리오 저장소에 라이선스가 없으면 기본적으로 "all rights reserved"로 해석되고 리뷰어 관점에서 감점 요인이다. README 는 이미 상표 회피·CC-BY attribution 을 다루므로 저작권 리스크는 낮으나, **저장소 라이선스 선택은 사용자 결정 사항**이라 임의로 고르지 않았다. operator 항목으로 남긴다.

## Next Phase Readiness

**Phase 1 완료 — 8/8 플랜.** 가드레일이 첫 push 이전에 존재한 상태로 공개 저장소가 만들어졌고, 그 가드레일이 CI 에서 실제로 실행됨을 관측했다.

Phase 2 착수 전 필요한 것은 위 §Phase 2 착수 조건 (a)~(d) 4건이며, 그중 (a) ffmpeg 설치와 (b) HLS 스펙 원문 확보는 **사람이 해야 하는 선행 작업**이다. (c)(d)는 operator 결정이다.

## Self-Check: PASSED

- 산출물 2/2 디스크 존재: `01-CI-GATE-LOG.md`, 본 SUMMARY
- 태스크 커밋 2/2 이력 존재: `1601c29` · `c2f3aec` (Task 1 은 워킹트리 변경 0이라 커밋 없음 — 의도적, §Task Commits 에 명기)
- 줄 수 주장 대조: `git diff --shortstat d45e800..HEAD` = `1 file changed, 213 insertions(+)` ↔ 본문 213줄 일치
- 원격 상태 12/12 API 확인 (§검증 실측 Task 1 표)
- Run URL 2종 실재: 31077663272(첫 런) · 31078140031(2회차)
- 요구사항 2종 체크박스 + traceability 표 양쪽 반영 확인 (`ARCH-01`/`ARCH-05` → `[x]` / `Complete`)
- 작업 트리 clean

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
