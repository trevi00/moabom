# Phase 1 CI 게이트 도달성 기록

> 워크플로 파일이 존재하는 것과 게이트 스텝이 실제로 돌아 결론을 낸 것은 다르다.
> 일괄 실행 경로가 없는 게이트는 썩는다 — 첫 런에서 도달성을 확인해 둔다.

**Run:** https://github.com/trevi00/moabom/actions/runs/31077663272
**Trigger:** push / main (event `push`, D-08 의 push 트리거가 실제로 발화)
**Conclusion:** success
**Runner:** ubuntu-latest · **Job:** `core` (ID 92539135966) · **소요:** 2m5s (06:33:09Z → 06:35:14Z)
**Commit:** `d45e800b93a9ce7c826181319c4fefc3440a19d2` · **실행일:** 2026-08-06

이 저장소의 **첫 CI 실행**이다. 그 전까지 워크플로는 0회 실행 상태였다(01-06/01-07 이 7회에 걸쳐
ARCH-01 체크를 미룬 이유). 첫 런은 캐시가 비어 있어 모든 태스크가 강제 실행된다 — 그래서 이 런이
게이트 도달성의 유일한 무오염 관측 지점이다.

## 스텝별 결론

스텝 16개 전부 `success`, 붉은 스텝 0개. 그중 게이트는 8종이다
(플랜이 예상한 7종 + 01-06 이 신설한 mysql 핀 대조 1종).

| # | 스텝 | conclusion | 실행 구간 | 무엇을 증명하는가 |
|---|------|-----------|----------|------------------|
| 1 | media guard (SC4) | success | 06:33:15 | 히스토리 전체에 5MB 초과 blob·미디어 확장자 0건 (`fetch-depth: 0` 위에서 — 아래 §도달성 전제 참조) |
| 2 | gitignore sanity (CP-1 회귀 / V-SC4c) | success | 06:33:15 | 미디어는 매칭, `frontend/next.config.ts` 는 비매칭, `.env` 는 매칭 (CP-1 회귀 없음) |
| 3 | modulith OPEN guard (SC2 / V-SC2b) | success | 06:33:15 | shared 외 `Type.OPEN` 0건. ⚠️ **오늘은 발화 불가한 전방 게이트** — 아래 §정직 표기 |
| 4 | module declaration coverage (SC2 / V-SC2c) | success | 06:33:15 | 모듈 package-info 8개 + `@ApplicationModule` 8개 |
| 5 | mysql 기동 파라미터 대조 (G3 / CP-10) | success | 06:33:15 | compose ↔ SchemaMigrationTest 파라미터 3종 문자 일치 + 개수 선-단언 3 (01-06 신설분의 첫 CI 발화) |
| 6 | build + test (ApplicationModules.verify() + SchemaMigrationTest 포함) | success | 06:33:18 → 06:34:52 (1m34s) | ubuntu 에서 `verify()` + Testcontainers MySQL 통합 테스트가 **실제로 실행**됨 — 아래 §실행 증거 |
| 7 | resilience4j pin gate (G2 / V-G2a) | success | 06:34:52 → 06:35:01 | 해석 버전 **2.2.0**, 2.3.x 부재 |
| 8 | compose config validity (ARCH-05 저비용 프록시 / V-SC1a) | success | 06:35:01 | `--profile full config -q` 통과 |

게이트 8종은 위반 시 `exit 1` 하고 정상 경로에서는 stdout 을 내지 않는 설계다. 따라서 스텝 1~5·8 의
증거는 **conclusion + 실행 구간**이며, 6·7 만 판정 근거를 stdout 으로 남긴다.

### 스텝 7 의 출력 원문

```
io.github.resilience4j:resilience4j-spring-boot3:2.2.0
   Selection reasons:
      - Forced
```

`Forced` 는 `build.gradle` 의 `resolutionStrategy.force` 가 살아 있다는 뜻이다 —
RESEARCH 가정 A7(force 가 게이트의 grep 패턴과 충돌하지 않는가)이 CI 층에서도 재확인됐다.

## 실행 증거 — 초록 체크마크가 아니라 로그로 (스텝 6)

