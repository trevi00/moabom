---
phase: 01-foundation-guardrails
verified: 2026-08-06T12:00:00Z
status: human_needed
score: 4/4 roadmap 성공기준 verified (+ ARCH-01/ARCH-05 요구사항 2/2 satisfied)
overrides_applied: 0
human_verification:
  - test: "U-1. http://localhost:3000 육안 확인 — '모아봄' 제목 + 헬스 JSON 박스가 정상 렌더되는지"
    expected: "CSS 붕괴·폰트 누락·오버플로 없이 정상 렌더"
    why_human: "HTML 바이트/HTTP 200이 옳은 것과 브라우저 렌더가 옳은 것은 다르다 — curl로 대체 불가 (01-HUMAN-UAT.md U-1)"
  - test: "U-2. http://localhost:9090/targets 육안 확인 — moabom-app-container 가 초록인지"
    expected: "moabom-app-container up (green), moabom-app-host down은 설계상 정상임을 인지"
    why_human: "Prometheus UI 렌더 확인 + '의도된 down'과 '고장'을 구분하는 맥락 판단 (01-HUMAN-UAT.md U-2)"
  - test: "U-3. http://localhost:3002 Grafana 로그인 화면 육안 확인 (3001이 아니라 3002 — 타 프로젝트 컨테이너 점유로 인한 임시 재매핑)"
    expected: "로그인 화면 정상 렌더"
    why_human: "브라우저 렌더 확인 (01-HUMAN-UAT.md U-3)"
  - test: "U-4. 01-COUNTERFACTUAL-LOG.md 잔여 표기 동의 — '--no-verify 우회 가능 / 실 boundary는 CI 히스토리 스캔 / core.hooksPath 미설정 시 훅 자체가 없음'"
    expected: "operator가 이 정직 표기에 동의"
    why_human: "위협모델 수용 여부는 사람 판단 (오케스트레이터는 잠정 수용) (01-HUMAN-UAT.md U-4)"
  - test: "U-5. docs/adr/0001-runtime-versions.md §9 EOL 스택 수용 근거가 포트폴리오 리뷰어에게 설득력 있는지"
    expected: "청중(리뷰어) 관점에서 납득 가능"
    why_human: "기술 판정이 아니라 청중 판정 — 기계가 대신할 수 없음 (01-HUMAN-UAT.md U-5)"
---

# Phase 1: 기반 + 가드레일 Verification Report

**Phase Goal:** 전 스택이 한 커맨드로 기동하고, 모듈 경계·스키마 단일 진실·미디어 격리가 첫 커밋부터 자동으로 강제된다
**Verified:** 2026-08-06
**Status:** human_needed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths (ROADMAP 성공 기준 4종)

