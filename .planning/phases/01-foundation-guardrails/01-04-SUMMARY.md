---
phase: 01-foundation-guardrails
plan: 04
subsystem: database
tags: [flyway, mysql, utf8mb4, ngram, testcontainers, service-connection, seed, cc-by, licensing, counterfactual-probe]

# Dependency graph
requires:
  - phase: 01-02
    provides: "backend Gradle 프로젝트 + flyway-core/flyway-mysql + Testcontainers(mysql/junit-jupiter) + spring-boot-testcontainers + application.yaml(spring.flyway.locations, ddl-auto=validate)"
provides:
  - "V1__init_catalog.sql — genre/title/title_genre 3테이블, 전부 utf8mb4_0900_ai_ci"
  - "title.attribution VARCHAR(300) NOT NULL / title.license VARCHAR(60) NOT NULL (LGL-01 스키마 수준 강제)"
  - "V2__seed_golden.sql — 한글 골든 5건(FIXTURE-METADATA-ONLY) + Blender 오픈무비 5건(CC-BY-3.0) + genre 5종 + title_genre 관계"
  - "SchemaMigrationTest 6종 — V-SC3a~d + V-G3b + FULLTEXT 선점 금지 게이트 (Testcontainers 실 mysql:8.0)"
  - "빈 DB에 Flyway만 돌려 스키마+시드가 재현됨이 자동 회귀로 고정 (SC3)"
  - "반사실 프로브 실측 기준선: 4개 단언이 각각 어떤 값으로 붉어지는지 (Plan 07 영구 프로브 기대출력)"
affects: [01-05-compose, 01-06-ci, 01-07-verification, 03-profile-account, 04-watch-progress, 07-search, 08-ingest]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "스키마·시드의 단일 진실 = Flyway 본선 V 마이그레이션 (initdb.d 미사용, 프로필 분기 없음)"
    - "테스트 컨테이너 기동 파라미터를 compose 와 미러링하고 그 결과를 SQL 로 자기 단언 (CP-10)"
    - "게이트 green 을 받은 뒤 반사실 프로브로 판별력을 실측하고 revert (01-02 확립 패턴의 2회차 적용)"
    - "타 phase 소유 테이블/컬럼 0건을 grep acceptance 로 기계 강제 (D-11 경계 침식 방지)"

key-files:
  created:
    - backend/src/main/resources/db/migration/V1__init_catalog.sql
    - backend/src/main/resources/db/migration/V2__seed_golden.sql
    - backend/src/test/java/com/moabom/SchemaMigrationTest.java
  modified: []

key-decisions:
  - "플랜 <action> 의 `publish_status` 리터럴 주석을 '발행 상태 컬럼' 으로 재작성 — 같은 태스크 acceptance 가 그 리터럴 0건을 요구해 자기모순 (동형 결함 3회째)"
  - "TDD RED 미성립을 조작하지 않고 그대로 보고 — 플랜 태스크 순서(V1·V2 선행)상 구조적으로 불가. 대신 반사실 프로브 4종으로 판별력을 실측"
  - "G3 게이트 강도의 상한을 실측해 명시 — 핀 3값이 mysql:8.0 이미지 기본값과 동일하므로 이 테스트는 'withCommand 삭제'를 잡지 못한다. 문자열 비교 테스트를 추가하는 대신 한계를 문서화하고 compose↔test 대조는 compose 가 실재하는 Plan 05/07 로 넘김"
  - "ARCH-05 mark-complete 미실행 — compose.yaml 부재, 7개 플랜 공유 요구사항 (01-01·01-02·01-03 선례 4회째)"

patterns-established:
  - "게이트는 결과로 판정한다 — green 을 얻은 직후 값을 일부러 틀어 붉어지는 값(expected/but was)을 기록해 vacuous 아님을 증명한다"
  - "게이트의 판별 상한도 함께 실측한다 — '무엇을 잡는가' 만큼 '무엇을 못 잡는가' 를 측정치로 남긴다"

requirements-completed: []
requirements-contributed: [ARCH-05]

# Metrics
duration: 18 min
completed: 2026-08-06
---

