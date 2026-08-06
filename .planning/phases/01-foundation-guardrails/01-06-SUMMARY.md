---
phase: 01-foundation-guardrails
plan: 06
subsystem: infra
tags: [adr, github-actions, dependabot, ci-gate, eol-policy, cve-visibility, media-guard, encoding, counterfactual-probe, vacuous-pass]

# Dependency graph
requires:
  - phase: 01-01
    provides: ".gitignore(경로 스코프·CP-1 준수) + .gitattributes(eol=lf) + .editorconfig(utf-8) + .githooks/pre-commit — CI 게이트가 검사하는 피검사체"
  - phase: 01-02
    provides: "backend/build.gradle(resilience4j 2.2.0 force + UTF-8) + 8모듈 package-info + ModularityTests — dependencyInsight·모듈수 게이트의 대상"
  - phase: 01-04
    provides: "SchemaMigrationTest.withCommand — mysql 기동 파라미터 3종의 정본(대조 게이트의 한쪽 축)"
  - phase: 01-05
    provides: "compose.yaml(mysql command 3종·profiles 이원화) + 수동 diff 대조 커맨드 — 이번 플랜이 자동 게이트로 승격"
provides:
  - "docs/adr/0001-runtime-versions.md — G1: 버전 핀 표(코드 대조) + Boot 3.2/MySQL 8.0 EOL 인지·수용 + non-blocking CVE 정책 근거 + resilience4j 3중+1 방어 + SC1 정직 재서술 + accept 위협 4종 + D-00 진화 서사"
  - "docs/adr/0002-media-and-encoding.md — G4: 미디어 4중 방어(층별 실제 강도) + Windows ASCII-safe 경로 정책 + UTF-8 명시 9지점 표 + D-14 event_publication 소유권 + P1 의존성 델타 4종"
  - ".github/workflows/ci.yml — D-09 최소 코어 게이트 7종(미디어 히스토리·gitignore 회귀·OPEN·모듈 8·mysql 핀 대조·build+verify·resilience4j·compose config)"
  - ".github/dependabot.yml — gradle/npm/github-actions 3 생태계 + Boot·Modulith major/minor·resilience4j 전량 ignore(CP-9), 자동 머지 0건"
  - "compose <-> SchemaMigrationTest 핀 대조의 자동 게이트화 (01-05 인계 항목 해소) + 개수 선-단언으로 vacuous PASS 구멍 차단"
affects: [01-07-verification, 01-08-repo-and-first-ci-run, 02-media-playback, 03-auth-events, 06-observability, 08-ingest]

# Tech tracking
tech-stack:
  added: ["actions/checkout@v7", "actions/setup-java@v5", "gradle/actions/setup-gradle@v6", "dependabot v2"]
  patterns:
    - "게이트는 라벨이 아니라 반사실 control arm 으로 판별력을 증명한 뒤 채택한다 (01-02/01-04/01-05 에 이은 4회차, 이번 6/6)"
    - "대조(diff) 게이트에는 개수 선-단언을 붙인다 — 양쪽이 함께 사라지면 diff 는 통과한다"
    - "로컬 빌드의 BUILD SUCCESSFUL 은 실행 증거가 아니다 — UP-TO-DATE/FROM-CACHE 를 배제한 뒤 읽는다"
    - "EOL 스택에서 CVE 는 차단하지 않고 보이게 둔다 — 상시 붉은 게이트는 CI 신호 전체를 죽인다"

key-files:
  created:
    - docs/adr/0001-runtime-versions.md
    - docs/adr/0002-media-and-encoding.md
    - .github/workflows/ci.yml
    - .github/dependabot.yml
  modified: []