| # | Truth (ROADMAP SC) | Status | Evidence |
|---|---|---|---|
| 1 | 앱 이미지 선행 빌드 1회 후 `docker compose --profile full up` 한 커맨드로 6서비스(mysql·nginx·prometheus·grafana·app·web) 기동 + `:3000/api/health` 200 | ✓ VERIFIED (조건부 명기) | `01-COUNTERFACTUAL-LOG.md`/`01-HUMAN-UAT.md` — 실기동에서 `curl :3000/api/health` → `{"status":"UP","app":"moabom",...}` 확인, 2회 샘플 `ts` 상이(캐시 아님) 확인. compose.yaml 실측: mysql/nginx/prometheus/grafana 무조건, app/web `profiles: ["full"]` (6서비스 토폴로지 확인). **알려진 조건**: 로컬 3001 포트를 무관 컨테이너(exoplanet-grafana)가 점유해 `--wait` 가 저장소 밖 임시 오버레이 없이는 바인드 실패 — compose.yaml 자체의 결함이 아니라 환경 조건으로 STATE.md에 Phase 2 이월 명기됨 |
| 2 | Modulith 8모듈 `ApplicationModules.verify()` green + 경계 위반 커밋은 CI에서 실패 (반사실 증명) | ✓ VERIFIED | 실측: `package-info.java` 8개 + `@ApplicationModule` 8개 (그 중 4종 named interface + `RowProvider`). `01-COUNTERFACTUAL-LOG.md` V-SC2d — `catalog→playback::spi` 위반 주입 시 `verify()` 가 `Violations` 로 실패(exit 1, "Allowed targets: none"), 원복 후 vacuous-PASS 2갈래(증분 컴파일 산출물 제거 + `cleanTest --no-build-cache`) 배제하고 green 재확인. CI 첫 런(`01-CI-GATE-LOG.md`) 스텝 6이 `7 actionable tasks: 7 executed`(캐시 접미사 0) + Hikari/JPA 생명주기 로그로 실제 `verify()`+`SchemaMigrationTest` 실행을 증명 |
| 3 | 빈 DB + Flyway만으로 스키마+한글 골든 시드 재현, title.attribution/license 존재 | ✓ VERIFIED | 실측: `V1__init_catalog.sql:27` `attribution VARCHAR(300) NOT NULL`, `V2__seed_golden.sql` `CC-BY-3.0` 5건 + `FIXTURE-METADATA-ONLY` 5건. `FULLTEXT` 0건(V1/V2 둘 다) — P7 선점 금지 준수. `SchemaMigrationTest` (Testcontainers mysql:8.0) 로컬 10/10(4+6) green, CI 첫 런에서 HikariPool 열림/닫힘 로그로 실 DB 커넥션 확인 |
| 4 | 미디어 파일이 저장소 히스토리에 못 들어감 — 물리 격리 + `.gitignore` + pre-commit + CI 히스토리 스캔이 첫 push 이전 존재 (반사실 증명) | ✓ VERIFIED | 실측: `D:/moabom-media/{hls,source}` 존재, `.gitignore` 미디어 확장자+경로스코프 패턴 확인, `.githooks/pre-commit` (755, `core.hooksPath=.githooks`) 확인. `01-COUNTERFACTUAL-LOG.md` V-SC4d — 미디어 확장자·5MB 초과 2건 모두 커밋 거부(exit 1) 확인 + 귀속 오류 배제 arm. `push protection`은 **첫 push 이전**에 활성화됐음을 `01-08-SUMMARY.md` 결정 기록으로 확인. `gh api repos/trevi00/moabom` 실측 — `secret_scanning=enabled`, `push_protection=enabled`, `visibility=public`, `default_branch=main`. CI 히스토리 스캔 스텝은 `fetch-depth: 0`(체크아웃 로그에 `--depth` 플래그 부재로 실적용 확인)으로 43개 커밋 전체를 스캔 |

**Score:** 4/4 roadmap 성공기준 verified

### 비-요구사항 가드레일 산출물 4종 (ROADMAP 명시 — 누락 금지)

| 산출물 | 상태 | 증거 |
|---|---|---|
| ADR-0001 (Boot 3.2.12 EOL 근거+최종패치+non-blocking CVE) | ✓ 존재 | `docs/adr/0001-runtime-versions.md` (17.3K) |
| resilience4j 2.2.0 핀 + 회귀 게이트 | ✓ 존재+발화 확인 | `build.gradle:29,56` force 선언 + CI 스텝 "resilience4j pin gate" 실 출력 `io.github.resilience4j:...:2.2.0 ... Forced` |
| mysql 기동 파라미터(`ngram_token_size=2` 등) | ✓ 존재+대조 확인 | `compose.yaml:19` `--ngram_token_size=2`, `SchemaMigrationTest.java:27` 동일값 `withCommand`, CI 스텝 "mysql 기동 파라미터 대조" green |
| Windows 파일명 ASCII-safe 정책 | ✓ 존재 | `docs/adr/0002-media-and-encoding.md:51` "Windows 파일명 ASCII-safe 정책" 섹션 |

### Required Artifacts

| Artifact | Expected | Status | Details |
|---|---|---|---|
| `.gitignore` / `.gitattributes` / `.editorconfig` / `.githooks/pre-commit` / `README.md` | 미디어·시크릿 격리, eol=lf, 훅 배선, UX 3줄 | ✓ VERIFIED | 전량 존재·패턴 실측 일치 (Plan 01) |
| `backend/build.gradle` + 8모듈 `package-info.java` + `ModularityTests.java` | Gradle 8.14.5 + Modulith 경계 | ✓ VERIFIED | 8/8 모듈, resilience4j 2.2.0 force, ModularityTests 4종 |
| `frontend/next.config.ts` + `HealthProbe.tsx` + `Dockerfile` | 단일 오리진 rewrites + standalone + 컨테이너화 | ✓ VERIFIED | `BACKEND_ORIGIN`/`MEDIA_ORIGIN` env 기반 rewrites, `output: 'standalone'`, `fetch('/api/health')` 상대경로(CORS 구조적 불가) |
| `V1__init_catalog.sql` / `V2__seed_golden.sql` / `SchemaMigrationTest.java` | 스키마+시드 단일진실 | ✓ VERIFIED | attribution/license NOT NULL, CC-BY 5건, FULLTEXT 0건 |
| `compose.yaml` / `infra/nginx/default.conf` / `infra/prometheus/prometheus.yml` / `.env.example` | profiles 이원화 + mysql 파라미터 + nginx ro + prometheus 타깃 | ✓ VERIFIED | 4서비스(무프로필)/6서비스(full), `pull_policy: never`, `sendfile off` |
| `docs/adr/0001,0002` / `.github/workflows/ci.yml` / `.github/dependabot.yml` | EOL 근거 + CI 코어 게이트 + CVE 가시성 | ✓ VERIFIED | 게이트 8종(플랜 예상 7+mysql 대조 1) 전부 존재, `fetch-depth: 0` |
| `01-COUNTERFACTUAL-LOG.md` | V-SC2d/V-SC4d 반사실 프로브 실행기록 | ✓ VERIFIED | 커맨드·종료코드·오류원문 인용, vacuous-PASS 2갈래 배제, 귀속 오류 배제 arm |
| `01-CI-GATE-LOG.md` | CI 첫 런 게이트별 실행 증거 | ✓ VERIFIED | 스텝 16개 전부 success, 실행증거 3갈래(태스크회계·캐시접미사부재·Hikari로그) |