01-06 이 로컬에서 **이중 vacuous PASS** 를 실측했다: `./gradlew build` 가 전 태스크 `UP-TO-DATE` 로
11초 만에 `BUILD SUCCESSFUL`, `cleanTest test` 는 `:test FROM-CACHE`. **두 번의 초록 모두 테스트를
한 줄도 실행하지 않았다.** 같은 위조가 CI 에서 일어나지 않았음을 세 갈래로 확인한다.

**(1) Gradle 의 태스크 회계 — 결정적 한 줄**

```
BUILD SUCCESSFUL in 1m 34s
7 actionable tasks: 7 executed
```

`7 executed` 다. `from cache` 도 `up-to-date` 도 0이다. 캐시 서빙이었다면
`7 actionable tasks: 6 executed, 1 from cache` 형태로 갈라져 나타난다.

**(2) 태스크 목록에 캐시 접미사가 없다**

```
> Task :compileJava
> Task :processResources
> Task :classes
> Task :resolveMainClassName
> Task :bootJar
> Task :jar
> Task :assemble
> Task :compileTestJava
> Task :processTestResources NO-SOURCE
> Task :testClasses
> Task :test
> Task :check
> Task :build
```

`> Task :test` 에 `FROM-CACHE`/`UP-TO-DATE` 접미사가 붙지 않았다. 유일한 접미사는
`:processTestResources NO-SOURCE`(테스트 리소스 디렉터리가 비어 있음 — 정상)다.

**(3) 실제 애플리케이션 컨텍스트가 뜨고 닫혔다**

```
2026-08-06T06:34:52.406Z  INFO 2643 --- [moabom] [ionShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2026-08-06T06:34:52.415Z  INFO 2643 --- [moabom] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2026-08-06T06:34:52.439Z  INFO 2643 --- [moabom] [ionShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed.
```

`HikariPool-1` 이 열렸다 닫혔다는 것은 **실 DB 커넥션이 맺어졌다**는 뜻이고, 이 빌드에서 DB 는
Testcontainers MySQL 뿐이다 — 즉 `SchemaMigrationTest` 가 컨테이너를 띄우고 붙었다. 캐시 서빙이면
JVM 자체가 뜨지 않으므로 이 로그는 존재할 수 없다. 시간축도 정합한다: `:testClasses` 06:34:13 →
`:check` 06:34:52 로 **약 39초**가 테스트에 쓰였다.

**(4) 부수 확인** — `Downloading .../gradle-8.14.5-bin.zip` 으로 wrapper 핀(8.14.5)이 CI 에서도 그대로다.

### 이 절이 증명하지 **못하는** 것 (정직 표기)

**개별 테스트 개수(`tests=`)는 CI 로그에 없다.** Gradle 은 테스트가 전부 통과하면 개수를 출력하지
않는다(붉을 때만 요약을 낸다). 따라서 위 증거는 **":test 가 실행됐고 붉은 테스트가 0개"** 까지를
말하며, "10/10 이 실행됐다"까지는 말하지 않는다. 10/10 은 01-06 이 **로컬에서** test-results XML
직독으로 확인한 수치다(`ModularityTests` 4 + `SchemaMigrationTest` 6).

수집이 0건이어도 통과하는 구조적 구멍(테스트 미등록·discover 누락)은 이 로그로 닫히지 않는다.
다만 (3)의 Hikari/JPA 컨텍스트 로그가 **최소 1개 이상의 `@SpringBootTest` 가 실제로 돌았음**은
증명하므로 "0건 수집"은 배제된다. 개수 단언을 CI 에 세우는 것은 P2 증설 후보로 남긴다(§잔여 3).

## 도달성의 전제 — `fetch-depth: 0` 이 실제로 적용됐는가

01-06 의 반사실 arm 6b 가 실측했다: 같은 저장소를 `--depth 1` 로 얕게 클론하면 미디어 히스토리
스캔이 **rc=0 으로 조용히 통과한다**(아무것도 못 봄). 스텝 1 의 green 은 이 전제가 성립할 때만
의미를 갖는다. 첫 런의 checkout 명령을 원문으로 확인했다:

```
/usr/bin/git -c protocol.version=2 fetch --no-tags --prune --no-recurse-submodules origin +refs/heads/*:refs/remotes/origin/* +refs/tags/*:refs/tags/*
```

**`--depth` 플래그가 없다.** 기본값(`fetch-depth: 1`)이었다면 같은 자리에 `--depth=1` 이 찍힌다.
워크플로 입력 역시 로그에 `fetch-depth: 0` 으로 기록됐다. 스텝 1 은 43개 커밋 전체를 봤다.

## V-CP5 — Windows→ubuntu 전이 확인

스텝 6 이 실행됐다는 사실 자체가 `gradlew` 의 개행(LF)과 실행 권한(exec bit)이 정상임을 증명한다.
CRLF 였다면 `/usr/bin/env: 'sh\r'`, exec 비트가 없었다면 `Permission denied` 로 죽는다.

push 이전에 로컬에서 선-확인한 값도 같은 결론이다:

```
$ git ls-files --eol backend/gradlew
i/lf    w/lf    attr/text eol=lf      backend/gradlew
$ git ls-files -s backend/gradlew
100755 739907dfd15937f032b5a46390e9d63709e87f7a 0  backend/gradlew
```

인덱스 `i/lf` + 모드 `100755` — 01-01 의 `.gitattributes`(`gradlew text eol=lf`)가 의도대로 작동했다.
**한글 인코딩도 같이 닫혔다**: `SchemaMigrationTest` 의 `koreanTitlesRoundTrip` 은 ubuntu 첫 런이
실검증 지점이었는데(01-04→01-06 인계), 스텝 6 이 붉지 않았으므로 `spring.flyway.encoding` 추가
대응은 불필요했다. 시드 ASCII 화 회피 규율도 그대로 유지된다.

## 두 층의 방어 대조 (정직 표기)

| 층 | 우회 가능? | 증명 위치 |
|---|---|---|
| 로컬 pre-commit 훅 | `--no-verify` 로 가능 → defense-in-depth | Plan 07 `01-COUNTERFACTUAL-LOG.md` V-SC4d |
| CI 히스토리 스캔 | push 시점 발화, 커밋 단위 우회 불가 = **실 boundary** | 본 문서 스텝 1 |

정확히 하면 CI 층도 절대적 boundary 는 아니다 — 워크플로 파일 자체를 같은 push 로 고치면
게이트가 사라진다. 그것을 막는 것은 GitHub 설정이 아니라 **커밋 diff 가 공개 이력에 남는다는
사실**이다. 즉 최종 boundary 는 사람의 diff 리뷰이고, CI 는 그 리뷰가 놓친 것을 기계적으로
붙잡는 층이다.

### 이 런이 함께 확정한 저장소 측 방어

| 항목 | 상태 | 확인 |
|---|---|---|
| visibility | public (D-07) | `gh repo view --json visibility` |
| default branch | `main` (D-08) | `gh repo view --json defaultBranchRef` |
| secret scanning | enabled | `gh api repos/trevi00/moabom` |
| secret scanning push protection | enabled — **첫 push 이전에 켰다** | 〃 |
| branch protection | 미설정 (404, D-08 solo 모드 의도) | `gh api .../branches/main/protection` |

## 잔여

- **CI 게이트의 반사실**(일부러 위반한 커밋을 push 해 붉어지는지)은 수행하지 않았다.
  공개 저장소 이력에 의도적 위반 커밋을 남기지 않기 위해서다. 로컬 층에서 같은 술어를
  반사실로 증명했고(Plan 07), CI 스텝은 같은 커맨드를 쓴다 — 그 사실이 이 결정의 근거다.
  다만 이것은 **논증이지 관측이 아니다**: 오늘 우리가 가진 것은 8종의 green(=거짓 음성 없음)이고,
  CI 층에서의 참 양성은 아직 0회 관측이다.