key-decisions:
  - "compose<->SchemaMigrationTest 핀 대조를 CI 스텝으로 신설 (STATE.md [Plan 06, HIGH] 인계 해소) — 플랜 명세 6종에 1종 추가, D-09 금지 목록(FE 빌드·브라우저 E2E·커버리지 임계·배포)과 무충돌"
  - "인계받은 수동 diff 커맨드를 그대로 쓰지 않고 개수 선-단언을 덧붙임 — 반사실 ARM2 로 '양쪽 동시 삭제 시 조용한 통과' 를 실측했고, 그 구멍이 바로 01-04 가 경고한 '누락은 못 잡는다' 와 동형"
  - "next 를 Dependabot ignore 에 넣지 않음 — npm audit high 3건(postcss·sharp)의 유일 수정 경로가 next@16.3.0 이라 잠긴 핀 16.2.12 와 충돌하지만, 그 충돌을 보이게 하는 것이 G1 의 목적이다. 억제=가시성 0, 자동머지=핀 파손 → 사람 판단으로 남김"
  - "CI 의 OPEN guard 는 Modulith 1.1.12 에서 발화 불가한 전방 게이트임을 워크플로 주석에 정직 표기 — 01-02 실측(속성 부재)과 정합. 1.2+ 상향 시 자동으로 살아난다"
  - "ARCH-01/ARCH-05 mark-complete 미실행 (선례 6회째) — 워크플로 파일은 생겼으나 CI 가 단 한 번도 실행된 적이 없다. '게이트 도달성 확인' 은 ROADMAP 이 01-08 에 배정"

patterns-established:
  - "대조 게이트 = 값 일치 + 개수 하한. 일치만 보는 게이트는 공집합에서 통과한다"
  - "빌드 게이트를 로컬에서 재현할 때는 캐시 계층 2종(task up-to-date, build cache)을 명시적으로 배제하고 test-results XML 의 tests= 수를 읽는다"

requirements-completed: []
requirements-contributed: [ARCH-01, ARCH-05]

# Metrics
duration: 11min
completed: 2026-08-06
---

# Phase 1 Plan 6: ADR 2종 + CI 코어 게이트 + Dependabot Summary

**EOL 스택 선택 근거(ADR-0001)와 미디어·인코딩 운영 규약(ADR-0002)을 문서로 못박고, 그 규율을 push 시점에 강제하는 GitHub Actions 코어 게이트 7종 + 핀을 공격하지 않는 Dependabot 을 배선했다 — 게이트 6종은 반사실 control arm 으로 판별력을 실측했고, 그 과정에서 인계받은 수동 대조 커맨드의 vacuous PASS 구멍 1건을 발견해 막았다.**

## Performance

- **Duration:** 11 min
- **Started:** 2026-08-06T05:40:56Z
- **Completed:** 2026-08-06T05:52:10Z
- **Tasks:** 3 / 3
- **Files created:** 4 (452 insertions, `git diff --stat eeba1a4..HEAD` 실측)

## Accomplishments

- **G1 착지** — ADR-0001 이 "왜 EOL 스택인가"에 답한다: Boot 3.2 OSS 지원 종료 2024-12-31(최종 패치 3.2.12), MySQL 8.0 Extended Support 2026-04-30 종료를 명시하고, 완화 3종(인터넷 미노출·민감 데이터 0·포트폴리오 수명)과 승격 경로(Boot 3.3+ ↔ Modulith 1.2+ 동반 이동)를 못박았다.
- **non-blocking 의 근거를 메커니즘 수준에서 서술** — Dependabot 은 CI 상태에 영향이 없어 태생적으로 non-blocking 이고, Trivy/OWASP 는 `continue-on-error` 를 **잊으면 blocking 이 되는** 설계다. EOL 라인 위에 blocking 게이트를 얹으면 상시 붉어져 CI 신호 전체(미디어·경계 게이트 포함)가 같이 죽는다는 것이 채택 이유다.
- **G4 착지** — ADR-0002 가 미디어 4중 방어의 **층별 실제 강도**를 구분한다: 훅은 `--no-verify` 로 우회되는 defense-in-depth 이고, 실 boundary 는 CI 의 히스토리 전체 스캔이다. Windows ASCII-safe 경로 정책과 UTF-8 명시 9지점(각각 실제 파일 위치 명기), D-14 `event_publication` 소유권 규율도 여기 들어갔다.
- **D-09 코어 게이트 7종 배선** — 미디어 히스토리 · gitignore 회귀(CP-1) · OPEN guard · 모듈 8개 선언 · **mysql 기동 파라미터 대조(신설)** · build+`verify()`+테스트 · resilience4j 핀 · compose config. D-09 가 열거한 금지 항목(FE 빌드·브라우저 E2E·커버리지 임계·배포)의 리터럴 출현 수는 **실측 0** (`grep -ci` 카운트), 잡(job) 수는 1개(`core`)다. 신설한 1종의 정당화는 §Deviations 1 에 별도로 적었다 — "금지 목록에 없다"는 것만으로 추가를 정당화하지 않았고, STATE.md 가 HIGH 로 인계한 미해소 항목이라는 근거를 함께 든다.
- **01-05 인계 항목 해소** — STATE.md 가 `[Plan 06, HIGH]` 로 남긴 "compose↔SchemaMigrationTest 핀 대조 자동 게이트 부재"를 CI 스텝으로 승격했다.
- **반사실 control arm 6종 전부 기대대로** — 게이트의 존재가 아니라 작동을 실측했다(아래 표).
- **RESEARCH 가정 A7 해소** — `dependencyInsight` 출력에 `Selection reasons: - Forced` 와 함께 2.2.0 이 표시돼, `resolutionStrategy.force` 가 G2 게이트의 grep 패턴과 충돌하지 않음을 실측 확인했다.