### Key Link Verification

| From | To | Via | Status | Details |
|---|---|---|---|---|
| `git commit` | `.githooks/pre-commit` | `core.hooksPath=.githooks` | ✓ WIRED | `git config --get core.hooksPath` → `.githooks` 실측 |
| `playback/package-info.java` | `com.moabom.catalog.spi` | `@ApplicationModule(allowedDependencies=...)` | ✓ WIRED | 8/8 package-info + ApplicationModule 실측, 반사실로 강제력 증명 |
| `frontend/app/HealthProbe.tsx` | `/api/health` | 브라우저 상대경로 fetch | ✓ WIRED | 실기동 curl 200 + 코드상 상대 URL(CORS 프리플라이트 구조적 불가) |
| `SchemaMigrationTest` | mysql 컨테이너 `ngram_token_size` | `withCommand` + `SELECT @@ngram_token_size` 단언 | ✓ WIRED | 코드/compose 문자열 일치 실측 + CI 대조 게이트 green |
| `.github/workflows/ci.yml` | `git rev-list --objects --all` | `fetch-depth: 0` 히스토리 전체 스캔 | ✓ WIRED | 체크아웃 로그에 `--depth` 부재 확인(전제 실적용) |
| 로컬 `main` | `github.com/trevi00/moabom` | `git push -u origin main` | ✓ WIRED | `git remote -v` 실측 일치, `gh api` 로 public/main/secret-scanning 확인 |
| `push` | `.github/workflows/ci.yml` | `on.push.branches:[main]` | ✓ WIRED | `gh run list` 최근 4런 전부 `event=push`, `conclusion=success` |

### Data-Flow Trace (Level 4)

이 phase는 골격/가드레일 단계로 사용자-가시 데이터 렌더링 화면이 `HealthProbe.tsx` 1개뿐이다. 그 데이터 흐름을 추적:

| Artifact | Data Variable | Source | Produces Real Data | Status |
|---|---|---|---|---|
| `frontend/app/HealthProbe.tsx` | `health` (useState) | `fetch('/api/health')` → `HealthController.health()` | 예 — `Instant.now()` 매 요청 상이한 `ts` (2회 샘플 상이값 확인, 캐시된 정적 응답 아님) | ✓ FLOWING |

### Behavioral Spot-Checks

무거운 재실행(gradle build 풀사이클, compose up)은 스킵 — 이미 `01-CI-GATE-LOG.md`(4회 CI 런, ubuntu-latest, 실측 로그 인용)와 `01-COUNTERFACTUAL-LOG.md`(로컬 실행 기록)가 동등한 강도의 실측 증거를 제공한다. 대신 정적/저비용 spot-check를 직접 재실행:

