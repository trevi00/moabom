---
phase: 01-foundation-guardrails
plan: 01
subsystem: infra
tags: [git, gitignore, gitattributes, editorconfig, pre-commit-hook, hls, mpeg-ts, docker-compose, licensing]

# Dependency graph
requires: []
provides:
  - "저장소 밖 미디어 루트 D:/moabom-media/{hls,source} (SC4 1차 방어선 — 물리 격리)"
  - ".gitignore 미디어/시크릿 격리 패턴 (경로 스코프 MPEG-TS, 전역 *.ts 부재)"
  - ".gitattributes eol=lf 정규화 (gradlew CRLF → ubuntu CI 사망 차단, CP-5)"
  - ".editorconfig charset=utf-8 (가드레일 G4)"
  - ".githooks/pre-commit 미디어 확장자 10종 + 5MB 게이트, exec bit 100755, core.hooksPath 배선"
  - "README — D-03 표준 커맨드 3줄 UX + SC1 정직 2단계 표기 + 상표/CC BY 3.0 고지"
affects: [01-02-modulith-skeleton, 01-03-compose, 01-05-env-example, 01-06-ci, 01-07-verification, 01-08-github-repo]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "git 훅 배포 = .githooks/ + core.hooksPath (훅이 버전관리 대상이 된다)"
    - "미디어 격리 3중 방어: 물리 격리 → .gitignore → pre-commit (실 boundary 는 Plan 06 CI 히스토리 스캔)"
    - "게이트는 라벨이 아니라 결과로 판정 — git check-ignore 반사실 프로브(TypeScript 비매칭)를 acceptance 에 고정"

key-files:
  created:
    - .gitignore
    - .gitattributes
    - .editorconfig
    - .githooks/pre-commit
    - README.md
  modified: []

key-decisions:
  - "전역 *.ts 패턴 금지 — MPEG-TS 세그먼트와 TypeScript 확장자 충돌(CP-1). 경로 스코프(media/**/*.ts, **/hls/**/*.ts) + 5MB 크기 게이트로 대체"
  - "pre-commit 훅 주석에서 verify() 문자열 제거 — 플랜의 acceptance criterion(grep -c 'verify()' == 0)과 action 텍스트가 상호 모순. D-10 의미(훅에 빌드 없음)는 그대로 보존"
  - "ARCH-05 를 mark-complete 하지 않음 — 7개 플랜이 공유하는 요구사항이고 compose 파일이 아직 없다. 마지막 기여 플랜(01-08)에서 표기"

patterns-established:
  - "정직 표기: '첫 커밋 이전'이 아니라 '가드레일이 첫 push 이전에 존재한다' (planning 커밋 5개 선행이라 사후 만족 불가)"
  - "게이트 고지 의무: 훅이 스스로 --no-verify 우회 가능성과 그 비용을 출력한다"

requirements-completed: []
requirements-contributed: [ARCH-05]

# Metrics
duration: 9 min
completed: 2026-08-06
---

# Phase 1 Plan 01: 저장소 가드레일 + 미디어 격리 Summary

**MPEG-TS↔TypeScript 확장자 충돌을 피한 경로 스코프 .gitignore + 5MB pre-commit 게이트(core.hooksPath 배선, exec bit 100755) + 저장소 밖 미디어 루트 + D-03 표준 커맨드 3줄 README**

## Performance

- **Duration:** 약 9분 (03:49Z 착수 추정 ~ 03:58Z)
- **Started:** 2026-08-06T03:49Z (근사 — 오케스트레이터 spawn 직후)
- **Completed:** 2026-08-06T03:58:40Z
- **Tasks:** 3/3
- **Files created:** 5 (+ 저장소 밖 디렉터리 2개)

## Accomplishments