# Phase 1 Plan 04: Flyway 스키마 단일 진실 + 한글 골든 시드 Summary

**빈 DB에 Flyway V1/V2만 적용해 카탈로그 스키마와 한글 골든 시드가 재현되고, mysql 기동 파라미터가 "문자열로 존재한다"가 아니라 "실제로 먹었다"로 검증되는 Testcontainers 게이트 6종 착지 — 그 6종이 vacuous 하지 않음을 반사실 프로브 4종(expected/but was 기록)으로 실측**

## Performance

- **Duration:** 약 18분 (14:00+9 착수 ~ 14:18+9)
- **Tasks:** 3/3
- **Files created:** 3 (SQL 2 + Java 1)
- **Commits:** 3 (task 커밋 3 — TDD RED 커밋은 성립하지 않음, 아래 Deviation 2)
- **Docker:** `mysql:8.0` 최초 pull 포함 (로컬 캐시 없었음)

## Accomplishments

- **SC3 가 자동 회귀가 됐다.** `./gradlew test --tests "*SchemaMigrationTest*"` 가 실 mysql:8.0 컨테이너를 띄워 Flyway V1/V2 를 빈 DB에 적용하고, `flyway_schema_history` 에 version 1·2 가 `success=1` 로 2건 있음을 직접 단언한다. "재현된다"가 산문이 아니라 실행 게이트다.
- **G3 가 "결과"로 승격됐다 (CP-10).** 테스트 컨테이너 `withCommand` 를 compose 예정값과 동일하게 미러링하고, `SELECT @@ngram_token_size` = 2 / `SELECT @@collation_server` = `utf8mb4_0900_ai_ci` 를 **실행 중인 서버에서 되읽어** 단언한다. 반사실(토큰 2→3)에서 `expected: 2 / but was: 3` 으로 실제 붉어짐을 확인 — 이 경로가 살아 있다는 증거가 기록으로 남았다.
- **라이선스 의무가 스키마 수준으로 강제된다.** `attribution VARCHAR(300) NOT NULL` / `license VARCHAR(60) NOT NULL` 이 V1 에 처음부터 있다(Pitfall 17 — 나중에 추가하면 시드 전량 재작성). `information_schema.columns` 의 `IS_NULLABLE='NO'` 단언 + `ccByRowsHaveAttribution` 가 빈 문자열 회피까지 막는다.
- **한글이 utf8mb4 로 라운드트립한다.** `title_ko REGEXP '[가-힣]'` 5건 이상 + `'인터스텔라'` 정확 1건. Windows 개발/ubuntu CI 인코딩 혼합에서 Flyway 가 시드를 잘못 읽으면 이 단언이 붉어진다 — 반사실('인터스텔라Z')에서 `expected: 1 / but was: 0` 확인. **`spring.flyway.encoding` 추가 대응은 불필요했다**(깨짐이 관측되지 않음).
- **P7 검색 결정의 선점을 기계가 막는다.** `information_schema.statistics` 의 FULLTEXT 인덱스 0건 게이트. 반사실(프로브가 `ALTER TABLE title ADD FULLTEXT ft_probe`)에서 `expected: 0 / but was: 1` 로 붉어짐 확인 — SRCH-02 의 "ngram vs LIKE 측정 후 채택" 규율이 코드로 지켜진다.
- **D-11 경계가 grep 으로 강제됐다.** `CREATE TABLE account|profile|watch_progress|ingest_job|event_publication` 0건, `asset_path|manifest_path|publish_status|WITH PARSER` 0건. 미확정 설계(에셋 매니페스트 형식, 발행 상태 enum)를 P1 이 추측하지 않았다.
- **두 종류의 시드 행이 값으로 구별된다.** 한글 골든 5건은 `license='FIXTURE-METADATA-ONLY'` 로 "재생 자산이 없다"를 **데이터 자신이 말하고**, Blender 5건은 `CC-BY-3.0` + `© Blender Foundation | blender.org` 로 라이선스 의무를 이행한다. README 의 저작자 표기 문구와 문자열이 일치한다.

## Task Commits

