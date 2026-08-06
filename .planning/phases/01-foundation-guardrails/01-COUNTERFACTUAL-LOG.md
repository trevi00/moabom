# Phase 1 반사실 프로브 실행 기록

> 게이트의 **존재**가 아니라 **작동**을 증명한다. SC2·SC4 의 문장("CI 에서 실패한다",
> "들어가지 못한다")은 반사실 없이는 검증되지 않는다.
> 실행일: 2026-08-06 / 실행 환경: Windows 11 (Git Bash, MSYS) · Docker 28.1.1 / Compose v2.35.1-desktop.1 ·
> Gradle 8.14.5 · 저장소 `D:/moabom` (실행 시작 시 `git status --porcelain` 빈 출력)

## V-SC2d — 모듈 경계 위반이 verify() 를 죽이는가

**위반 주입:** `backend/src/main/java/com/moabom/catalog/BoundaryProbe.java`
(catalog → `com.moabom.playback.spi.WatchHistoryPort`, catalog 의 allowedDependencies 는 `{}`)

**커맨드:** `cd backend && ./gradlew test --tests "*ModularityTests*" --no-daemon --no-build-cache`
**종료코드:** `1`   (기대: != 0)

**실제 출력 인용** (Gradle 콘솔):
```
> Task :test

ModularityTests > verifiesModularStructure() FAILED
    org.springframework.modulith.core.Violations at ModularityTests.java:22

4 tests completed, 1 failed

> Task :test FAILED
```

**위반 원문** (`build/test-results/test/TEST-com.moabom.ModularityTests.xml` 의 failure message):
```
org.springframework.modulith.core.Violations: - Module 'catalog' depends on named interface(s)
'playback :: spi' via com.moabom.catalog.BoundaryProbe -> com.moabom.playback.spi.WatchHistoryPort.
Allowed targets: none.
```

즉 **컴파일 실패가 아니라 `verify()` 의 정적 분석이 잡았다** — 위반 클래스·대상 named interface·
"Allowed targets: none"(= `allowedDependencies = {}` 선언이 실효 중)까지 지목했다.
플랜이 경고한 "컴파일 실패로 테스트가 아예 안 돈 경우"가 **아님**을 `4 tests completed, 1 failed`
(= 4개가 모두 실행됐고 그중 경계 테스트만 붉어짐)이 구분해 준다.

**원복:** `rm src/main/java/com/moabom/catalog/BoundaryProbe.java`
**원복 후 재실행:** `./gradlew cleanTest test --tests "*ModularityTests*" --no-daemon --no-build-cache`
**종료코드:** `0`   (기대: 0)

**vacuous-PASS 배제 (01-06 인계 대응):** 원복 후 Gradle 이 `compileJava UP-TO-DATE` 로 보고했기 때문에
"낡은 `BoundaryProbe.class` 가 남아 있는데도 통과한 것" 또는 "테스트가 캐시로 서빙돼 한 줄도 안 돈 것"
두 가지 위조 가능성을 각각 실측으로 배제했다:
- `ls build/classes/java/main/com/moabom/catalog/` → `CatalogApi.class`, `package-info.class`, `spi/` 뿐
  (프로브 클래스 없음 — 증분 컴파일이 산출물을 제거했다)
- `TEST-com.moabom.ModularityTests.xml` → `tests="4" skipped="0" failures="0" errors="0"`
  (`cleanTest` + `--no-build-cache` 로 재생성된 결과. 실행 0줄이 아니다)

**판정: PASS**

## V-SC4d — pre-commit 훅이 미디어·대용량을 거부하는가

훅 배선 전제 확인: `git config --get core.hooksPath` → `.githooks`, `.githooks/pre-commit` 권한 `755`.

### (a) 미디어 확장자

`.gitignore` 가 먼저 잡는다는 것을 먼저 확인하고(`git check-ignore -v probe.mp4` →
`.gitignore:7:*.mp4  probe.mp4`), `-f` 로 **1차 방어를 일부러 우회해** 훅이 2차 방어인지 시험했다.