- **미디어 격리 3중 방어 착지.** (1) 미디어 루트 `D:/moabom-media/{hls,source}` 를 저장소 밖에 물리 생성, (2) `.gitignore` 경로 스코프 패턴, (3) `.githooks/pre-commit` 확장자 10종 + 5MB 게이트. 실 boundary(CI 히스토리 스캔)는 Plan 06 소관임을 훅·README·본 문서가 동일하게 고지.
- **CP-1 회귀 게이트를 반사실 프로브로 고정.** `git check-ignore -q frontend/next.config.ts` 가 **non-zero 여야 한다**는 조건을 acceptance 에 넣어, "미디어를 잘 막는가"뿐 아니라 "소스를 잘못 삼키지 않는가"를 같이 판정한다. 전역 `*.ts` 부재는 `grep -c '^\*\.ts$' == 0` 으로 기계 검사.
- **CP-5(ubuntu CI 사망) 선제 차단.** 이 머신은 `core.autocrlf=true`(실측 재확인). `.gitattributes` 의 `* text=auto eol=lf` + `gradlew text eol=lf` 로 `gradlew` 가 CRLF 로 커밋되는 경로를 미리 닫았다. 훅 파일은 CRLF 0줄로 확인.
- **훅이 실제 발화하는 상태로 배선.** `core.hooksPath=.githooks` 설정 후 Task 2·3 커밋이 훅을 통과 — 훅이 존재만 하고 문법 오류로 죽는 상태가 아님을 `sh -n` + 실 커밋 2회로 확인.
- **README 가 UX 계약을 못박음.** `up`/`down`/`down -v` 3줄 + 래퍼 스크립트 부재를 본문에 명시(D-03), SC1 을 "이미지 빌드 1회 후 compose 한 커맨드"로 정직하게 2단계 표기(CP-3 A안), Netflix 무관 고지 + Blender CC BY 3.0 표기(Pitfall 17).

## Task Commits

1. **Task 1: .gitignore / .gitattributes / .editorconfig + 저장소 밖 미디어 루트** — `3982e76` (chore) — acceptance 12/12
2. **Task 2: pre-commit 훅 + core.hooksPath 배선** — `2dc1dbc` (chore) — acceptance 10/10 (플랜 9종 + `sh -n` 자체 추가)
3. **Task 3: README 3줄 UX + 선행 빌드 + 상표·라이선스 고지** — `74c1ed2` (docs) — acceptance 12/12

## Files Created/Modified

- `.gitignore` — 미디어 확장자 10종 + `media/`·`transcode-tmp/`·`uploads/` 디렉터리 + 경로 스코프 MPEG-TS + 시크릿(`.env`, `*.pem`, `*.key`, `*.p12`) + 빌드 산출물 + IDE/OS
- `.gitattributes` — `* text=auto eol=lf`, `gradlew` LF / `*.bat`·`gradlew.bat` CRLF, 미디어·jar binary
- `.editorconfig` — `charset = utf-8`, LF, indent 4 (TS/JS/JSON/YAML/MD 는 2), `*.bat` 만 CRLF
- `.githooks/pre-commit` — 스테이징 파일의 미디어 확장자(대소문자 무시) + 경로 스코프 `.ts` + 5MB 초과 차단. 우회 경로와 그 비용을 스스로 출력
- `README.md` — 소개/상표 고지, 콘텐츠 라이선스, Quick start 3줄, full 프로필 데모(선행 빌드 1줄), 평시 개발, 클론 후 1회, 미디어 루트 + 3중 방어 표, 진화 서사

## Decisions Made

1. **전역 `*.ts` 금지, 경로 스코프로 대체** — 플랜/CP-1 지시 그대로. `frontend/` 가 같은 저장소에 살기 때문에 전역 배제는 Next.js 소스를 조용히 삼킨다. 크기 게이트가 세그먼트에 대한 2차 방어.
2. **훅 주석에서 `verify()` 문자열 제거** — 아래 Deviation 1. 의미 보존, 게이트 통과.
3. **`requirements mark-complete ARCH-05` 미실행** — 아래 Deviation 2.
4. **`sh -n` 문법 검사를 acceptance 에 자체 추가** — "게이트의 존재는 게이트의 작동이 아니다"라는 플랜 문구를 값싼 범위에서 한 칸 좁힘. 실 차단 동작 증명은 플랜 지시대로 Plan 07 반사실 프로브에 남겨둠.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] 플랜 내부 모순 — 훅 주석의 `verify()` 가 자기 acceptance 를 실패시킴**