1. **Task 1: V1__init_catalog.sql** — `16ccddd` (feat) — acceptance 8/8
2. **Task 2: V2__seed_golden.sql** — `94da7b0` (feat) — acceptance 8/8
3. **Task 3: SchemaMigrationTest** — `993cef7` (test) — acceptance 8/8 (RED 커밋 없음, Deviation 2)

## Files Created/Modified

- `backend/src/main/resources/db/migration/V1__init_catalog.sql` — genre(코드 유니크) / title(최소 골격 + attribution·license NOT NULL + created_at·updated_at) / title_genre(복합 PK + FK CASCADE), 3테이블 전부 `ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci`. FULLTEXT DDL 0건.
- `backend/src/main/resources/db/migration/V2__seed_golden.sql` — genre 5종(sf/animation/drama/action/thriller), title 10행(한글 골든 5 + Blender 5), title_genre 관계 4개 INSERT…SELECT. DDL 문장 0건.
- `backend/src/test/java/com/moabom/SchemaMigrationTest.java` — `@SpringBootTest` + `@Testcontainers` + `@Container @ServiceConnection MySQLContainer<>("mysql:8.0")`, 테스트 6종.

## Decisions Made

1. **`publish_status` 리터럴 주석 재작성** — Deviation 1. 플랜 산문이 자기 acceptance 를 깨는 3회째 동형 결함.
2. **TDD RED 를 조작하지 않음** — Deviation 2. 플랜의 태스크 순서상 구조적으로 RED 가 불가능했고, 그 사실을 커밋 메시지와 여기에 남긴 뒤 반사실 프로브로 대체 증명했다.
3. **G3 게이트의 판별 상한을 실측하고 명시** — Deviation 3. 문자열 비교 테스트를 덧붙여 커버리지를 "보이게" 만들지 않았다.
4. **`spring.flyway.encoding: UTF-8` 미추가** — 플랜이 예비한 대응이었으나 한글 라운드트립이 처음부터 통과했다. 증상 없는 설정을 선제 추가하지 않는다(추가하면 "이 설정이 필요했다"는 거짓 근거가 남는다).
5. **ARCH-05 mark-complete 미실행** — Deviation 4.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 플랜 `<action>` 이 지시한 주석이 같은 태스크의 acceptance 를 실패시킴 (동형 3회째)**

- **Found during:** Task 1
- **Issue:** `<action>` 의 SQL 헤더 주석이 `--   publish_status (INGST-12 = P3 소유)` 인데, 같은 태스크 acceptance 는 `grep -ciE '(publish_status|asset_path|manifest_path|WITH PARSER)' V1` 이 `0` 일 것을 요구한다. 지시대로 쓰면 반드시 `1` 이 나온다.
- **Fix:** 주석을 `--   발행 상태 컬럼 (INGST-12 = P3 소유)` 로 재작성. criterion 의 **의도**(P1 이 발행 상태 컬럼을 만들지 않는다)와 경고 가치를 둘 다 보존하면서 리터럴만 제거했다. 게이트를 통과시키려고 토큰을 심지 않는 것과 **대칭**인 조치다(01-02 Deviation 2 의 역방향).
- **Files modified:** `backend/src/main/resources/db/migration/V1__init_catalog.sql`
- **Verification:** `grep -ciE '(publish_status|asset_path|manifest_path|WITH PARSER)'` → `0`, 나머지 7개 criterion 동시 PASS.
- **Committed in:** `16ccddd`
- **2-Strike 초과 (3회):** 01-01 Deviation 1 · 01-02 Deviation 4 · 본 건. **패턴이 확정됐다 — 플래너가 `<action>` 코드블록에 넣은 설명 주석이 같은 태스크의 금지어 grep 과 충돌한다.** 하네스 스코프라 이 저장소에서 고치지 않았으나, 플랜 작성 규칙(금지어 목록과 예시 주석의 어휘를 분리)으로 코드화할 후보다.

**2. [정직 표기] TDD RED 가 구조적으로 성립 불가 — 조작하지 않고 반사실 프로브로 대체 증명**