**커맨드:** `git add -f probe.mp4 && git commit -m "probe: should be blocked (media extension)"`
**종료코드:** `1`   (기대: != 0)
**실제 stderr 인용:**
```
BLOCKED (media extension): probe.mp4

미디어/대용량 파일은 저장소에 들어갈 수 없습니다. 미디어 루트는 D:/moabom-media/ 입니다.
우회가 정말 필요하면: git commit --no-verify (이력 세탁 비용을 감수한다는 뜻)
```

### (b) 5MB 초과

`git check-ignore -v probe-big.bin` → `(not ignored)` — 즉 이 파일은 `.gitignore` 에 걸리지 않으므로
**크기 게이트만** 시험한다.

**커맨드:** `git add probe-big.bin && git commit -m "probe: should be blocked (size)"` (6,291,456 B)
**종료코드:** `1`   (기대: != 0)
**실제 stderr 인용:**
```
BLOCKED (>5MB: 6291456B): probe-big.bin
BLOCKED (media extension): probe.mp4
```

**⚠️ 귀속 오류 배제 (추가 arm):** 위 실행 시점에는 (a) 의 `probe.mp4` 가 아직 스테이징에 남아 있었다.
그러면 **종료코드 1 이 크기 게이트 때문인지 확장자 게이트 때문인지 종료코드만으로는 구분되지 않는다**
(stderr 의 `BLOCKED (>5MB` 줄은 크기 분기에서만 나오므로 이미 구분되지만, 종료코드 귀속을 닫기 위해
격리 arm 을 한 번 더 돌렸다):

**커맨드:** `git reset HEAD probe.mp4` → 스테이징 `probe-big.bin` 단독 → `git commit -m "probe: should be blocked (size, isolated)"`
**종료코드:** `1`
**실제 stderr 인용:**
```
BLOCKED (>5MB: 6291456B): probe-big.bin
```
크기 게이트가 **단독으로** 커밋을 거부한다.

### 대조군 (판별력 — 훅이 무조건 막는 것이 아님)

무조건 실패하는 훅도 위 증거를 똑같이 만들어 낸다. 그 위조 가능성은 **이 로그 파일 자신의 커밋**이
배제한다 — 같은 세션·같은 훅 아래에서 일반 텍스트 파일 커밋은 통과했다(본 문서를 담은 `docs(01-07)` 커밋).
차단은 미디어 확장자·5MB 초과라는 술어에 한정된다.

### 원복

**커맨드:** `git reset HEAD probe-big.bin; rm -f probe.mp4 probe-big.bin`
**원복 확인:** `git status --porcelain` → 빈 출력
**이력 오염 확인:** `git log --all --diff-filter=A --name-only --pretty=format: | grep -c probe` → `0`
**잔여물 확인:** `test -f backend/src/main/java/com/moabom/catalog/BoundaryProbe.java` → 비존재

**판정: PASS**

## 알려진 잔여 (정직 표기)

- pre-commit 훅은 `git commit --no-verify` 로 우회된다. 이 층은 defense-in-depth 이고
  **실 boundary 는 CI 의 히스토리 전체 스캔**이다 (Plan 06, `fetch-depth: 0` + `git rev-list --objects --all`).
  CI 게이트가 실제로 발화하는지는 Plan 08 의 첫 push 에서 확인한다.
- 훅은 `core.hooksPath` 설정에 의존한다. 저장소를 새로 clone 한 사람이 `git config core.hooksPath .githooks`
  를 실행하지 않으면 **훅 자체가 없다**. 이 역시 CI 스캔이 최종 방어선인 이유다 (README 에 설정 절차 명시).
- 두 프로브는 manual-once 다. 자동화하면 "일부러 깨진 커밋" 이 이력에 남는다. 대신 이 문서가
  커맨드·종료코드·오류 원문을 인용해 재현 가능하게 남긴다.
- V-SC2d 는 `catalog → playback` 한 쌍만 시험했다. 8모듈 전체 조합(모든 금지 간선)을 다 돌린 것은 아니다 —
  `verify()` 가 선언을 읽어 판정한다는 메커니즘이 확인된 것이고, 조합 커버리지는 아니다.
  선언 자체의 존재는 `ModularityTests.everyModuleExplicitlyDeclaresItsBoundary` 가 상시 지킨다(01-02).
