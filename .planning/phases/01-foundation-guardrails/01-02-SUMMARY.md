---
phase: 01-foundation-guardrails
plan: 02
subsystem: backend
tags: [gradle, spring-boot, spring-modulith, named-interface, allowed-dependencies, resilience4j, flyway, actuator, tdd, utf-8]

# Dependency graph
requires: []
provides:
  - "backend/ Gradle 8.14.5 wrapper (exec bit 100755 인덱스 고정) + Boot 3.2.12 / Modulith 1.1.12 프로젝트"
  - "G2 피검사체: resilience4j-spring-boot3:2.2.0 이 runtimeClasspath 에 실재 + resolutionStrategy.force 로 강제"
  - "G4: JavaCompile UTF-8 + Test file.encoding/sun.jnu.encoding UTF-8"
  - "8모듈 Modulith 골격 (bff/catalog/playback/profile/recommendation/experiment/ingest/shared) + allowedDependencies 8종 선언"
  - "named interface 5종: catalog/playback/profile/experiment :: spi + ingest :: event"
  - "포트 계약 5종(TitleQueryPort/WatchHistoryPort/ProfilePort/AssignmentPort/RowProvider) + 루트 API 마커 6종"
  - "ModularityTests 4종 — verify()/8모듈/Documenter/경계 명시선언 (커밋 단위 quick gate, Docker 불요)"
  - "GET /api/health (bff) — D-02 단일 진입 오리진의 소비 대상"
  - "application.yaml: Actuator 화이트리스트 · ddl-auto=validate · modulith schema-init=false · ../compose.yaml start-only"
affects: [01-03-compose, 01-04-flyway, 01-05-env-example, 01-06-ci, 01-07-verification, 01-08-github-repo]

# Tech tracking
tech-stack:
  added:
    - "Gradle 8.14.5 (wrapper)"
    - "Spring Boot 3.2.12"
    - "Spring Modulith 1.1.12 (starter-core + actuator + starter-test)"
    - "io.github.resilience4j:resilience4j-spring-boot3:2.2.0 (force)"
    - "Flyway core + flyway-mysql, mysql-connector-j, micrometer-registry-prometheus"
    - "JaCoCo 0.8.12, Lombok, Testcontainers(mysql/junit-jupiter), spring-boot-docker-compose"
  patterns:
    - "선언 순서: @NamedInterface + 포트를 먼저, allowedDependencies 를 나중에 (CP-6 회피)"
    - "P1 포트 시그니처는 원시 타입 — shared 를 import 하는 순간 소비자가 allowedDependencies 에 \"shared\" 를 명시해야 한다"
    - "모듈 루트 API(bff 용) vs spi 포트(도메인 모듈 용) 분리 (ARCHITECTURE §2.1)"
    - "게이트는 반사실로 증명한다 — 일부러 위반을 넣어 붉어지는지 확인 후 되돌림"