| Behavior | Command | Result | Status |
|---|---|---|---|
| 8모듈 package-info + @ApplicationModule 선언 수 | `ls .../*/package-info.java \| wc -l` + `grep -l ApplicationModule` | 8 / 8 | ✓ PASS |
| resilience4j 2.2.0 force 선언 | `grep resilience4j backend/build.gradle` | `2.2.0` force 확인 | ✓ PASS |
| title.attribution NOT NULL + FULLTEXT 0건 | `grep attribution V1` + `grep -ic FULLTEXT V1 V2` | NOT NULL 확인 / 0,0 | ✓ PASS |
| 미디어 루트 물리 존재 | `ls D:/moabom-media/` | `hls/`, `source/` | ✓ PASS |
| GitHub repo 보안 설정 | `gh api repos/trevi00/moabom` | public/main/secret-scanning enabled/push-protection enabled | ✓ PASS |
| 최근 CI 런 결론 | `gh run list --workflow=ci.yml --limit 5` | 4/4 `success` (push 트리거) | ✓ PASS |
| D-01 프로필 이원화 토폴로지 | `grep profiles: compose.yaml` | app/web만 `["full"]`, 나머지 무조건 (4/6 분할) | ✓ PASS |
| D-14 event_publication 미생성 | `grep -i event_publication V1/V2` | 주석에만 존재(P3 이월), DDL 없음 | ✓ PASS |
| 스텁/TODO 안티패턴 스캔 | `grep -rniE "TODO\|FIXME\|placeholder\|not implemented" backend/src frontend/app compose.yaml ci.yml` | 매치 0건 | ✓ PASS |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|---|---|---|---|---|
| ARCH-01 | 01-02, 01-06, 01-08 | Spring Modulith 6(+bff+shared=8)모듈 경계 + `ApplicationModules.verify()` CI 게이트 | ✓ SATISFIED | 8모듈 골격 + verify() green + 반사실 증명 + CI 실행 증명. REQUIREMENTS.md에 이미 `[x]`/`Complete` |
| ARCH-05 | 01-01, 01-03, 01-04, 01-05, 01-07, 01-08 | docker-compose 한 번으로 전 스택 기동 | ✓ SATISFIED (caveat) | full 프로필 6서비스 실기동 확인(01-07), CI가 clean ubuntu에서 compose config 유효성 상시 확인. 잔여: 로컬 3001 포트 충돌은 무관 컨테이너로 인한 환경 조건(compose.yaml 결함 아님), STATE.md에 명기. REQUIREMENTS.md에 이미 `[x]`/`Complete` |

**Orphaned requirements:** 없음 — REQUIREMENTS.md traceability 표에서 Phase 1에 매핑된 요구사항은 ARCH-01/ARCH-05 둘뿐이며 둘 다 전 플랜 frontmatter `requirements:` 필드에 나타남.

### Anti-Patterns Found (01-REVIEW.md 인용 — advisory, 비-블로커)

| File | Line | Pattern | Severity | Impact |
|---|---|---|---|---|
| `.githooks/pre-commit` | 13,20 | 따옴표 없는 `$staged` word-split — 파일명에 공백 있으면 5MB 게이트가 조용히 무력화 | ⚠️ Warning | defense-in-depth 층 한정, 실 boundary(CI blob 스캔)는 파일명 무관 영향 없음 |
| `.github/workflows/ci.yml` | 31 | CI 히스토리 확장자 스캔이 `.gitignore`/pre-commit보다 좁음(`mpg`/`mpeg`/`.ts` 경로스코프 누락) | ⚠️ Warning | 5MB 미만 해당 확장자 파일이 훅 우회 시 CI에서도 이름기반으로는 안 잡힘(크기기반은 여전히 잡음) |
| `frontend/.dockerignore` | — | `.env*` 미제외로 빌드 컨텍스트에 로컬 설정 유입 가능 | ⚠️ Warning | 현재 `.env` 값 자체는 비민감(엔드포인트 URL)이나 중간 레이어에 남을 수 있음 |
| `compose.yaml` | 61-91 | `app` healthcheck 부재로 `web`의 `depends_on`이 준비완료를 보장 못함 | ℹ️ Info | 일시적 프록시 실패 가능(자연 회복) |
| `.github/workflows/ci.yml` | 53-58 | 글롭 0건 매치 시 친절한 `::error::` 대신 원시 오류로 죽음 | ℹ️ Info | 차단 효과는 안전(빌드 red), 진단 메시지만 열화 |

이 5건은 `01-REVIEW.md`가 이미 식별·Fix 제안까지 남긴 것이며 Critical 0건이다(공격 표면 자체가 아직 없는 골격 단계). 본 검증에서 신규 발견한 anti-pattern은 없다(핵심 파일 재스캔에서 TODO/FIXME/placeholder/stub 매치 0건).

### 잠긴 결정(D-00~D-14) 존중 여부 spot-check