- **Found during:** Task 3 (`tdd="true"`)
- **Issue:** 플랜은 Task 1(V1) → Task 2(V2) → Task 3(테스트) 순서를 지시한다. 즉 테스트를 쓰는 시점에 **검증 대상이 이미 전부 존재**한다. 첫 실행부터 `tests=6 failures=0 errors=0` 으로 green 이었고, RED 커밋을 만들려면 테스트를 일부러 틀리게 쓰는 수밖에 없다.
- **Fix:** 01-02 에서 확립한 규율("RED 가 실패하지 않으면 그 사실 자체를 보고하고, 판별하는 테스트를 찾는다")을 적용. RED 를 위조하지 않고 커밋 메시지에 미성립 사실을 명시한 뒤, **green 커밋 직후 반사실 프로브 1회**로 6종의 판별력을 실측했다. 4개 단언에 동시에 반사실을 넣어 컨테이너 1회 기동으로 끝냈다:

  | 반사실 변형 | 대상 테스트 | 실측 결과 |
  |---|---|---|
  | `--ngram_token_size=2` → `=3` | `mysqlStartupParametersActuallyApplied` | FAIL — `expected: 2 / but was: 3` |
  | `nullability("attribution")` → `nullability("title_en")` | `attributionAndLicenseAreNotNull` | FAIL — `expected: "NO" / but was: "YES"` |
  | `'인터스텔라'` → `'인터스텔라Z'` | `koreanTitlesRoundTrip` | FAIL — `expected: 1 / but was: 0` |
  | `ALTER TABLE title ADD FULLTEXT ft_probe (title_ko)` 주입 | `noFulltextIndexYet` | FAIL — `expected: 0 / but was: 1` |

  → `tests=6 failures=4 errors=0`. 나머지 2종(`flywayAppliedBothMigrations`, `ccByRowsHaveAttribution`)은 반사실을 넣지 않았으므로 pass 로 남았다(=프로브가 무차별 실패를 만든 게 아니라는 대조군 역할).
- **Verification:** 프로브 후 `git checkout --` → `git status --porcelain` 빈 출력 + `git diff HEAD --stat` 빈 출력. 이후 `./gradlew build --no-daemon` 이 `ModularityTests 4 + SchemaMigrationTest 6 = 10 tests / 0 failures` 로 green — **프로브 잔재 0 이 복원 green 으로 이중 확인**됐다. 프로브는 어떤 커밋에도 포함되지 않았다.
- **Committed in:** 테스트 자체는 `993cef7`. 프로브는 미커밋(의도).

**3. [정직 표기 / Rule 2 판단] G3 게이트의 판별 상한 — "withCommand 삭제"는 이 테스트가 잡지 못한다**

- **Found during:** Task 3 프로브 해석
- **Issue:** `mysqlStartupParametersActuallyApplied` 는 값이 **바뀌면** 붉어지지만(위 프로브 실증), 누군가 `withCommand(...)` 를 통째로 지워도 **여전히 통과한다**. 실측 근거 — `docker run --rm mysql:8.0 mysqld --verbose --help`:
  ```
  character-set-server    utf8mb4
  collation-server        utf8mb4_0900_ai_ci
  ngram-token-size        2
  ```
  세 핀 값이 **정확히 이미지 기본값**이다. 즉 이 게이트가 실제로 잡는 것은 (a) 핀 값 자체의 드리프트, (b) my.cnf/conf.d/이미지 교체로 기본값이 달라지는 경우, (c) 향후 MySQL 8.4/9.x 로 태그를 올렸을 때 기본값 변화 — 이고, **못 잡는 것은 (d) `withCommand` 누락**이다.
- **판단 (왜 지금 안 고치는가):** 잡으려면 "테스트 파일의 command 문자열"을 검사해야 하는데 그건 자기 파일을 자기가 읽는 자기충족 검사다. 의미 있는 대조 상대는 **compose.yaml 의 `command:`** 인데 **compose.yaml 은 아직 없다(Plan 05 소관)**. 없는 파일과의 대조를 지금 만들면 그 자체가 죽은 게이트가 된다. → 한계를 측정치로 남기고 대조 게이트는 Plan 05/07 로 넘긴다. **커버리지를 "보이게" 만드는 문자열 테스트를 추가하지 않았다.**
- **Verification:** 위 `mysqld --verbose --help` 출력이 근거. 프로브는 (a) 경로가 살아 있음을 증명했다.
- **Committed in:** 코드 변경 없음(문서화만). Plan 05/07 인계 항목 + STATE 블로커로 등록.

