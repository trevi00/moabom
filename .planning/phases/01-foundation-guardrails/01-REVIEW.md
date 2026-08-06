---
phase: 01-foundation-guardrails
reviewed: 2026-08-06T00:00:00Z
depth: standard
files_reviewed: 39
files_reviewed_list:
  - .editorconfig
  - .env.example
  - .gitattributes
  - .githooks/pre-commit
  - .github/dependabot.yml
  - .github/workflows/ci.yml
  - .gitignore
  - README.md
  - backend/build.gradle
  - backend/gradle.properties
  - backend/gradle/wrapper/gradle-wrapper.properties
  - backend/gradlew
  - backend/gradlew.bat
  - backend/settings.gradle
  - backend/src/main/java/com/moabom/MoabomApplication.java
  - backend/src/main/java/com/moabom/bff/HealthController.java
  - backend/src/main/java/com/moabom/recommendation/row/RowProvider.java
  - backend/src/main/resources/application.yaml
  - backend/src/main/resources/db/migration/V1__init_catalog.sql
  - backend/src/main/resources/db/migration/V2__seed_golden.sql
  - backend/src/test/java/com/moabom/ModularityTests.java
  - backend/src/test/java/com/moabom/SchemaMigrationTest.java
  - compose.yaml
  - docs/adr/0001-runtime-versions.md
  - docs/adr/0002-media-and-encoding.md
  - frontend/.dockerignore
  - frontend/.gitignore
  - frontend/AGENTS.md
  - frontend/CLAUDE.md
  - frontend/Dockerfile
  - frontend/app/HealthProbe.tsx
  - frontend/app/globals.css
  - frontend/app/layout.tsx
  - frontend/app/page.tsx
  - frontend/next.config.ts
  - frontend/package.json
  - frontend/tsconfig.json
  - infra/nginx/default.conf
  - infra/prometheus/prometheus.yml
findings:
  critical: 0
  warning: 3
  info: 2
  total: 5
status: issues_found
---

# Phase 01: Code Review Report

**Reviewed:** 2026-08-06
**Depth:** standard
**Files Reviewed:** 39
**Status:** issues_found

## Summary

Phase 1(기반+가드레일)의 39개 소스 파일을 파일별 정독 + 설정 간 정합성 대조(compose ↔ CI ↔ pre-commit ↔ gitignore, application.yaml ↔ compose env, nginx ↔ compose 마운트, Next rewrites ↔ nginx/backend)로 리뷰했다. Critical 등급 결함은 없다 — Phase 1은 인증·사용자 입력·외부 노출 표면이 아직 없는 골격 단계라 공격 표면 자체가 작다.

발견된 결함은 모두 **문서가 스스로 선언한 "가드레일" 계약과 실제 구현 사이의 미세한 드리프트**다:

- 미디어/대용량 가드레일 4중 방어 중 **층 3(pre-commit)이 공백 포함 파일명에서 조용히 무력화**된다 (실 boundary인 층 4/CI는 영향 없음).
- **층 4(CI 히스토리 스캔)의 확장자 목록이 층 2(.gitignore)·층 3(pre-commit)보다 좁다** — `mpg`/`mpeg`/`ts` 누락.
- `frontend/.dockerignore`가 `.gitignore`와 달리 `.env*`를 제외하지 않아, Docker 빌드 컨텍스트로 로컬 설정 파일이 유입될 수 있는 경로가 열려 있다.

나머지 2건은 Info 등급 견고성 제안이다. 알려진/의도된 사항(EOL 스택 수용, CI 캐시 구멍, 8080 이중 LISTEN, admin/admin 로컬 기본값, `--no-verify` 우회, event_publication 부재 등)은 컨텍스트에 따라 재보고하지 않았다.

## Warnings

### WR-01: pre-commit 훅의 워드 스플릿으로 5MB 크기 게이트가 공백 파일명에서 무력화됨

**File:** `.githooks/pre-commit:13,20`
**Issue:**
```sh
staged=$(git diff --cached --name-only --diff-filter=ACMR)
...
for f in $staged; do          # line 13 — 따옴표 없는 전개, IFS 기본값(공백+개행)으로 분리
  ...
  size=$(git cat-file -s ":$f" 2>/dev/null || echo 0)   # line 20
```
`$staged`가 따옴표 없이 전개되므로 파일명에 **공백**이 포함되면 `for` 루프가 단어 단위로 쪼갠다. 예를 들어 스테이징된 `big archive.zip`(8MB)은 `f="big"`, `f="archive.zip"` 두 조각으로 나뉜다. 두 조각 모두 실제 git 인덱스 경로가 아니므로 `git cat-file -s ":$f"`가 실패하고, `2>/dev/null || echo 0` 폴백에 의해 **size가 조용히 0으로 처리**된다 — 8MB 파일이 5MB 게이트를 완전히 우회해 커밋된다. (확장자 매치는 마지막 조각에 확장자가 남는 경우가 많아 상대적으로 덜 취약하지만, 크기 게이트는 파일명에 공백이 있는 모든 대용량 파일에 대해 무력화된다.)