## Task Commits

1. **Task 1: ADR-0001 + ADR-0002** — `62ed93b` (docs)
2. **Task 2: CI 워크플로 (코어 게이트 7종)** — `fb0b8a7` (ci)
3. **Task 3: Dependabot (ignore + 자동머지 0)** — `b512486` (ci)

## Files Created

- `docs/adr/0001-runtime-versions.md` (163줄) — 버전 핀 표(build.gradle·compose.yaml·wrapper 실값 대조) / EOL 인지·수용 / non-blocking CVE 정책 / resilience4j 3중+1 방어 / SC1 정직 재서술 / 빌더 다이제스트 자리 / accept 위협 4종 / D-00 진화 서사
- `docs/adr/0002-media-and-encoding.md` (151줄) — 미디어 4중 방어 층별 강도 / 전역 `*.ts` 금지 근거 / 5MB 임계 근거 / ASCII-safe 경로 정책 / UTF-8 9지점 / D-14 소유권 / P1 의존성 델타 4종 / nginx 운영 규약
- `.github/workflows/ci.yml` (100줄) — `fetch-depth: 0` + `permissions: contents: read` + 코어 게이트 7종
- `.github/dependabot.yml` (38줄) — 3 생태계 + ignore 3규칙 + 자동 머지 0건

## 검증 실측

### Acceptance (플랜 명시 criterion 전수)

| 태스크 | 검사 | 결과 |
|---|---|---|
| Task 1 | ADR grep 24종 (`3.2.12`·`2024-12-31`·`2026-04-30`·`1.1.12`·`8.14.5`·`non-blocking`·`2.2.0`/`2.3.0`·`builder-jammy-base`·`pull_policy`·`Modulith`+`redis` / `schema-initialization`·`starter-jpa`·`ASCII`·`utf8mb4`·`sun.jnu.encoding`·`D:/moabom-media`·`starter-security`·`boundary`) | **24/24 PASS** |
| Task 1 | 문서-코드 정합 (ADR `2.2.0` ↔ `build.gradle` `resilience4j-spring-boot3:2.2.0`) | 양쪽 비-공집합 PASS |
| Task 2 | ci.yml 검사 15종 (YAML 파싱 · 액션 v7/v5/v6 · `fetch-depth: 0` · `permissions`+`contents: read` · 게이트 문자열 6종 · `branches: [main]` · D-09 범위 0건) | **15/15 PASS** |
| Task 3 | dependabot.yml 검사 12종 (YAML 파싱 · `package-ecosystem` 정확히 3 · directory 2종 · ignore 3규칙 · 자동머지 0 · 워크플로 0 · ADR 링크) | **12/12 PASS** |
| 플랜 `<verify>` | `ADR_OK` / `CI_WORKFLOW_OK` / `DEPENDABOT_OK` | 3/3 rc=0 |

### CI 게이트 로컬 재현 (green arm — 실제 스텝 스크립트를 그대로 실행)

`media guard` OK · `gitignore sanity` OK(3/3) · `OPEN guard` OK(빈 출력) · `module coverage` OK(n=8, m=8) · `mysql 핀 대조` OK(3/3 문자 일치) · `./gradlew build` **BUILD SUCCESSFUL** · `dependencyInsight` 2.2.0 존재 + 2.3.x 부재 · `docker compose --profile full config -q` rc=0.

테스트는 실행을 강제해 실측했다 — **10/10 통과** (`ModularityTests` tests=4 failures=0, `SchemaMigrationTest` tests=6 failures=0, test-results XML 직독).