| 결정 | 확인 |
|---|---|
| D-00 (증설 미선반영) | compose.yaml에 Redis/read-replica 0건 확인 |
| D-01 (프로필 이원화) | mysql/nginx/prometheus/grafana 무조건, app/web만 `profiles:["full"]` |
| D-04 (모노레포 backend/+frontend/) | 디렉토리 구조 실측 일치 |
| D-07 (public repo) | `gh api` visibility=public |
| D-08 (main 직접 push, PR-only 아님) | default_branch=main, branch protection 404(01-CI-GATE-LOG.md) |
| D-10 (로컬 훅=미디어차단만, verify() 없음) | pre-commit 스크립트에 build/verify 호출 없음 (01-01-SUMMARY 결정 기록) |
| D-11 (P1 Flyway는 title 최소골격만) | migration 폴더에 V1/V2뿐, 타 도메인 테이블 0건 |
| D-14 (event_publication 미생성) | V1 주석에만 언급, DDL 없음 |

전부 존중됨 — 위반 0건.

### Human Verification Required

`01-HUMAN-UAT.md` (frontmatter `status: partial`)가 이미 이 목록을 대장으로 관리하고 있다. 01-07 Task 3 체크포인트는 오케스트레이터가 **HTTP 대리 검증**으로 조건부 승인했으나, "서버가 옳은 것을 돌려준다"와 "사람 눈에 그렇게 보인다"는 별개다(하네스 원칙: surface render PASS ≠ 실 동작 PASS). 기계로 닫힌 4개 항목(:3000 헬스 렌더 대리확인, CORS 프리플라이트 구조적 부재, Prometheus 타깃 up, Grafana `/api/health` 200)은 대리 검증으로 충분히 강하나, 아래 5항목은 육안·청중 판단이 본질이라 대체 불가:

1. **U-1 — `:3000` 육안 렌더 확인**
   **Test:** 브라우저로 http://localhost:3000 방문
   **Expected:** "모아봄" 제목 + 헬스 JSON이 CSS 붕괴/오버플로 없이 정상 렌더
   **Why human:** HTML 바이트가 옳은 것과 렌더가 옳은 것은 다르다

2. **U-2 — `:9090/targets` 육안 확인**
   **Test:** Prometheus UI에서 `moabom-app-container` 상태 확인
   **Expected:** 초록(up), `moabom-app-host`가 빨강(down)인 것은 설계상 정상임을 인지
   **Why human:** UI 렌더 + 맥락적 정상/고장 구분

3. **U-3 — `:3002` Grafana 로그인 화면 확인** (주의: `:3001`이 아님 — 타 프로젝트 컨테이너 점유로 인한 임시 재매핑)
   **Test:** 브라우저로 로그인 화면 렌더 확인
   **Expected:** 정상 렌더
   **Why human:** 브라우저 렌더 확인

4. **U-4 — `01-COUNTERFACTUAL-LOG.md` 잔여 표기 동의**
   **Test:** "--no-verify 우회 가능/실 boundary=CI 히스토리 스캔/훅 미설정 시 훅 없음" 3줄 검토
   **Expected:** operator 동의
   **Why human:** 위협모델 수용은 사람 판단 (오케스트레이터는 잠정 수용만 했음)

5. **U-5 — ADR-0001 §9 EOL 근거 설득력 판단**
   **Test:** 포트폴리오 리뷰어 관점에서 문서 검토
   **Expected:** 납득 가능
   **Why human:** 청중 판정 — 기계 대체 불가

### Gaps Summary

코드 레벨 gap은 없다. 4개 roadmap 성공기준과 ARCH-01/ARCH-05 요구사항 모두 온디스크 실측 + 반사실 프로브 + CI 실행 증거로 강하게 뒷받침된다. Anti-pattern 5건은 REVIEW.md가 이미 식별한 advisory 항목(Critical 0)이며 실 boundary(CI 블롭 스캔, 게이트 자체의 red/green 판정)를 무력화하지 않는다. 알려진 조건부/이월 사항(3001 포트 충돌·8080 이중 LISTEN·CI 2회차 캐시 구멍·LICENSE 부재)은 모두 `01-HUMAN-UAT.md`/`01-CI-GATE-LOG.md`/`.planning/STATE.md`에 정직하게 문서화되어 있고 Phase 2/operator로 명시 이월되어 있어 본 검증에서 신규 gap으로 재발명하지 않았다.

유일하게 남은 것은 `01-HUMAN-UAT.md` 자신이 `status: partial`로 표기한 U-1~U-5(육안 렌더 3건 + 위협모델/설득력 판단 2건)이며, 이는 정의상 자동화로 닫을 수 없는 항목이다. 이것이 이 보고서의 status를 `human_needed`로 만드는 유일한 사유다.

---

_Verified: 2026-08-06_
_Verifier: Claude (kha-verifier)_