잔여 위험은 비교적 낮다 — ADR-0002가 명시하듯 이 계층은 defense-in-depth이고, 실 boundary는 CI의 `git rev-list --objects --all` 기반 blob 크기 스캔(파일명과 무관하게 객체 크기만 봄)이라 push 시점에는 여전히 잡힌다. 다만 "로컬에서 값싸게 잡는다"는 층 3의 존재 목적 자체가 이 조건에서 소멸한다.

**Fix:**
```sh
staged=$(git diff --cached --name-only --diff-filter=ACMR)
[ -z "$staged" ] && exit 0

OLDIFS=$IFS
IFS='
'
for f in $staged; do
  ...
done
IFS=$OLDIFS
```
(`#!/bin/sh` POSIX 호환을 유지하려면 `IFS=$'\n'` 대신 리터럴 개행을 담은 대입을 쓴다. 개행 자체가 포함된 파일명은 여전히 엣지케이스로 남지만 공백 포함 파일명은 이걸로 해결된다.)

### WR-02: CI 히스토리 미디어 확장자 스캔이 pre-commit/.gitignore보다 좁아 mpg·mpeg·ts를 놓침

**File:** `.github/workflows/ci.yml:31` (vs `.githooks/pre-commit:6`, `.gitignore:7-18`)
**Issue:** 세 계층의 미디어 확장자 목록이 서로 다르다.
- `.gitignore` (7-18행): `mp4 mkv mov avi webm m4s flv wmv mpg mpeg` + 경로 스코프 `media/**/*.ts`, `**/hls/**/*.ts`
- `.githooks/pre-commit` (6행) `MEDIA_RE`: `mp4|mkv|mov|avi|webm|m4s|flv|wmv|mpg|mpeg` + 별도 `PATH_TS_RE`
- `.github/workflows/ci.yml` (31행) `hits` 정규식: `\.(mp4|mkv|mov|avi|webm|m4s|flv|wmv)$` — **`mpg`, `mpeg` 누락**, `.ts`(MPEG-TS) 경로 스코프 검사 자체가 아예 없음

ADR-0002 §1은 "층 4(CI 히스토리 스캔)가 실 boundary이고 층 3(로컬 훅)은 defense-in-depth일 뿐"이라고 명시한다. 그런데 정작 실 boundary 쪽의 이름 기반 검사가 로컬 훅보다 **더 좁다**. 크기 기반 스캔(`big=...`, 30행 이전)은 확장자와 무관하게 전체 blob을 5MB 기준으로 훑으므로 큰 파일은 여전히 잡히지만, 5MB 미만의 `.mpg`/`.mpeg`/`.ts` 파일이 (a) `core.hooksPath` 미설정 클론에서 훅이 아예 안 돌거나 (b) `--no-verify`로 우회되어 히스토리에 들어간 경우, CI 이름 기반 스캔도 이를 못 잡는다 — 문서가 주장하는 "실 boundary"에 실제 구멍이 있다. HLS 세그먼트(`.ts`)는 개별로는 5MB 미만인 경우가 흔해 이 시나리오의 현실성이 낮지 않다.

**Fix:**
```sh
hits=$(git log --all --diff-filter=A --name-only --pretty=format: | sort -u \
  | grep -Ei '\.(mp4|mkv|mov|avi|webm|m4s|flv|wmv|mpg|mpeg)$' || true)
ts_hits=$(git log --all --diff-filter=A --name-only --pretty=format: | sort -u \
  | grep -E '(^|/)(media|transcode-tmp|uploads|hls)/.*\.ts$' || true)
if [ -n "$hits" ] || [ -n "$ts_hits" ]; then
  echo "$hits" "$ts_hits"; echo "::error::미디어 파일이 커밋된 적이 있습니다"; exit 1
fi
```

### WR-03: `frontend/.dockerignore`가 `.env*`를 제외하지 않아 로컬 설정이 빌드 컨텍스트로 유입 가능