### 반사실 control arm 6종 (게이트가 실제로 판별하는가)

| Arm | 주입한 결함 | 기대 | 실측 |
|---|---|---|---|
| 1 | compose 만 `ngram_token_size` 2→3 (한쪽 드리프트) | 차단 | ✅ rc=1 |
| 2 | 파라미터 3종을 **양쪽에서 동시 삭제** | 차단 | ✅ rc=1 — 단, **구 diff-only 판정은 통과했다**(아래 발견 1) |
| 3 | 모듈 package-info 를 8→6 | 차단 | ✅ rc=1 (실측 6) |
| 4 | shared 아닌 catalog 에 `Type.OPEN` | 차단 | ✅ rc=1 (해당 파일 적발) |
| 5 | `.gitignore` 에 전역 `*.ts` (CP-1 회귀 재현) | 차단 | ✅ rc=1 |
| 5b | 경로 스코프 형태(현 저장소) | 통과 | ✅ rc=0 (오탐 없음) |
| 6 | 6MB blob 커밋 후 **워킹트리에서 삭제** | 차단 | ✅ rc=1 (히스토리에서 적발) |
| 6b | 같은 저장소를 `--depth 1` 얕은 클론 후 동일 스캔 | — | ⚠️ **rc=0 통과 (아무것도 못 봄)** — `fetch-depth: 0` 필요성의 실측 근거 |

Arm 6b 는 종전까지 산문 주장이던 "`fetch-depth: 0` 없으면 게이트가 조용한 no-op 이 된다"를 처음으로 실측한 것이다.

## Decisions Made

1. **핀 대조 스텝을 CI 에 신설** — 01-05 가 수동 diff 로 일치를 확인하고 자동화를 이 플랜에 인계했다. D-09 가 금지한 범위(FE 빌드·브라우저 E2E·커버리지 임계·배포)와 무관한 가드레일 정합성 게이트라 범위 확대가 아니라고 판단했다.
2. **인계 커맨드를 그대로 쓰지 않고 강화** — 발견 1 참조.
3. **`next` 를 ignore 하지 않는다** — 억제하면 G1 이 요구한 가시성이 0 이 되고, 자동 머지하면 핀이 깨진다. 충돌을 보이게 두고 사람이 판단하는 것이 정책의 정의다. 근거를 `dependabot.yml` 주석과 ADR-0001 §3 양쪽에 남겼다.
4. **OPEN guard 의 현재 무발화를 숨기지 않는다** — Modulith 1.1.12 에는 `type` 속성이 없다(01-02 javap 실측). 게이트를 지우지 않고 "오늘 발화 불가한 전방 게이트"로 주석에 명기했다 — 1.2+ 상향이 이 게이트를 자동으로 활성화한다.
5. **ARCH-01/ARCH-05 mark-complete 미실행** — 아래 §요구사항 참조.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing Critical] compose↔SchemaMigrationTest mysql 파라미터 대조 게이트 신설 + vacuous PASS 구멍 차단**
- **Found during:** Task 2
- **Issue:** STATE.md 가 `[Plan 06, HIGH]` 로 인계한 항목 — 지금은 양쪽이 일치하지만 **한쪽만 고치면 아무도 못 잡는다**. 인계받은 커맨드는 두 grep 결과의 `diff` 였는데, 반사실 ARM2 로 실행해 보니 **양쪽에서 파라미터를 함께 지우면 공집합끼리 일치해 조용히 통과**했다. 이는 01-04 가 이미 경고한 "그 테스트는 값 드리프트는 잡지만 누락은 못 잡는다"와 **동형 결함이 대조 게이트 층에서 재발**한 것이다.
- **Fix:** CI 스텝 `mysql 기동 파라미터 대조 (G3 / CP-10)` 를 신설하고, 값 일치 판정 **앞에** 개수 선-단언(`compose 쪽 3종`)을 배치했다. 프로세스 치환 대신 변수 비교를 써서 러너 셸 종류에 의존하지 않게 했다.
- **Files modified:** `.github/workflows/ci.yml`
- **Verification:** green arm 3/3 문자 일치 / ARM1(한쪽 드리프트) rc=1 / ARM2(양쪽 삭제) rc=1 — 구 판정에서는 통과하던 케이스
- **Committed in:** `fb0b8a7`