**4. [Rule 3 - Blocking] ARCH-05 조기 완료 표기 회피 (선례 4회째)**

- **Found during:** 플랜 종료 시 `requirements mark-complete` 단계
- **Issue:** 이 플랜 frontmatter 는 `requirements: [ARCH-05]` 이지만 ARCH-05 문구는 "docker-compose **한 번으로 전 스택 기동**"이다. `compose.yaml` 은 여전히 존재하지 않고(Plan 05 소관), ARCH-05 는 7개 플랜이 공유한다. 지금 체크하면 기동되지 않는 스택을 기동된다고 선언한다.
- **Fix:** `mark-complete` 미실행. frontmatter 를 `requirements-completed: []` + `requirements-contributed: [ARCH-05]` 로 분리 표기(01-01·01-02·01-03 과 동일 형식).
- **Verification:** `.planning/REQUIREMENTS.md` 의 ARCH-05 가 `[ ]` / `Pending` 로 유지됨.

**5. [Rule 1 - Bug] `gsd-tools` 진행 갱신 결함 재발 (4회째)**

- **Found during:** 플랜 종료 시 STATE/ROADMAP 갱신
- **재현 3종 (전부 01-01·01-02·01-03 과 동일 증상):**
  - `state update-progress` 가 `{"updated": true, "percent": 50, "bar": "[█████░░░░░] 50%"}` 를 반환하고 **frontmatter 는 50% 로 갱신**했지만, 본문 `Progress:` 라인은 `[████░░░░░░] 38%` 그대로였다.
  - `state advance-plan` 이 `last_activity` 의 **서술 텍스트를 파괴**했다 — frontmatter·본문 양쪽에서 `2026-08-06 -- 01-03 완료 (Next 16.2.12 골격 + ...)` → `2026-08-06`.
  - `roadmap update-plan-progress 01 01-04 completed` 가 `{"updated": true, "summary_count": 4}` 를 반환하고 **체크박스 `- [x] 01-04-PLAN.md` 는 실제로 썼지만**, Progress 표 행은 `3/8` 에 머물렀다.
- **신규 1종 (도구가 아니라 호출자 = 나의 인자 실수):** `state add-decision --phase "Phase 01"` 로 호출해 `- [Phase Phase 01]:` 이중 접두가 3건 생성됐다. 도구가 `Phase` 를 스스로 붙인다 — 올바른 인자는 `--phase "01"`. 기존 78줄과 서식이 어긋나므로 3건 전부 수기 정정했다.
- **부수 확인:** `state add-blocker` 의 계약은 `--text "..."` 다(`--help` 는 미지원 — 인자 없이 실행해야 usage 가 나온다). 블로커 2건은 정상 기록됐고, `record-metric`·`record-session` 도 정상이었다. 즉 **6개 호출 중 3개가 부분 기록**이다.
- **Fix:** 하네스는 이 프로젝트 스코프 밖이라 손대지 않고, 이 저장소 산출물(`.planning/STATE.md`, `.planning/ROADMAP.md`)만 디스크 실측값으로 정정했다.
- **패턴:** 도구 반환값(`updated: true`)을 믿지 않고 `git diff` 로 실측한다 — 4회 연속 유효했다.

---

**Total deviations:** 5 (1 blocking-자기모순, 1 TDD 정직 표기, 1 게이트 한계 정직 표기, 1 요구사항 표기, 1 도구 버그)
**Impact on plan:** 스코프 확대 없음. 잠긴 결정(D-11·D-12·D-13·D-14)과 CP-10 미러링, FULLTEXT 선점 금지가 전부 그대로 집행됐다. 코드 레벨 변경은 Deviation 1 의 주석 한 줄뿐이다.

## Verification Results