**File:** `frontend/.dockerignore`
**Issue:** `frontend/.gitignore`(34행 `.env*`)와 README(84-86행)가 명시하듯 `frontend/.env`는 `BACKEND_ORIGIN`/`MEDIA_ORIGIN` 등 로컬 오버라이드를 담을 수 있는 실재 파일이다. 그런데 `frontend/.dockerignore`는 `node_modules`, `.next`, `.git`, `Dockerfile`, `.dockerignore`, `npm-debug.log`만 제외하고 `.env*` 패턴이 없다. `Dockerfile`의 `build` 스테이지가 `COPY . .`(19행)로 빌드 컨텍스트 전체를 복사하므로, 로컬에 `frontend/.env`가 존재하는 상태로 `docker build`/`docker compose --profile full up`을 실행하면 그 파일이 `build` 스테이지 레이어에 그대로 들어간다. 최종 `runtime` 스테이지는 `.next/standalone` 등 선별 복사라 완제품 이미지엔 직접 노출되지 않지만, 중간 레이어(`docker history`/캐시/레지스트리 push 시)로는 값이 남는다. 현재 `.env`에 담기는 값 자체는 비민감(엔드포인트 URL)이지만, 프로젝트 전반이 `.env` 취급에 엄격한 것과 대비해 이 지점만 비어 있다.

**Fix:**
```
# frontend/.dockerignore 에 추가
.env
.env.local
.env.*.local
*.pem
*.key
```

## Info

### IN-01: `compose.yaml`의 `app` 서비스에 healthcheck가 없어 `web`의 `depends_on`이 "준비됨"을 보장하지 않음

**File:** `compose.yaml:61-77` (app), `79-91` (web)
**Issue:** `mysql` 서비스는 `healthcheck` + `app`의 `depends_on: { mysql: { condition: service_healthy } }`로 기동 순서를 엄밀히 보장한다(31-36행, 68-69행). 반면 `app` 서비스는 healthcheck가 없고 `web`의 `depends_on: [app]`(90행)은 기본 조건인 "컨테이너 시작됨"만 보장한다 — Spring Boot 앱이 실제로 요청을 받을 준비가 됐는지는 보장하지 않는다. `management.endpoint.health.probes.enabled: true`(application.yaml)로 이미 readiness 프로브가 노출되어 있으므로 활용 여지가 있다. `docker compose --profile full up` 직후 `:3000`에 바로 접근하면 `/api/*` 프록시가 일시적으로 실패할 수 있다(자연 회복되긴 함).

**Fix:** `app`에 actuator 헬스 엔드포인트 기반 healthcheck 추가하고 `web`의 `depends_on`을 조건부로 승격 (Paketo run 이미지에 `wget`/`curl` 존재 여부는 실측 필요 — 없으면 셸 없는 헬스체크 대안 검토):
```yaml
app:
  healthcheck:
    test: ["CMD", "wget", "-qO-", "http://localhost:8080/actuator/health"]
    interval: 5s
    timeout: 5s
    retries: 20
    start_period: 30s
web:
  depends_on:
    app: { condition: service_healthy }
```

### IN-02: CI 모듈 커버리지 체크가 글롭 매치 0건일 때 친절한 `::error::` 없이 원시 오류로 죽음

**File:** `.github/workflows/ci.yml:53-58`
**Issue:**
```sh
n=$(ls backend/src/main/java/com/moabom/*/package-info.java | wc -l)
[ "$n" -eq 8 ] || { echo "::error::모듈 package-info 가 8개가 아닙니다 (실측 $n)"; exit 1; }
m=$(grep -l 'ApplicationModule' backend/src/main/java/com/moabom/*/package-info.java | wc -l)
```
GitHub Actions의 `run:` 스텝 기본 셸은 `bash --noprofile --norc -eo pipefail`이다. 만약 회귀로 `package-info.java`가 전부 삭제되어 글롭이 0건 매치하면, `ls`(또는 `grep`)가 비영(非零) 종료 코드를 내고 `pipefail`로 인해 `n=$(... | wc -l)` 대입 자체가 실패로 간주되어 `set -e`가 즉시 스크립트를 중단시킨다 — 의도한 `::error::모듈 package-info 가 8개가 아닙니다` 메시지 대신 `ls: cannot access ...` 류의 원시 오류로 죽는다. 게이트가 여전히 실패(빌드 red)하므로 **차단 효과 자체는 안전**하지만, 실패 원인 진단이 원래 설계한 메시지보다 나빠진다.

**Fix:**
```sh
n=$(ls backend/src/main/java/com/moabom/*/package-info.java 2>/dev/null | wc -l || true)
[ "$n" -eq 8 ] || { echo "::error::모듈 package-info 가 8개가 아닙니다 (실측 $n)"; exit 1; }
```
(핵심은 글롭/grep이 매치 0건일 때도 `set -e -o pipefail`이 조기 종료하지 않도록 실패를 명시적으로 흡수하는 것.)

---

_Reviewed: 2026-08-06_
_Reviewer: Claude (kha-code-reviewer)_
_Depth: standard_