- **[HIGH · 실측 확정] 2회차 런은 이미 테스트를 0건 실행하고 green 이다.**
  01-06 이 "가능성(미실측)"으로 남긴 것을 이 플랜이 **관측으로 닫았다**. 첫 런의 post 스텝이
  `Saved cache entry with key gradle-build-cache-v2-daae706698ad081b77dd835d8d020023` 로 Gradle
  build cache 를 GitHub Actions 캐시에 저장했고, 곧이어 문서 커밋 `1601c29` 로 발화한
  **2회차 런**([31078140031](https://github.com/trevi00/moabom/actions/runs/31078140031), conclusion `success`)이
  같은 키를 복원해 다음을 남겼다:

  ```
  Restored cache entry with key gradle-build-cache-v2-daae706698ad081b77dd835d8d020023 in 982ms
  > Task :testClasses UP-TO-DATE
  > Task :test FROM-CACHE
  BUILD SUCCESSFUL in 10s
  7 actionable tasks: 4 executed, 3 from cache
  ```

  1m34s → **10s**, `7 executed` → `4 executed, 3 from cache`. 즉 **2회차 CI 는 초록이면서
  테스트를 한 줄도 돌리지 않았다.** 01-06 이 로컬에서 실측한 이중 vacuous PASS 가 CI 층에서
  그대로 재현된 것이다 — 이번엔 추정이 아니라 관측이다.

  함의: 첫 런의 무오염성은 훼손되지 않지만(캐시가 비어 있었다), **오늘 이후의 모든 초록은
  기본적으로 실행 증거가 아니다.** 소스가 바뀌면 캐시 키가 달라져 재실행되므로 "코드 변경 커밋은
  실행된다"가 성립하나, 그 판정을 사람이 매번 로그로 해야 한다는 뜻이다. 이 플랜의 위협 등록
  T-1-39("게이트가 존재하지만 실행되지 않는 상태로 방치")의 완화가 **첫 런 1회만 덮는다**는
  사실이 여기서 드러났다.

  **해소 후보 (D-09 가 P1 CI 범위를 못박았으므로 이 플랜은 변경하지 않고 결정을 이월한다):**

  | 안 | 방법 | 대가 |
  |---|---|---|
  | (a) 테스트만 캐시 배제 | `test { outputs.upToDateWhen { false } }` 또는 test 스텝에 `--no-build-cache` | 매 런 +~1.5분, 컴파일 캐시는 유지 |
  | (b) 개수 단언 스텝 증설 | test-results XML 의 tests 합계를 읽어 하한(10) 단언 | FROM-CACHE 여도 XML 은 복원되므로 **이 구멍은 못 막는다** — (a) 와 병행해야 의미 |
  | (c) 현 상태 수용 | 초록을 신뢰하지 않고 릴리즈 시점에만 로그 확인 | 규율 의존 = 썩는다 (본 문서 서두의 명제와 충돌) |

  권장은 (a)+(b) 병행이며, **Phase 2 착수 시 operator 판단 항목**으로 이월한다.
- **스텝 3(OPEN guard)의 green 은 오늘 아무것도 증명하지 않는다.** 잠긴 핀 Modulith 1.1.12 의
  `@ApplicationModule` 에는 `type` 속성 자체가 없다(01-02 javap 실측). 어떤 커밋도 이 게이트를
  발화시킬 수 없으므로 green 은 "경계가 지켜졌다"가 아니라 "검사 대상이 존재할 수 없다"는 뜻이다.
  1.2+ 상향 시 자동으로 살아나는 전방 회귀 게이트로 유지한다(ci.yml 주석에 동일 표기).
- 테스트 **개수** 단언이 CI 에 없다(§실행 증거 말미). test-results XML 요약 스텝 증설이 후보이나,
  D-09 가 P1 CI 범위를 코어 게이트로 못박았으므로 **P2 증설 후보**로 남긴다.
- FE 빌드 게이트는 D-09 범위 밖이라 P1 CI 에 없다. **Phase 2 증설 후보**로 남긴다.
- `Deprecated Gradle features were used in this build, making it incompatible with Gradle 9.0` 경고가
  나왔다. STACK 이 이미 Gradle 9.x 를 지원 목록 밖으로 판정했으므로 현 핀(8.14.5)과 무충돌이며,
  Boot 3.2 라인을 쓰는 동안은 대응 불요다. Boot 상향 시 동반 처리 항목으로만 기록해 둔다.