태스크 acceptance 24종 **전부 PASS** + 플랜 `<verification>` 블록 전량 green.

```
V1: exists / attribution NOT NULL 1 / license NOT NULL 1 / title_ko·title_en·synopsis 각 2
    utf8mb4_0900_ai_ci 3 / 타 phase CREATE TABLE 0 / 금지 컬럼 0 / FULLTEXT DDL 0 / UTF-8 no-BOM
V2: FIXTURE-METADATA-ONLY 5 / CC-BY-3.0 5 / '인터스텔라' 1 / Blender Foundation 5
    INSERT INTO genre 1 / DDL 문장 0 / UTF-8 no-BOM
T3: ngram_token_size=2 / SELECT @@ngram_token_size / utf8mb4_0900_ai_ci
    index_type = 'FULLTEXT' / @ServiceConnection  (grep 5종 PASS)

./gradlew test --tests "*SchemaMigrationTest*" --no-daemon → BUILD SUCCESSFUL
   SchemaMigrationTest tests=6 skipped=0 failures=0 errors=0
./gradlew build --no-daemon                               → BUILD SUCCESSFUL
   ModularityTests tests=4 failures=0 / SchemaMigrationTest tests=6 failures=0  (TOTAL 10 / 0)

반사실 프로브 (미커밋, revert 완료)                        → tests=6 failures=4 errors=0
git status --porcelain (프로브 후)                         → 빈 출력
```

## Issues Encountered

- **`mysql:8.0` 로컬 캐시 없음** — Testcontainers 첫 기동 전에 `docker pull mysql:8.0` 을 선행해 gradle 테스트 타임아웃과 pull 지연을 분리했다. 이후 테스트 1회당 ~47s(컨테이너 기동 + 컨텍스트 + Flyway).
- **Gradle 데몬 소켓 노이즈** — 첫 실행 로그에 `Unexpected type tag 71 found` / `Could not read message from '/127.0.0.1:...'` 스택이 다수 출력됐다. `--no-daemon` 단일 사용 데몬의 클라이언트 연결 종료 시 직렬화 경합으로 보이며 **BUILD SUCCESSFUL 과 테스트 XML(6/0/0)에는 영향이 없었다.** 후속 실행에서는 재현되지 않았다. 원인 미확정 — 정직 표기로 남긴다.
- **python stdout cp949** — 테스트 XML 파싱 스크립트가 한글/em-dash 출력에서 `UnicodeEncodeError` 로 죽었다. `sys.stdout.reconfigure(encoding='utf-8')` 로 해소(하네스 메모리 기지 항목).

- **알려진 결함 (잔여 노름, 비-제로):**
  1. **G3 게이트가 `withCommand` 누락을 못 잡는다** — Deviation 3. 핀 3값 = 이미지 기본값(실측). compose↔test 대조는 Plan 05/07 소관.
  2. **compose.yaml 과의 정합은 아직 미검증** — 이 테스트가 미러링한다고 주장하는 대상이 존재하지 않는다. Plan 05 가 compose 를 만들 때 **이 파일의 3개 인자와 문자 그대로 같아야** 한다.
  3. **`spring.flyway.encoding` 미명시** — 현재 Windows 로컬에서는 한글이 정상 라운드트립하지만 **ubuntu CI 에서는 아직 한 번도 실행되지 않았다**(Plan 06). CI 첫 런에서 `koreanTitlesRoundTrip` 이 붉어지면 플랜이 지시한 대응(`spring.flyway.encoding: UTF-8` 추가, 시드 ASCII 화 금지)을 그대로 적용해야 한다.
  4. **`ddl-auto: validate` 는 아직 엔티티가 0개라 사실상 무검증** — 스키마↔엔티티 드리프트 방지 효과는 P3 에서 첫 `@Entity` 가 생길 때부터 발효한다.
  5. **빈 DB 재현성의 "수동 확인"(compose down -v → bootRun)은 수행하지 않았다** — compose.yaml 부재. Testcontainers 가 매 실행 새 컨테이너를 띄우므로 "빈 DB 재현"은 자동 경로로는 이미 매번 증명되지만, **실 compose 볼륨 경로는 Plan 05/07 에서 처음 검증된다.**
  6. **반사실 프로브는 1회성이다** — 영구 자동 프로브는 Plan 07 소관. 그전까지 "6종 게이트가 살아 있다"의 증거는 이 SUMMARY 의 expected/but-was 기록뿐이다.