- **Found during:** Task 2 (pre-commit 훅)
- **Issue:** 플랜 `<action>` 이 지시한 훅 주석 2행은 `# ... verify()/빌드는 CI 소관 ...` 인데, 같은 태스크의 acceptance criterion 은 `grep -c 'gradlew\|gradle build\|verify()' .githooks/pre-commit` 이 `0` 을 출력할 것을 요구한다. 지시대로 쓰면 주석 때문에 `1` 이 나와 게이트가 반드시 실패한다.
- **Fix:** 주석을 `# D-10: 미디어 확장자 + 대용량 파일만 차단. 모듈 경계 검증·빌드는 CI 소관 (로컬 훅에 태우지 않는다).` 로 조정. criterion 의 **의도**(훅이 빌드/모듈검증을 실행하지 않는다)는 그대로이며, 훅 본문에는 애초에 빌드 호출이 없다.
- **Files modified:** `.githooks/pre-commit`
- **Verification:** `grep -c 'gradlew\|gradle build\|verify()' .githooks/pre-commit` → `0`. 나머지 9개 criterion 도 동시 통과(10/10).
- **Committed in:** `2dc1dbc`

**2. [Rule 3 - Blocking] ARCH-05 조기 완료 표기 회피 (CLAUDE.md 정직 표기 원칙 우선)**

- **Found during:** 플랜 종료 시 `requirements mark-complete` 단계
- **Issue:** 이 플랜의 frontmatter 는 `requirements: [ARCH-05]` 지만, ARCH-05("docker-compose 한 번으로 전 스택 기동")는 **Phase 1 의 7개 플랜(01-01·03·04·05·06·07·08)이 공유**한다. 현재 `compose.yaml` 은 존재조차 하지 않는다. 표준 흐름대로 `mark-complete ARCH-05` 를 실행하면 REQUIREMENTS.md 에 `[x]` + `Complete` 가 찍혀 **기동되지 않는 스택을 기동된다고 선언**하게 된다.
- **Fix:** `mark-complete` 미실행. SUMMARY frontmatter 를 `requirements-completed: []` + `requirements-contributed: [ARCH-05]` 로 분리 표기. ARCH-05 체크는 마지막 기여 플랜(01-08) 또는 Plan 07 검증 통과 시점에 표기해야 한다.
- **근거:** CLAUDE.md(L0) "정량 표현 — 절대 단언 금지, 알려진 결함·잔여 리스크 항상 표기" + executor 지침의 CLAUDE.md 우선 규칙. 실측 확인: `requirements-completed` 필드는 `gsd-tools.cjs` 어디에서도 파싱되지 않아(grep 0건) 기계 계약을 깨지 않는다.
- **Verification:** `.planning/REQUIREMENTS.md` line 95/208 이 `[ ] ARCH-05` / `Pending` 으로 유지됨.
- **Committed in:** 본 SUMMARY 커밋 (코드 변경 없음)

**3. [Rule 2 - Missing Critical] README 의 `.env.example` 부재 정직 표기**

- **Found during:** Task 3 (README)
- **Issue:** 플랜이 지시한 "클론 후 1회" 블록은 `cp .env.example .env` 를 안내하는데, `.env.example` 은 Plan 05 산출물이라 현시점 존재하지 않는다. 그대로 두면 첫 클론 사용자가 실패하는 명령을 실행한다.
- **Fix:** 해당 블록 아래 한 줄 추가 — "`.env.example` 은 compose·앱 설정이 들어오는 단계에서 추가됩니다. 그 전까지 두 번째 줄은 건너뛰어도 됩니다."
- **Files modified:** `README.md`
- **Verification:** Task 3 acceptance 12/12 유지(추가 문장이 어떤 criterion 도 깨지 않음).
- **Committed in:** `74c1ed2`

**4. [Rule 1 - Bug] `state update-progress` 가 STATE.md 본문 진행 바를 갱신하지 않음**