**2. [Rule 2 - Missing Critical] OPEN guard 의 현재 무발화 사실을 워크플로에 정직 표기**
- **Found during:** Task 2
- **Issue:** 플랜의 ci.yml 명세는 OPEN guard 를 살아 있는 게이트처럼 서술하나, 01-02 가 실측했듯 잠긴 핀 Modulith 1.1.12 의 `@ApplicationModule` 에는 그 속성이 없다 — 오늘은 어떤 커밋도 이 게이트를 발화시킬 수 없다. 주석 없이 두면 미래의 독자가 "경계가 지켜지고 있다"를 이 게이트의 green 에서 잘못 읽는다.
- **Fix:** 게이트를 유지하되(1.2+ 상향 시 자동 활성) 주석에 발화 불가 사실과 그 근거(javap 실측)를 명기.
- **Files modified:** `.github/workflows/ci.yml`
- **Verification:** acceptance 의 `Type.OPEN` 리터럴 grep 은 그대로 PASS / ARM4 로 게이트 로직 자체의 판별력은 별도 확인
- **Committed in:** `fb0b8a7`

---

**Total deviations:** 2 auto-fixed (2× Rule 2 — 누락된 필수 기능/정직 표기)
**Impact on plan:** 둘 다 플랜 acceptance 를 하나도 깨지 않았고(전수 재실행으로 확인), 산출물 4종·D-09 범위 제약도 그대로다. 스코프 크리프 없음.

### 플래너 자기모순 — 이번 플랜 0건 (5회 연속 기록 중단)

01-01~01-05 에서 5회 발생한 "`<action>` 산문/주석 리터럴이 자기 grep acceptance 를 깨는" 패턴을 전수 대조했다:
- Task 2 의 `grep -ci 'playwright\|npm run build\|jacocoTestCoverageVerification\|deploy'` = 0 요구 ↔ 플랜의 ci.yml **yaml 블록**에 해당 리터럴 0건 → 무모순.
- Task 3 의 `grep -ci 'auto-merge\|automerge'` = 0 요구 ↔ 플랜의 dependabot.yml **yaml 블록**에 0건 → 무모순.

⚠️ 다만 **근접 실패(near-miss)** 다: 두 리터럴 모두 플랜의 `<action>` **산문**(주의 사항 / 금지 절)에는 존재한다. 산문을 산출물 주석으로 옮겨 적는 순간 acceptance 가 깨진다. 이번엔 금지 항목을 한국어로만 표기해 회피했다. 구조적 해법(플랜 생성 단계 정적 게이트)의 필요성은 유지된다.

## Issues Encountered

**1. 로컬 `./gradlew build` 의 이중 vacuous PASS — BUILD SUCCESSFUL 인데 테스트가 한 번도 안 돌았다**

CI 의 build 스텝을 로컬에서 재현하려고 `./gradlew build --no-daemon` 을 돌리자 11초 만에 `BUILD SUCCESSFUL`, 전 태스크 `UP-TO-DATE`. `cleanTest test` 로 강제하자 이번엔 `:test FROM-CACHE`(`org.gradle.caching=true`). **두 번의 초록 모두 테스트를 한 줄도 실행하지 않았다.** `cleanTest test --no-build-cache` 로 40초, 그제서야 Testcontainers 가 뜨고 10/10 이 실제로 실행됐다.

- **해소:** 캐시 계층 2종을 명시 배제한 실행으로 재검증 + test-results XML 의 `tests=`/`failures=` 직독으로 확인.
- **CI 로의 함의 (미해소, 아래 블로커로 이월):** `gradle/actions/setup-gradle` 은 Gradle user home 을 캐시/복원한다. 로컬 build cache 디렉터리가 그 범위에 포함되면 **2회차 이후 CI 런도 `:test` 를 FROM-CACHE 로 서빙할 수 있다.** 첫 런은 캐시가 비어 있어 반드시 실행되므로 "ubuntu 첫 런이 Flyway 한글 인코딩의 실검증 지점"(01-04 인계)이라는 전제는 유지되지만, **그 초록을 체크마크가 아니라 로그로 확인**해야 한다.

**2. Git Bash 로컬 검증의 MSYS 경로 변환** — 01-05 인계 사항대로 `docker compose` 계열 호출에 `MSYS_NO_PATHCONV=1` 를 붙였다. ubuntu 러너에는 무영향이므로 ci.yml 에는 반영하지 않았다.