key-files:
  created:
    - backend/settings.gradle
    - backend/gradle.properties
    - backend/build.gradle
    - backend/gradlew
    - backend/gradlew.bat
    - backend/gradle/wrapper/gradle-wrapper.properties
    - backend/gradle/wrapper/gradle-wrapper.jar
    - backend/src/main/java/com/moabom/MoabomApplication.java
    - backend/src/main/resources/application.yaml
    - backend/src/main/java/com/moabom/shared/{package-info,TitleId,ProfileId}.java
    - backend/src/main/java/com/moabom/{catalog,playback,profile,experiment}/spi/{package-info,*Port}.java
    - backend/src/main/java/com/moabom/ingest/event/{package-info,TitleAssetPublished}.java
    - backend/src/main/java/com/moabom/recommendation/row/RowProvider.java
    - backend/src/main/java/com/moabom/bff/HealthController.java
    - backend/src/main/java/com/moabom/{bff,catalog,playback,profile,recommendation,experiment,ingest}/package-info.java
    - backend/src/main/java/com/moabom/{catalog,playback,profile,recommendation,experiment,ingest}/*Api.java
    - backend/src/test/java/com/moabom/ModularityTests.java
  modified: []

key-decisions:
  - "shared 를 open module 로 선언하지 못함 — Modulith 1.1.12 에 해당 속성이 없다(실측 javap). 버전 상향 대신 allowedDependencies={} 일반 모듈로 착지 = 경계를 넓히지 않는 방향"
  - "ModularityTests 에 4번째 테스트(경계 명시선언 검사) 추가 — 플랜 명세 3종은 구현 이전에도 전부 green 이라 Task 3 를 판별하지 못했다"
  - "반사실 프로브 1회 실행 후 되돌림 — catalog → playback::spi 침범 시 verify() 가 Violations 로 실제 붉어짐을 확인"
  - "ARCH-01 mark-complete 미실행 — 요구사항 문구의 'CI 게이트' 절반이 Plan 06 소관이고 .github/workflows 는 아직 없다"

patterns-established:
  - "grep 기반 acceptance 는 산문으로도 통과한다 — 게이트를 통과시키려고 주석에 토큰을 넣지 않는다"
  - "TDD RED 가 실패하지 않으면 그 사실 자체를 보고하고, 판별하는 테스트를 찾는다 (RED 를 조작하지 않는다)"

requirements-completed: []
requirements-contributed: [ARCH-01]

# Metrics
duration: 21 min
completed: 2026-08-06
---

# Phase 1 Plan 02: Modulith 8모듈 골격 + Gradle 스캐폴딩 Summary

**손으로 놓은 Boot 3.2.12/Modulith 1.1.12 Gradle 8.14.5 프로젝트 위에 8모듈 경계를 첫 커밋부터 선언하고, `ApplicationModules.verify()` 를 반사실 프로브로 "실제로 붉어지는" 게이트임을 확인한 상태로 green 착지**

## Performance

- **Duration:** 약 21분 (13:04Z+9 직후 착수 ~ 13:25+9)
- **Tasks:** 3/3 (Task 3 은 TDD — RED/GREEN 분리 커밋)
- **Files created:** 30 (main java 29 + test java 1) + 빌드 파일 7
- **Commits:** 4 (task 커밋 3 + TDD RED 1)

## Accomplishments

- **Gradle 8.14.5 wrapper 를 시스템 gradle 9.4.1 로 생성하고 `./gradlew` 만으로 빌드 성립.** `--version` 이 8.14.5, `build` 가 exit 0. `gradlew` 실행 비트를 `git update-index --chmod=+x` 로 인덱스에 100755 로 박아 CP-5(ubuntu 러너 `Permission denied`)를 선제 차단했다.
- **G2 피검사체가 클래스패스에 실재한다.** `dependencyInsight --configuration runtimeClasspath` 가 `2.2.0` + `Selection reasons: Forced` 를 출력하고 `2.3.` 은 0건. Plan 06 의 회귀 게이트가 이제 **측정할 대상이 있는** 상태다 — 게이트만 있고 피검사체가 없는 공회전을 피했다.
- **P1 의존성 델타 4종 제외 확인.** `spring-modulith-starter-jpa`(CP-4 · D-14 충돌) / `spring-boot-starter-security`(CP-7 · 401) / `spring-session` / `springdoc` 전부 0건. `flyway-mysql` 은 포함(MySQL 8 `Unsupported Database` 방지).
- **8모듈 전부가 자기 경계를 명시 선언한다.** ARCHITECTURE §8.2 DAG 를 그대로 이식 — `catalog/profile/experiment/ingest = {}`, `playback = {catalog::spi, profile::spi}`, `recommendation = {4종 ::spi}`, `bff = {5개 도메인 루트}`, `shared = {}`. RESEARCH P-3 의 선언 순서(named interface 먼저, allowedDependencies 나중)를 지켜 CP-6 참조 해석 실패를 만들지 않았다.
- **`verify()` 가 라벨이 아니라 게이트임을 실측했다.** `catalog` 에 `playback.spi` 침범 파일(`ProbeViolationTemp.java`)을 넣자 `verifiesModularStructure` 가 `org.springframework.modulith.core.Violations` 로 FAIL(`tests="4" failures="1"`) → `rm` 후 4/4 green 복구, `git status --porcelain` 빈 출력로 잔재 0 확인. 프로브는 어떤 커밋에도 포함되지 않았다. RESEARCH 가 요구한 "반사실 프로브" 를 이 플랜 범위에서 값싸게 1회 수행한 것이며, **영구 프로브는 플랜 지시대로 Plan 07 Task 1 에 그대로 남겨 두었다.**
- **Documenter 산출물이 실재한다.** `build/spring-modulith-docs/` 에 `components.puml` + 모듈 8종의 `.puml`/`.adoc` (총 17개). `spring-modulith-starter-test` 가 docs 아티팩트를 전이 제공해 플랜이 예비해 둔 `spring-modulith-docs` 추가 의존성은 불필요했다.

## Task Commits

1. **Task 1: Gradle 스캐폴딩 + build.gradle(G2/G4) + application.yaml** — `30c4392` (chore) — acceptance 18/18
2. **Task 2: named interface + 포트 계약 + shared + bff 헬스** — `4401f11` (feat) — acceptance 9/10 (1건 충족 불가, 아래 Deviation 2)
3. **Task 3 RED: ModularityTests (실패하는 4번째 테스트 포함)** — `5706cb4` (test)
4. **Task 3 GREEN: @ApplicationModule 8종 + 루트 API 마커 6종** — `f61d710` (feat) — acceptance 9/9

## Files Created/Modified

- `backend/build.gradle` — Boot 3.2.12 / dependency-management 1.1.7 / jacoco, toolchain 17, Modulith BOM 1.1.12, P1 의존성 델타, `resolutionStrategy.force`, UTF-8 인코딩 2종
- `backend/gradle.properties` · `settings.gradle` · `gradlew`(100755) · `gradlew.bat` · `gradle/wrapper/*`
- `backend/src/main/resources/application.yaml` — Actuator 화이트리스트(`health,info,prometheus`), `ddl-auto: validate`, `flyway.locations`, `modulith.events.jdbc.schema-initialization.enabled: false`, `docker.compose.file: ../compose.yaml` + `start-only`
- `backend/src/main/java/com/moabom/` — `MoabomApplication` + 8모듈 `package-info` + spi 4종 + `ingest/event` + 포트 5종 + 루트 API 마커 6종 + `shared/{TitleId,ProfileId}` + `bff/HealthController`
- `backend/src/test/java/com/moabom/ModularityTests.java` — 테스트 4종

## Decisions Made

1. **`shared` 는 open module 이 아니라 `allowedDependencies = {}` 일반 모듈** — 아래 Deviation 1. 잠긴 버전 핀을 지키면서 경계를 **넓히지 않는** 방향으로 착지.
2. **`ModularityTests` 에 4번째 테스트 추가** — 아래 Deviation 3. 플랜 명세 3종이 Task 3 구현 이전에도 전부 green 이었다.
3. **반사실 프로브를 이 플랜에서 1회 실행하고 되돌림** — 영구 프로브(Plan 07)를 선점하지 않되, "verify() green" 이 vacuous 가 아님을 지금 증명. 프로브 파일은 커밋되지 않았다(`git status` clean 확인).
4. **`requirements mark-complete ARCH-01` 미실행** — 아래 Deviation 5.
5. **`spring-modulith-docs` 의존성 미추가** — 플랜이 예비한 대응이었으나 `starter-test` 가 전이 제공해 불필요. `writesDocumentation` 삭제 회피 경로는 쓰지 않았다.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] `Type.OPEN` 이 잠긴 Modulith 1.1.12 에 존재하지 않는다**

- **Found during:** Task 2 (`shared/package-info.java` 컴파일)
- **Issue:** 플랜/ARCHITECTURE §2/RESEARCH C-3 이 지시한 `@ApplicationModule(type = ApplicationModule.Type.OPEN)` 이 컴파일 실패했다 — `symbol: method type()` / `symbol: variable Type`. **실측(javap, `spring-modulith-api-1.1.12.jar`): 선언된 멤버는 `displayName()` 과 `allowedDependencies()` 둘뿐이고 중첩 `Type` 열거형이 없다.** (`OPEN_TOKEN` 상수는 존재하나 값이 `¯\_(ツ)_/¯` 로 `allowedDependencies` 미선언 sentinel 이지 open module 표기가 아니다.) open module 기능은 Modulith **1.2** 도입분이다.
- **Fix:** 버전 상향은 하지 않았다 — Boot 3.2.x ↔ Modulith 1.1.x 는 공식 호환 매트릭스가 고정한 조합(STACK.md §7)이고 사용자 지시가 "버전 핀 절대 준수"다. 대신 **경계를 넓히지 않는 방향**으로: `shared` 를 `allowedDependencies = {}` 일반 모듈로 선언하고, shared 타입을 처음 import 하는 소비자가 자기 `allowedDependencies` 에 `"shared"` 를 명시하게 한다. 이는 플랜 Task 3 의 verify() 실패 대응 2번이 **이미 지시한 경로**이며 "open 으로 도망가지 말 것" 규율과 정합한다. 근거 전문을 `shared/package-info.java` 주석에 남겼다.
- **잔여 차이 (비-제로, 정직 표기):** (a) open 이었다면 `shared` 의 **하위 패키지**까지 열렸겠지만 shared 는 하위 패키지를 두지 않는다(ARCHITECTURE §2 — TitleId/ProfileId 만) → P1 실질 차이 0. (b) open 의 **사이클 탐지 제외** 특권도 불필요 — 나가는 의존이 0 이라 사이클에 참여할 수 없다. (c) 대가: shared 소비가 늘면 각 소비자에 `"shared"` 한 줄씩 추가해야 한다. **Phase 3+ 가 반드시 알아야 하는 항목이다.**
- **Files modified:** `backend/src/main/java/com/moabom/shared/package-info.java`
- **Committed in:** `4401f11`

**2. [정직 표기] Task 2 acceptance 1건이 충족 불가 — 통과시키지 않고 실패로 남김**

- **Issue:** Task 2 의 `grep -q 'Type.OPEN' backend/src/main/java/com/moabom/shared/package-info.java` 는 Deviation 1 때문에 **선언으로는 충족 불가**다.
- **초안이 실제로 이 게이트를 잘못 통과했다:** 근거 주석에 플랜 문구 `"Type.OPEN 으로 도망가지 말 것"` 을 인용했더니 grep 이 매칭돼 **PASS 가 떴다**. 이는 "선언이 실재한다"가 아니라 "그 문자열을 언급했다"를 측정한 것이다.
- **Fix:** 주석에서 해당 리터럴을 전부 제거해 criterion 이 **정직하게 실패**하도록 되돌렸다. 게이트를 통과시키려고 토큰을 심지 않는다.
- **영향 없음 확인:** 같은 토큰을 쓰는 **V-SC2b**(`shared 외 Type.OPEN == 0`), 플랜 `<verification>` 의 동일 grep, must_haves truth "shared 외 어떤 모듈도 Type.OPEN 이 아니다" 는 전부 통과한다(어떤 모듈도 아니므로). Plan 06 CI 의 "modulith OPEN guard" 스텝도 유효하게 남는다.
- **Files modified:** `backend/src/main/java/com/moabom/shared/package-info.java`
- **Committed in:** `4401f11`

**3. [Rule 2 - Missing Critical] 플랜 명세 테스트 3종이 Task 3 구현 이전에도 전부 green — RED 가 성립하지 않았다**

- **Found during:** Task 3 TDD RED
- **Issue:** `ModularityTests` 3종(verify / detectsEightModules / writesDocumentation)을 먼저 쓰고 실행하니 **`@ApplicationModule` 선언이 `shared` 하나뿐인 상태에서 4개 전부 통과**했다(`tests="3" failures="0"`, `build/spring-modulith-docs/` 에 8모듈 산출). Modulith 는 패키지 구조만으로 8모듈을 감지하고, 선언이 없는 모듈은 **제약이 없어** verify() 가 vacuous 하게 green 이다. 즉 플랜의 must_haves truth "8모듈 전부에 @ApplicationModule 선언이 있고" 는 **플랜 문서의 1회성 grep 말고는 어떤 실행 게이트도 없었다** — package-info 를 누가 지워도 아무도 붉어지지 않는다.
- **Fix:** RED 를 조작하지 않고(테스트를 억지로 실패시키지 않고) **판별하는 테스트**를 추가했다 — `everyModuleExplicitlyDeclaresItsBoundary`: 8모듈 각각의 `package-info` 에 `@ApplicationModule` 이 있고 `allowedDependencies` 가 기본 sentinel(`OPEN_TOKEN`)이 아님을 검사. 추가 직후 **ClassNotFoundException 으로 FAIL(진짜 RED)** → 구현 후 GREEN.
- **근거:** RESEARCH P-3 "경계는 명시적으로 선언해야만 존재한다" 를 실행 게이트로 승격. Plan 06 CI 가 `./gradlew build` 를 돌리므로 이 테스트는 영구 회귀 게이트가 된다.
- **Verification:** RED `tests="4" failures="1"` → GREEN `tests="4" failures="0"`.
- **Committed in:** RED `5706cb4` / GREEN `f61d710`

**4. [Rule 3 - Blocking] Task 1 acceptance 자기모순 — 플랜이 지시한 주석이 자기 criterion 을 실패시킴**

- **Found during:** Task 1
- **Issue:** 플랜 `<action>` 이 지시한 주석은 `// Boot 3.2 구성명 (3.4+ 의 testAndDevelopmentOnly 아님)` 인데, 같은 태스크의 acceptance 는 `grep -c 'testAndDevelopmentOnly' backend/build.gradle` 이 `0` 일 것을 요구한다. 지시대로 쓰면 반드시 `1` 이 나온다. (01-01 Deviation 1 과 **동형** — 플랜의 산문이 자기 grep 게이트를 깨는 패턴 2회째.)
- **Fix:** 주석을 리터럴 없이 재작성 — "Boot 3.2 의 구성명은 `developmentOnly` 다. 3.4+ 문서에 나오는 test 겸용 구성명을 복붙하면 3.2 에서 구성을 찾을 수 없음으로 죽는다". criterion 의 **의도**(3.4+ 구성명을 쓰지 않는다)와 경고 가치를 둘 다 보존한다.
- **Verification:** `grep -c 'testAndDevelopmentOnly'` → `0`, `developmentOnly` 선언 1건 유지, `compileJava` green. Task 1 18/18.
- **Committed in:** `30c4392`

**5. [Rule 3 - Blocking] ARCH-01 조기 완료 표기 회피 (01-01 선례 + CLAUDE.md 정직 표기)**

- **Found during:** 플랜 종료 시 `requirements mark-complete` 단계
- **Issue:** 이 플랜의 frontmatter 는 `requirements: [ARCH-01]` 이지만, REQUIREMENTS.md 의 ARCH-01 문구는 "Spring Modulith 6모듈 경계 **+ `ApplicationModules.verify()` CI 게이트**" 다. **CI 게이트 절반은 Plan 06 소관이고 `.github/workflows/` 는 아직 존재하지 않는다.** ARCH-01 은 01-02·01-06·01-07·01-08 **4개 플랜이 공유**한다. 지금 체크하면 "CI 가 경계 위반을 잡는다"를 CI 없이 선언하게 된다.
- **Fix:** `mark-complete` 미실행. frontmatter 를 `requirements-completed: []` + `requirements-contributed: [ARCH-01]` 로 분리 표기(01-01 이 ARCH-05 에 쓴 것과 동일 형식). 실측 확인: `requirements-completed` 필드는 `gsd-tools.cjs` 가 파싱하지 않아 기계 계약을 깨지 않는다.
- **Verification:** `.planning/REQUIREMENTS.md` line 91/204 가 `[ ] ARCH-01` / `Pending` 로 유지됨.

**6. [Rule 1 - Bug] `gsd-tools` 진행 갱신 결함 재발(3회째) + 신규 1종**

- **Found during:** 플랜 종료 시 STATE/ROADMAP 갱신
- **재발 2종 (01-01 Deviation 4·5 와 동일):**
  - `state update-progress` 가 `{"bar": "[███░░░░░░░] 25%"}` 를 반환하고 **frontmatter 는 25% 로 갱신**했지만 본문 `Progress:` 라인은 `[█░░░░░░░░░] 13%` 그대로였다.
  - `roadmap update-plan-progress 01 01-02 completed` 가 `{"summary_count": 2, "status": "In Progress"}` 를 반환하고 **체크박스(`- [x] 01-02-PLAN.md`)는 실제로 바꿨지만**, Progress 표 행은 `1/8` 에 머물렀다.
- **신규 1종:** `state advance-plan` 이 본문 `Last activity` 의 **설명 텍스트를 파괴**한다 — `2026-08-06 -- 01-01 완료 (저장소 가드레일 4종 + 저장소 밖 미디어 루트)` → `2026-08-06` (날짜만 남고 서술 소실). 01-01 도 같은 증상을 겪고 손으로 복원했으므로 이 역시 재발이다.
- **Fix:** 하네스(`C:/Users/user/.claude/get-shit-done`)는 이 프로젝트 스코프 밖이라 손대지 않았다. 이 저장소 산출물인 `.planning/STATE.md`(본문 바 25% + Last activity 서술 복원)와 `.planning/ROADMAP.md`(표 행 `2/8`)만 **디스크 실측값**으로 정정했다.
- **디스크 실측 근거:** SUMMARY 파일 2개 / PLAN 파일 8개 / ROADMAP 체크박스 `[x]` 2건 — frontmatter(`completed_plans: 2`, `percent: 25`)와 전부 일치.
- **패턴:** 오케스트레이터 지시대로 **도구 반환값(`updated: true`)을 믿지 않고 `git diff` 로 실측**해서 잡았다. 이번엔 두 파일 모두 *일부는* 쓰였기 때문에 `git status` 만 봤다면 "갱신됨"으로 오판했을 것이다 — **부분 기록이 무기록보다 위험하다**는 것이 이번 회차의 추가 정보다.

---

**Total deviations:** 6 (2 blocking, 1 missing-critical, 1 정직 표기, 1 요구사항 표기, 1 도구 버그)
**Impact on plan:** 스코프 확대 없음. 잠긴 결정(D-04·D-14·CP-2·CP-4·CP-5·CP-6·CP-7·CP-8)과 버전 핀(Boot 3.2.12 / Modulith 1.1.12 / Gradle 8.14.5 / resilience4j 2.2.0)은 전부 그대로 집행됐다. Deviation 1 만 **설계 표현이 잠긴 버전에서 물리적으로 불가**한 경우이며, 경계를 넓히지 않는 방향으로 해소했다.

## Verification Results

플랜 `<verification>` 블록 전량 + 태스크 acceptance 36종 중 **35 PASS / 1 충족 불가**(Deviation 2).

```
./gradlew --version               → Gradle 8.14.5
./gradlew build --no-daemon       → BUILD SUCCESSFUL
./gradlew test --tests "*ModularityTests*" → tests=4 failures=0 errors=0
dependencyInsight resilience4j    → 2.2.0 (Selection reasons: Forced), 2.3.x 0건
grep -rln 'Type\.OPEN' ... | grep -v '/shared/'  → 빈 출력
ls .../*/package-info.java | wc -l → 8
build/spring-modulith-docs/       → 17 파일 (components.puml + 모듈 8종 puml/adoc)
git status --porcelain            → 빈 출력 (프로브 잔재 없음)
```

## Issues Encountered

- **알려진 결함 (잔여 노름, 비-제로):**
  1. **`shared` open module 부재의 잔여 차이** — Deviation 1 (c). shared 소비가 시작되는 phase 는 소비자마다 `"shared"` 를 `allowedDependencies` 에 추가해야 하고, 잊으면 verify() 가 붉어진다(조용한 실패는 아님 = 안전한 실패 방향).
  2. **`spring.modulith.events.jdbc.schema-initialization.enabled` 키 이름 미검증** — 플랜/STATE 가 이미 블로커로 등록한 항목. 현재 이 키는 **소비자가 없어**(starter-jpa 미포함) 오타여도 아무 증상이 없다. P3 착수 시 실측 재확인 필요.
  3. **`ddl-auto: validate` + `flyway` 설정은 아직 기동으로 검증되지 않았다** — 이 플랜은 `build`/`test` 까지만 돌렸고 앱을 띄우지 않았다(DB·compose 는 Plan 03/04 소관). 설정 오류는 Plan 04 이후에 드러난다.
  4. **`/api/health` 는 컴파일만 확인** — 실제 200 응답과 D-02 rewrites 경유는 Plan 03/05/07 소관.
  5. **Gradle deprecation 경고** — `Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0`. Boot 3.2 플러그인 계열에서 나오는 것으로 추정되나 **출처 미확인**(`--warning-mode all` 미실행). Gradle 9 는 어차피 Boot 3.2 지원 목록 밖이라 P1 에서는 무해하지만, 정직 표기로 남긴다.
  6. **반사실 프로브는 1회성이다** — 이번에 손으로 넣고 지웠다. 영구 자동 프로브는 Plan 07 Task 1 소관이며, 그전까지는 "verify() 가 살아 있다"의 증거가 이 SUMMARY 의 기록뿐이다.

## User Setup Required

None — 이 플랜은 외부 서비스 설정을 요구하지 않는다.
단, 다른 머신에서 클론할 경우 첫 `./gradlew` 실행이 Gradle 8.14.5 배포판(~130MB)과 의존성을 내려받는다.

## Next Phase Readiness

- **Plan 01-03 (compose)**: `application.yaml` 이 `../compose.yaml` 를 `start-only` 로 가리키고 있다 — compose 파일이 **루트에 그 이름으로** 있어야 `bootRun` 자동 기동이 성립한다. 앱 컨테이너는 `profiles: [full]` 이어야 dev 에서 8080 을 다투지 않는다.
- **Plan 01-04 (Flyway)**: `spring.flyway.locations: classpath:db/migration` 이 이미 선언됐다 — V 마이그레이션은 `backend/src/main/resources/db/migration/` 에 놓아야 한다. `ddl-auto: validate` 이므로 **엔티티가 생기는 순간 스키마가 먼저 있어야** 기동한다.
- **Plan 01-06 (CI)**: G2 게이트의 피검사체가 준비됐다(`dependencyInsight` 가 2.2.0/Forced 출력). `./gradlew build` 가 ModularityTests 4종을 포함하므로 SC2 게이트가 CI 에서 자동 성립한다. `gradlew` exec bit 100755 확인됨.
- **Plan 01-07 (검증)**: V-SC2d 반사실 프로브를 영구화할 때 **이번 1회 프로브가 실제로 붉어졌다는 사실**(`Violations`, catalog → playback::spi)을 기대 출력의 기준선으로 쓸 수 있다.
- **Phase 3 이후 필독:** Deviation 1 — `shared` 는 open module 이 아니다. `TitleId`/`ProfileId` 를 처음 쓰는 모듈이 자기 `allowedDependencies` 에 `"shared"` 를 추가해야 한다.

## Self-Check: PASSED

- 생성 주장 파일 전량 디스크 실재: main java 29 + test java 1 + 빌드 파일 7 (`backend/build.gradle`, `settings.gradle`, `gradle.properties`, `gradlew`, `gradlew.bat`, `gradle/wrapper/*` 2종)
- 커밋 4종 전부 `git log` 에 실재: `30c4392`, `4401f11`, `5706cb4`, `f61d710`
- 정직 표기 검증: `.planning/REQUIREMENTS.md` ARCH-01 이 `[ ]` / `Pending` 로 유지됨 (조기 완료 표기 없음)
- 반사실 프로브 잔재 없음: `git status --porcelain` 빈 출력, `ProbeViolationTemp.java` 부재
- 충족 불가 criterion 1건을 통과로 위장하지 않음 (`grep -c 'Type.OPEN' shared/package-info.java` → 0)

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