- **Found during:** 플랜 종료 시 STATE 갱신
- **Issue:** `gsd-tools state update-progress` 가 `{"updated": true, "bar": "[█░░░░░░░░░] 13%"}` 를 반환하고 **frontmatter 는 갱신**하지만, 본문 `Progress: [░░░░░░░░░░] 0%` 라인은 그대로 남았다. 재실행해도 동일 → 순서 문제가 아니라 도구가 본문을 쓰지 않는다. 도구가 보고하는 라벨(`updated: true`)과 실제 결과가 불일치한다.
- **Fix:** 하네스(`C:/Users/user/.claude/get-shit-done`)는 이 프로젝트 스코프 밖이라 손대지 않았다. 이 저장소의 산출물인 `.planning/STATE.md` 본문 1줄만 실측값(13%)으로 정정하고, `Last activity` 에 유실된 설명을 복원했다.
- **Files modified:** `.planning/STATE.md`
- **Verification:** frontmatter(`completed_plans: 1`, `percent: 13`) 와 본문 바가 일치. 디스크 실측(SUMMARY 1개 / PLAN 8개)과도 일치.
- **Committed in:** 본 SUMMARY 커밋
- **잔여:** 도구 자체 결함은 미수정 — 다음 플랜에서도 재발한다. 하네스 백로그 후보.

**5. [Rule 1 - Bug] `roadmap update-plan-progress` 가 Progress 표 행을 갱신하지 않음 (동형 결함 2회 = 2-Strike)**

- **Found during:** 플랜 종료 시 ROADMAP 갱신
- **Issue:** 도구가 `{"updated": true, "summary_count": 1, "status": "In Progress"}` 를 반환하고 **플랜 체크박스(`- [ ] 01-01-PLAN.md` → `- [x]`)는 실제로 바꿨지만**, 워크플로 문서가 약속한 "Progress 표 행 갱신"은 하지 않았다 — 표는 `| 1. 기반 + 가드레일 | 0/8 | Planned | - |` 그대로.
- **반사실 판별:** 호출 인자 형태 문제인지 확인하기 위해 워크플로 문서형(`update-plan-progress 01`, 인자 1개)으로 재호출 → `"updated": true` 를 반환했으나 `git status` 는 **무변경**(파일을 아예 쓰지 않음). 즉 호출 형태가 아니라 도구 결함이다.
- **Fix:** 하네스는 스코프 밖이라 미수정. 이 저장소의 `.planning/ROADMAP.md` 표 행만 디스크 실측값(PLAN 8 / SUMMARY 1)으로 `1/8 · In Progress` 정정.
- **Files modified:** `.planning/ROADMAP.md`
- **Verification:** 표 행이 체크박스 상태(`- [x] 01-01-PLAN.md` 1건) 및 STATE frontmatter(`completed_plans: 1`)와 일치.
- **패턴:** Deviation 4 와 **동형** — 두 도구 모두 `updated: true` 라벨을 반환하면서 본문/표를 쓰지 않는다. 같은 유형 2회 = 하네스 2-Strike 대상. 다음 플랜들은 도구 출력의 `updated: true` 를 신뢰하지 말고 **`git status` 로 실 변경을 확인**해야 한다.

---

**Total deviations:** 5 auto-fixed (2 blocking, 1 missing critical, 2 bug)
**Impact on plan:** 스코프 확대 없음. 1·2 는 플랜/워크플로 텍스트가 서로 모순되는 지점을 의도 보존 방향으로 푼 것이고, 3 은 문서 1줄이다. 잠긴 결정(D-03·D-10·CP-1·CP-3·CP-5) 은 전부 그대로 집행됐다.

## Issues Encountered