## User Setup Required

None — 외부 서비스 설정 없음.
단, 이 테스트는 **Docker 를 요구한다**. Docker 데몬이 없는 환경에서 `./gradlew build` 는 `SchemaMigrationTest` 단계에서 실패한다(CI 는 ubuntu-latest = OK, Windows 러너는 불가 — STACK §2.9).

## Next Phase Readiness

- **Plan 01-05 (compose)**: mysql 서비스 `command:` 는 이 테스트와 **문자 그대로 동일**해야 한다 — `--ngram_token_size=2`, `--character-set-server=utf8mb4`, `--collation-server=utf8mb4_0900_ai_ci`. 두 곳이 갈라지면 P7 에서 "테스트는 통과하는데 실 DB 는 다르다"로 터진다(CP-10). **또한 Deviation 3 때문에 이 테스트가 그 divergence 를 자동으로는 못 잡는다 — Plan 05/07 이 compose↔test 대조 게이트를 세워야 한다.**
- **Plan 01-06 (CI)**: `./gradlew build` 가 이제 Docker 를 요구한다. CI job 이 `ubuntu-latest` 인지, Docker 서비스가 있는지 확인 필요. **ubuntu CI 첫 런이 Flyway 인코딩(잔여 결함 3)의 실검증 지점**이다.
- **Plan 01-07 (검증)**: 반사실 프로브 영구화 시 이 플랜의 4종 기대출력(`expected: 2/but was: 3` 등)을 기준선으로 재사용할 수 있다.
- **Phase 3 (account/profile, event_publication)**: V3 이후 마이그레이션은 **증분 추가**다. `event_publication` DDL 은 `@ApplicationModuleListener` 첫 사용 phase 가 소유(D-14). `title` 에 컬럼을 추가할 때 `ddl-auto: validate` 가 엔티티와의 정합을 처음으로 실제 검사하기 시작한다.
- **Phase 7 (검색)**: FULLTEXT 인덱스는 **여기서 처음 생긴다**. 생기는 순간 `noFulltextIndexYet` 테스트를 P7 이 **삭제/전환**해야 하며, 그 삭제가 곧 "측정 후 채택" 결정이 내려졌다는 표시가 된다. 대상 컬럼(`title_ko`/`title_en`/`synopsis`)은 이미 존재하므로 스키마 재작업은 없다.
- **Phase 8 (인제스트)**: 에셋 경로/매니페스트 컬럼은 P1 에 **없다**. Blender 5행은 메타만 있고 에셋 경로는 P2 배치 스크립트가 형식을 확정한 뒤 그 phase 가 추가한다(D-12).

## Self-Check: PASSED

- 생성 주장 파일 4종 전량 디스크 실재: `V1__init_catalog.sql`, `V2__seed_golden.sql`, `SchemaMigrationTest.java`, `01-04-SUMMARY.md`
- 커밋 3종 전부 `git log` 에 실재: `16ccddd`, `94da7b0`, `993cef7`
- **진행 표기 5중 대조 일치** — SUMMARY 파일 4 = ROADMAP 체크박스 `[x]` 4 = ROADMAP 표 `4/8` = STATE frontmatter `completed_plans: 4`/`percent: 50` = STATE 본문 `Progress: [█████░░░░░] 50%`
- 도구 파손 복구 확인: `last_activity` 서술 2곳 복원, `[Phase Phase` 이중 접두 잔재 0건
- 정직 표기 검증: `.planning/REQUIREMENTS.md` 의 ARCH-05 가 line 95 `[ ]` / line 208 `Pending` 로 유지됨 (조기 완료 표기 없음)
- 반사실 프로브 잔재 없음: 프로브 revert 후 `git status --porcelain` 빈 출력 + 복원 풀빌드 10/10 green

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