## Known Stubs

| 항목 | 파일 | 성격 | 해소 주체 |
|---|---|---|---|
| `first successful build digest: (Plan 07 실기동 태스크가 기록)` | `docs/adr/0001-runtime-versions.md` §6 | **의도된 자리표시자** — 플랜 Task 1 요구사항 8번이 명시적으로 지시한 형태. 첫 성공 빌드가 없으므로 채울 값이 존재하지 않는다(RESEARCH 가정 A5 미해소). 해당 절에 "이 시점에는 성공 빌드가 없다"를 명기했다 | **Plan 07** (full 프로필 실기동) |

이 자리표시자 외에 하드코딩 빈 값·"준비 중" 문구·미배선 컴포넌트는 없다.

## 요구사항 상태 (mark-complete 미실행 — 선례 6회째)

| 요구사항 | 이번 기여 | 왜 아직 체크하지 않는가 |
|---|---|---|
| **ARCH-01** | `verify()` 를 돌리는 CI 게이트 파일이 처음으로 존재하고, OPEN·모듈수 보조 게이트까지 갖췄다 | 요구사항 문구의 "CI 게이트"는 **CI 가 실행돼야** 성립한다. 이 저장소에는 remote 가 없고 워크플로가 **단 한 번도 실행된 적이 없다**. ROADMAP 이 "CI 첫 런 게이트 도달성 확인"을 **01-08** 에 배정했다 |
| **ARCH-05** | `docker compose --profile full config -q` 를 저비용 프록시로 CI 에 고정 | `config -q` 는 파싱 검증이지 기동 검증이 아니다. "한 번으로 전 스택 기동"의 실기동은 **01-07** 소관 (선례 5회 유지) |

## Next Phase Readiness

**Plan 07 (반사실 프로브 + full 프로필 실기동) 준비 완료**
- V-SC2d/V-SC4d 프로브가 재사용할 게이트 스크립트가 ci.yml 에 문자 그대로 있다. 이번 플랜의 arm 4·6 이 그 프로브의 축소판이므로 방법론은 검증됐다.
- ADR-0001 §6 의 빌더 다이제스트 자리가 Plan 07 의 산출물을 기다린다 (가정 A5 해소 지점).

**Plan 08 (main 전환 + public repo + CI 첫 런)**
- 워크플로가 `push: branches: [main]` 트리거다 — 현 브랜치는 `master` 이므로 **전환 전에는 어떤 push 도 CI 를 깨우지 않는다**.
- 첫 런에서 확인할 것: (a) `:test` 가 FROM-CACHE 가 아니라 실제 실행되는지, (b) `koreanTitlesRoundTrip` 이 ubuntu 에서 통과하는지(붉으면 `spring.flyway.encoding: UTF-8` 추가로 대응, 시드 ASCII 화는 금지), (c) 8개 스텝 전부가 skip 없이 도달하는지.

**잔여 리스크 (비-제로, 정직 표기)**
- 알려진 결함 **N=3**: (1) CI 가 한 번도 실행되지 않아 워크플로의 실 도달성이 미검증, (2) 2회차 이후 CI 런에서 `:test` 가 캐시로 서빙될 가능성 미실측, (3) 빌더 다이제스트 미기록(A5).
- 자동 회귀 커버: 이번 플랜이 만든 8스텝은 **로컬 재현으로 8/8 green**, 판별력은 **6/6 arm 으로 실측**. 그러나 이 전부가 **push 시점에 실제로 도는지는 아직 0회 관측**이다 — 게이트의 존재는 게이트의 작동이 아니다.
- ADR 이 규정한 것 중 **자동 게이트가 없는 항목**: ASCII-safe 파일명 정책, UTF-8 9지점, nginx 캐시 헤더. 사람이 리뷰에서 본다(ADR-0002 말미에 명기).

## Self-Check: PASSED

- 파일 5/5 존재 (ADR 2종 · ci.yml · dependabot.yml · 본 SUMMARY)
- 커밋 3/3 존재 (`62ed93b` · `fb0b8a7` · `b512486`)
- 줄 수 주장 4/4 `git show --numstat` 과 일치 (163 / 151 / 100 / 38)
- 총계 주장 452 insertions ↔ `git diff --shortstat eeba1a4..HEAD` = `4 files changed, 452 insertions(+)` 일치
- 작업 트리 clean

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