- **워킹트리가 dirty 한 상태로 착수** — `.planning/STATE.md`, `.planning/config.json` (오케스트레이터가 실행 시작 직전 갱신), `.planning/_graph/` (planner 산출물). 사용자 코드가 아니고, 모든 커밋을 파일 단위 스테이징(`git add .` 미사용)했으므로 태스크 커밋에 섞이지 않았다. `git checkout --` 류 롤백은 수행하지 않았다.
- **알려진 결함 (잔여 노름, 비-제로):**
  1. 훅의 `for f in $staged` 는 **파일명에 공백이 있으면 분해된다** (플랜이 지시한 원문 그대로 유지). 이 저장소의 Windows 파일명 ASCII-safe 정책과 상충하지 않아 방치했으나, 공백 포함 미디어 파일이 스테이징되면 차단을 우회할 수 있다. Plan 07 반사실 프로브에서 공백 파일명 케이스를 넣으면 값싸게 드러난다.
  2. 훅의 **실 차단 동작은 아직 증명되지 않았다** — 존재·문법·배선만 검증했다. 플랜이 명시적으로 Plan 07 Task 1(V-SC4d)에 위임한 항목이다.
  3. `.gitignore` 는 **이미 tracked 인 파일에는 무력**하다. 현재 tracked 목록에 미디어가 0건임은 히스토리 스캔으로 확인했다(5MB 초과 blob 0, 미디어 확장자 추가 이력 0).
  4. 코드 라이선스(LICENSE 파일)가 아직 없다 — D-07 public repo 전에 결정이 필요하다. 본 플랜 스코프 밖이며 Plan 08(저장소 생성) 후보.
  5. `gsd-tools` 진행 갱신 결함 2종(Deviation 4·5)은 **도구 쪽에 그대로 남아 있다** — `state update-progress` 는 STATE 본문 바를, `roadmap update-plan-progress` 는 ROADMAP Progress 표를 쓰지 않으면서 둘 다 `updated: true` 를 반환한다. 다음 플랜들은 진행률을 **디스크 실측(SUMMARY 파일 수)** 으로 판정하고, 도구 실행 후 `git status` 로 실 변경을 확인할 것.

## User Setup Required

None — 이 플랜은 외부 서비스 설정을 요구하지 않는다.
단, **다른 머신에서 클론할 경우** `git config core.hooksPath .githooks` 1회 실행이 필요하다 (README 명시). 이 설정은 저장소가 아니라 로컬 git config 라 클론을 따라오지 않는다.

## Next Phase Readiness

- **Plan 01-02 (Modulith 8모듈 골격) 착수 가능** — 의존 산출물 없음(wave 1 병렬).
- **Plan 01-03(compose)·01-05(.env.example) 이 이 플랜의 전제를 소비한다:** `.gitignore` 의 `.env` 배제가 이미 유효하므로 `.env.example` 만 커밋되는 구조가 성립한다. `MEDIA_ROOT` 기본값은 `D:/moabom-media` 를 가리켜야 README 와 정합한다.
- **Plan 01-06 (CI) 가 실 boundary 를 만든다** — 본 플랜의 훅은 defense-in-depth 다. `git rev-list --objects --all` 히스토리 스캔 + `git check-ignore` 반사실 스텝(TypeScript 비매칭)을 CI 에 넣어야 CP-1 회귀가 push 시점에 잡힌다.
- **Plan 01-07 (검증) 미결 항목:** V-SC4d(훅 실 차단 반사실 프로브) — 공백 포함 파일명 케이스 포함 권장.
- **Plan 01-08 (GitHub repo) 선행 조건 충족:** 가드레일 4종이 첫 push 이전에 존재한다. `master` → `main` 전환(D-08)과 LICENSE 결정이 그 플랜의 잔여 항목이다.

## Self-Check: PASSED

- 생성 주장 파일 6종 전부 디스크에 실재 (`.gitignore`, `.gitattributes`, `.editorconfig`, `.githooks/pre-commit`, `README.md`, 본 SUMMARY) + 저장소 밖 디렉터리 2종
- 커밋 3종 전부 `git log --all` 에 실재 (`3982e76`, `2dc1dbc`, `74c1ed2`)
- 정직 표기 검증: `.planning/REQUIREMENTS.md` ARCH-05 가 `[ ]` / `Pending` 로 유지됨 (조기 완료 표기 없음)
- 플랜 `<verification>` 8종 + 태스크 acceptance 34종 전부 통과 (실패 0)

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
