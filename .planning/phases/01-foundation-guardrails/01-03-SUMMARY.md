---
phase: 01-foundation-guardrails
plan: 03
subsystem: frontend
tags: [nextjs, app-router, react, typescript, rewrites, single-origin, standalone, docker, node24, cors]

# Dependency graph
requires:
  - "01-01: 루트 .gitignore 에 전역 *.ts 부재 (CP-1) — 이 플랜 산출물이 삼켜지지 않는 전제"
  - "01-02: GET /api/health (bff) — rewrites 가 프록시할 대상 계약"
provides:
  - "frontend/ Next 16.2.12 App Router 골격 (정확 핀: next 16.2.12 / react·react-dom 19.2.8)"
  - "app/HealthProbe.tsx — 'use client' 브라우저 동일 오리진 fetch('/api/health') (D-02 실동작 증거)"
  - "app/page.tsx — 헬스 스텁 1페이지 (D-05 '얇게', 행 UI 없음)"
  - "next.config.ts — /api/:path* → BACKEND_ORIGIN, /media/:path* → MEDIA_ORIGIN rewrites + output standalone"
  - "frontend/Dockerfile — node:24-alpine 3스테이지, build-arg 로 목적지 주입(기본값=full 프로필), npm ci"
  - "frontend/.dockerignore (node_modules/.next/.git — T-1-11) + public/.gitkeep"
  - "실측 사실: rewrites destination 은 build 시점 확정 — compose 는 build.args 로 목적지를 줘야 한다"
affects: [01-05-env-example, 01-06-ci, 01-07-verification, 01-08-github-repo, 02-hls-spike, 05-lolomo-ui]

# Tech tracking
tech-stack:
  added:
    - "Next.js 16.2.12 (App Router, Turbopack 빌드)"
    - "React / React DOM 19.2.8"
    - "TypeScript ^5 + @types/{node,react,react-dom}"
    - "node:24-alpine (FE 컨테이너 베이스 이미지)"
  patterns:
    - "빌드 시점 확정 값 vs 런타임 값 구분 — rewrites destination 은 전자다. env 외부화만으로는 컨테이너가 올바른 목적지를 갖지 않는다"
    - "이미지 기본값을 '가장 자주 틀리는 쪽'이 아니라 '이 이미지가 존재하는 이유'(full 프로필)로 둔다"
    - "게이트 통과 여부를 인라인 셸이 아니라 스크립트 파일로 판정한다 (Windows bash 따옴표 mangling 이 PASS/FAIL 을 뒤집는다)"

key-files:
  created:
    - frontend/package.json
    - frontend/package-lock.json
    - frontend/tsconfig.json
    - frontend/.gitignore
    - frontend/AGENTS.md
    - frontend/CLAUDE.md
    - frontend/app/layout.tsx
    - frontend/app/page.tsx
    - frontend/app/HealthProbe.tsx
    - frontend/app/globals.css
    - frontend/next.config.ts
    - frontend/Dockerfile
    - frontend/.dockerignore
    - frontend/public/.gitkeep
  modified: []

key-decisions:
  - "RESEARCH §Assumptions A3 을 이 플랜에서 반증 — rewrites destination 은 `next build` 시점에 routes-manifest.json 으로 구워지고 런타임 env 는 무시된다 (반사실 2-arm 실측). Dockerfile 이 목적지를 ARG 로 받고 기본값을 full 프로필(app:8080 / nginx:80)로 둔다"
  - "create-next-app 생성 잔재(page.module.css / favicon.ico / svg 5종 / 보일러플레이트 README) 삭제 — D-05 '얇게'. AGENTS.md + CLAUDE.md(11B 포인터)는 유지: Next 16 이 학습데이터와 다르다는 경고가 이후 phase 에 유효"
  - "next-env.d.ts 는 생성값 .gitignore 그대로 배제 — 생성 파일이며, 부재 상태에서 tsc --noEmit 이 통과함을 실측(fresh-clone 시뮬레이션)"
  - "ARCH-05 mark-complete 미실행 — 01-01/01-02 선례. compose.yaml 이 아직 없다"

patterns-established:
  - "가정(A3)은 플랜이 위임한 시점(Plan 07)보다 먼저, 그 가정에 의존하는 산출물을 만드는 그 자리에서 값싸게 반증할 수 있으면 반증한다"
  - "판별자(프로브)는 known-positive 로 먼저 검증한다 — fake backend 200 이 실제로 잡히는 것을 control arm 에서 확인한 뒤 negative 결과를 신뢰했다"

requirements-completed: []
requirements-contributed: [ARCH-05]

# Metrics
duration: 23 min
completed: 2026-08-06
---

# Phase 1 Plan 03: Next 골격 + 단일 진입 오리진 + FE 컨테이너 Summary

**Next 16.2.12 App Router 골격(정확 핀)에 `/api`·`/media` rewrites 를 얹어 D-02 단일 진입 오리진을 세우고, 그 과정에서 "rewrites 목적지는 런타임 env 로 바뀐다"는 리서치 가정(A3)을 반사실 2-arm 실측으로 반증해 FE 이미지가 full 프로필에서 실제로 백엔드를 가리키도록 build-arg 로 고쳤다**

## Performance

- **Duration:** 약 23분 (13:31 ~ 13:54 KST)
- **Tasks:** 2/2
- **Commits:** 2 (태스크당 1)
- **Files created:** 14 (커밋 기준) + 생성만 되고 배제된 `next-env.d.ts`

## Accomplishments

- **D-02 단일 진입 오리진이 "설정 파일에 문자열이 있다"가 아니라 "실제로 프록시된다"로 증명됐다.** control arm 에서 스텁 백엔드를 8099 에 띄우고 그 오리진으로 빌드한 뒤 standalone 서버에 `GET /api/health` 를 던지자 **HTTP 200 + `{"status":"FAKE-8099","path":"/api/health"}`** 가 돌아왔다. 브라우저(=curl)는 `:3210` 하나만 봤다. 실 Spring 백엔드 + compose 네트워크로 닫는 것은 플랜 지시대로 Plan 07 몫이지만, **프록시 경로 자체는 더 이상 미검증이 아니다.**
- **리서치 가정 A3 을 반증했다 (이 플랜의 가장 값진 산출).** `rewrites()` 의 destination 은 `next build` 시점에 `.next/routes-manifest.json` 으로 **구워지고**, `next start`/standalone 런타임의 `BACKEND_ORIGIN` 은 **무시된다**. 2-arm 으로 확인:
  - arm A: 빌드=기본값(localhost:8080), 런타임=127.0.0.1:8099 → 요청이 **localhost:8080** 으로 감 (fake hit 0).
  - arm B: 빌드=127.0.0.1:8099, 런타임=127.0.0.1:9999 → 요청이 **8099** 로 감 (fake hit 1, HTTP 200).
  - 판별자 자체를 known-positive(arm B)로 검증한 뒤 negative(arm A)를 신뢰했다.
- **그 결과 플랜 명세 그대로의 Dockerfile 이 가진 결함을 이 플랜 안에서 고쳤다.** 원안대로면 이미지에 `http://localhost:8080` 이 구워져 **full 프로필 컨테이너가 자기 자신에게 프록시**하고 Plan 07 의 `curl -f localhost:3000/api/health` 가 반드시 실패한다. `ARG BACKEND_ORIGIN=http://app:8080` / `ARG MEDIA_ORIGIN=http://nginx:80` 로 받고 build 스테이지 ENV 로 흘려 해결. **이미지 안에서 실측**: `http://app:8080/api/:path* | http://nginx:80/media/:path*`.
- **Dockerfile 이 문서가 아니라 동작하는 산출물임을 확인했다.** `docker build` rc=0 (217MB), 컨테이너 기동 후 `GET /` → **200** 이며 서빙된 HTML 에 `lang="ko"` · `모아봄 (moabom)` · `data-testid="health"` · `loading…` 이 전부 실재. `GET /api/health` 는 **500 + 컨테이너 로그 `getaddrinfo ENOTFOUND app`** — 이것이 오히려 "구워진 full 목적지로 다이얼한다"는 양성 증거다(compose 네트워크에서 `app` 이 해석된다).
- **위협 3종을 라벨이 아니라 산출물에서 검증했다.** T-1-10: 서빙된 클라이언트 HTML 에 `app:8080`·`BACKEND_ORIGIN` 문자열 **0건**(공개 접두 미사용). T-1-11: 이미지 `/app` 에 `.git` **없음**(`NO-LEAK`), 내용물은 `.next / node_modules / package.json / public / server.js` 뿐. T-1-13: `dependencies` 전부 정확 버전 + `package-lock.json` 커밋 + Dockerfile `npm ci`(`npm install` 0건).
- **CP-1 회귀 게이트 통과.** `git check-ignore` 가 `frontend/app/page.tsx`·`HealthProbe.tsx`·`layout.tsx`·`next.config.ts`·`Dockerfile`·`public/.gitkeep` 중 **어느 것도 잡지 않는다**.

## Task Commits

1. **Task 1: Next 16.2.12 App Router 스캐폴딩 + 헬스 스텁 페이지** — `fa01c83` (feat) — acceptance 15/15
2. **Task 2: next.config.ts rewrites + FE 컨테이너 Dockerfile** — `89568ee` (feat) — acceptance 18/18

## Files Created/Modified

- `frontend/package.json` — `next 16.2.12` / `react 19.2.8` / `react-dom 19.2.8` **정확 핀**(레인지 0건), scripts dev/build/start
- `frontend/package-lock.json` — 28 패키지 잠금 (Dockerfile `npm ci` 의 단일 진실)
- `frontend/tsconfig.json` · `frontend/.gitignore` — create-next-app 생성값 유지
- `frontend/app/HealthProbe.tsx` — `'use client'` + `useEffect` 안에서 `fetch('/api/health')`, 실패 시 `ERROR:` 렌더 (서버 fetch 로 대체하지 않음 = D-02 의 핵심)
- `frontend/app/page.tsx` — 헬스 스텁 1페이지 (h1 모아봄 + 단일 오리진 설명 + `<HealthProbe />`)
- `frontend/app/layout.tsx` — `lang="ko"`, `title: 모아봄 (moabom)`, 구글 폰트 로더 제거(빌드의 네트워크 의존 제거)
- `frontend/app/globals.css` — 생성값 유지(시스템 폰트 스택, 폰트 변수 참조 없음)
- `frontend/next.config.ts` — `output: 'standalone'` + `/api/:path*`·`/media/:path*` rewrites, 목적지는 `process.env.BACKEND_ORIGIN`/`MEDIA_ORIGIN` (기본값 = dev)
- `frontend/Dockerfile` — `node:24-alpine` deps/build/runtime 3스테이지, `npm ci`, **`ARG BACKEND_ORIGIN`/`ARG MEDIA_ORIGIN`(기본 full 프로필)**, `HOSTNAME=0.0.0.0`, `CMD ["node","server.js"]`
- `frontend/.dockerignore` — `node_modules`/`.next`/`.git`/`Dockerfile`/`.dockerignore`/`npm-debug.log`
- `frontend/public/.gitkeep` — 빈 `public/` 이 클론에 존재하도록 (없으면 이미지 빌드의 마지막 COPY 가 죽는다)
- `frontend/AGENTS.md` · `frontend/CLAUDE.md` — create-next-app 16.2.12 생성값 유지 (아래 Deviation 3)

## Decisions Made

1. **Dockerfile 이 rewrites 목적지를 build-arg 로 받는다** — Deviation 1. 잠긴 결정(D-02/D-06)은 그대로이고, 그 결정이 컨테이너에서 실제로 성립하도록 만드는 최소 변경.
2. **이미지 기본값 = full 프로필 목적지** — D-06 이 "이 이미지는 full 전용, 평시 개발은 `npm run dev`"라고 못박았으므로, 이미지의 기본값은 dev 가 아니라 full 이어야 한다. compose 가 `build.args` 로 덮어쓸 수 있다.
3. **템플릿 잔재 삭제 / AGENTS.md·CLAUDE.md 유지** — Deviation 3.
4. **`next-env.d.ts` 를 커밋하지 않음** — 생성값 `.gitignore` 그대로. fresh-clone 시뮬레이션(`next-env.d.ts` + `.next` 둘 다 치운 상태)에서 `tsc --noEmit` 통과를 실측했다.
5. **`requirements mark-complete ARCH-05` 미실행** — Deviation 4 (01-01·01-02 선례 계승).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] 플랜 명세 그대로의 Dockerfile 은 full 프로필에서 D-02 를 깬다 — rewrites 목적지가 build 시점에 구워진다**

- **Found during:** Task 2 (빌드 산출물 확인)
- **발견 경로:** `npm run build` 후 `.next/standalone/.next/routes-manifest.json` 을 열어보니 destination 이 문자열로 **박혀 있었다**(`http://localhost:8080/api/:path*`). 플랜/RESEARCH 는 `rewrites()` 가 **서버 기동 시** 평가된다고 가정(A3)하고 그 검증을 Plan 07 로 위임한 상태였다.
- **실측 (반사실 2-arm, 스텁 백엔드 :8099):**
  | arm | build env | runtime env | 실제 도착지 | fake hit |
  |---|---|---|---|---|
  | A | (기본) localhost:8080 | 127.0.0.1:8099 | localhost:8080 | 0 |
  | B | 127.0.0.1:8099 | 127.0.0.1:9999 | **127.0.0.1:8099** (HTTP 200 JSON) | 1 |
  arm A 의 응답이 404 였는데, 조사 결과 이 머신의 **무관한 로컬 서비스(PID 5140, EnterpriseDB)가 8080 을 점유**하고 있었다 — 즉 "아무도 안 듣는 포트라 실패했다"가 아니라 **8080 으로 실제로 갔다**는 양성 증거였다. (`curl localhost:8080/` → `Server is up and running` EDB 페이지.)
- **Fix:** build 스테이지에 `ARG BACKEND_ORIGIN=http://app:8080` / `ARG MEDIA_ORIGIN=http://nginx:80` + `ENV` 전달. runtime 스테이지에는 "여기서 주면 효과 없음"을 주석으로 명시.
- **Verification:** 이미지 내부 `/app/.next/routes-manifest.json` → `http://app:8080/api/:path* | http://nginx:80/media/:path*`. 컨테이너 `GET /api/health` → 500 + `ENOTFOUND app`(= 구워진 목적지로 다이얼).
- **Files modified:** `frontend/Dockerfile`
- **Committed in:** `89568ee`
- **잔여:** compose 가 이 이미지를 **build** 할 때만 성립한다. Plan 05/07 가 `image:` 로 미리 빌드된 이미지를 끌어오는 구조를 택하면 목적지를 다시 정해야 한다.

**2. [Rule 3 - Blocking] 플랜 산문이 자기 grep 게이트를 깬다 (3회째 동형 — 01-01 D1 · 01-02 D4 에 이어)**

- **Found during:** Task 2 acceptance
- **Issue:** 플랜 `<action>` 이 지시한 주석은 `// ⚠️ NEXT_PUBLIC_ 접두를 쓰지 말 것 …` 인데, 같은 태스크의 acceptance 는 `grep -c 'NEXT_PUBLIC_' frontend/next.config.ts` 가 `0` 일 것을 요구한다. 지시대로 쓰면 `1` 이 나와 반드시 실패한다.
- **Fix:** 리터럴 없이 재작성 — "이 두 값에 '클라이언트로 인라인되는 공개 접두'(Next 의 공개 env prefix)를 붙이지 말 것 — 붙이는 순간 서버 전용 목적지가 클라이언트 번들에 박혀 공개 저장소로 영구 유출된다 (T-1-10)". 경고 가치와 criterion 의도(코드에 그 접두가 없다)를 둘 다 보존.
- **동형 재발 1건 더 (자책):** 내가 Task 1 에서 쓴 `layout.tsx` 주석 `폰트 import(next/font/google)…` 이 `grep -c 'next/font' == 0` 게이트를 깼다. 같은 방식으로 리터럴을 제거해 통과.
- **패턴:** "금지 토큰을 설명하는 주석"과 "금지 토큰 부재를 세는 게이트"는 구조적으로 충돌한다. 게이트가 **선언부만** 보게 하거나(예: `grep -c '^\s*import.*next/font'`), 플랜이 주석에 리터럴을 쓰지 않아야 한다. 3회 반복 = 플래너 쪽 규약 후보.
- **Committed in:** `fa01c83`(layout) / `89568ee`(next.config.ts)

**3. [정직 표기] create-next-app 16.2.12 실 생성값이 플랜 파일 목록과 다르다**

- **Issue:** 16.2.12 템플릿은 플랜이 예상하지 않은 파일을 만든다 — `AGENTS.md`, `CLAUDE.md`(11B, `@AGENTS.md` 포인터), `README.md`(Vercel 배포 보일러플레이트), `app/page.module.css`, `app/favicon.ico`, `public/*.svg` 5종. 또 `react` 기본값이 **19.2.4** 라 플랜의 19.2.8 핀과 다르다.
- **처리:**
  - **삭제**(D-05 "얇게" + 상표/브랜딩 규율): `README.md`(루트 README 가 이미 기동 UX 를 소유, Vercel 배포 안내는 이 프로젝트에서 거짓), `app/page.module.css`·`app/favicon.ico`(미사용 + 타사 로고), `public/*.svg` 5종(Next/Vercel 워드마크).
  - **유지**: `AGENTS.md` + `CLAUDE.md` — 내용이 "이건 네가 아는 Next 가 아니다, `node_modules/next/dist/docs/` 를 먼저 읽어라"는 경고다. Next 16 이 학습데이터와 어긋난다는 사실은 P5 UI 작업에서 실제로 값을 낸다. 루트 `CLAUDE.md`(GSD 관리)와 충돌하지 않는다(중첩 로드는 가산적).
  - **핀 교체**: `react`/`react-dom` 을 19.2.8 로 바꾸고 `npm install` 재실행 → 설치본 실측 19.2.8.
- **Verification:** `ls frontend/app/*.tsx | wc -l` = 3, `public/` 은 `.gitkeep` 만.

**4. [Rule 3 - Blocking] ARCH-05 조기 완료 표기 회피 (01-01·01-02 선례)**

- **Issue:** frontmatter 는 `requirements: [ARCH-05]` 지만 ARCH-05("docker-compose 한 번으로 전 스택 기동")는 7개 플랜이 공유하고 **`compose.yaml` 은 아직 없다**. 지금 체크하면 기동되지 않는 스택을 기동된다고 선언하게 된다.
- **Fix:** `requirements mark-complete` 미실행. `requirements-completed: []` + `requirements-contributed: [ARCH-05]` 분리 표기.
- **Verification:** `.planning/REQUIREMENTS.md` 의 ARCH-05 가 `[ ]` / `Pending` 로 유지됨.

**5. [Rule 1 - Bug] 인라인 셸의 따옴표 mangling 이 게이트 판정을 뒤집었다**

- **Found during:** 최종 verification 블록 실행
- **Issue:** `grep -q "fetch('/api/health')" frontend/app/HealthProbe.tsx` 를 **인라인**으로 실행하면 rc=1(FAIL), 같은 검사를 **스크립트 파일**로 실행하면 PASS. 파일 내용은 `cat -A` 로 `    fetch('/api/health')$` 임을 확인 — 즉 **파일 결함이 아니라 도구 경계의 따옴표 소실**이다(작은따옴표가 래퍼에서 먹힘).
- **Fix:** 모든 acceptance 판정을 스크립트 파일(`acc_t1.sh`/`acc_t2.sh`/`must_haves.sh`)로 옮겨 재실행. 최종 결과는 스크립트 판정을 정본으로 삼는다.
- **의미:** 인라인 grep 은 **거짓 FAIL 뿐 아니라 거짓 PASS 도** 만들 수 있다(패턴이 뭉개져 더 느슨해지는 경우). 이후 플랜의 acceptance 는 스크립트 파일 실행을 권장.

**6. [Rule 1 - Bug] `gsd-tools` 진행 갱신 결함 4회째 재발 + 문서-도구 인자 불일치 1건(신규)**

- **재발 3종 (01-01 D4·D5 / 01-02 D6 과 동일):**
  - `state update-progress` → `{"bar":"[████░░░░░░] 38%","updated":true}` 를 반환하고 **frontmatter 만** 38% 로 갱신, 본문 `Progress:` 는 `[███░░░░░░░] 25%` 그대로.
  - `state advance-plan` → 본문 `Last activity` 의 서술을 **파괴**(`2026-08-06 -- 01-02 완료 (…)` → `2026-08-06`). 3회 연속 동일 증상.
  - `roadmap update-plan-progress 01 01-03 completed` → `{"summary_count":3,"updated":true}` 반환, 체크박스는 `- [x] 01-03-PLAN.md` 로 **실제 변경**했으나 Progress 표 행은 `2/8` 에 머묾.
- **신규 1종 (문서-도구 계약 불일치):** executor 워크플로 문서는 `state add-blocker "설명"`(위치 인자)로 안내하는데 실제 도구는 `--text` 를 요구한다 → 첫 3회 호출이 전부 `{"error":"text required"}` 로 **조용히 실패**했다. `--text` 로 재호출해 3건 등록 완료.
- **Fix:** 하네스(`C:/Users/user/.claude/get-shit-done`)는 이 프로젝트 스코프 밖이라 손대지 않았다. 저장소 산출물인 `.planning/STATE.md`(본문 바 38% + `Last activity` 서술 복원)와 `.planning/ROADMAP.md`(표 행 `3/8`)만 **디스크 실측값**으로 정정.
- **정합성 확인 (디스크 실측 5중 대조):** SUMMARY 파일 3개 = ROADMAP `[x]` 3건 = 표 행 `3/8` = frontmatter `completed_plans: 3` / `percent: 38` = 본문 바 `[████░░░░░░] 38%`.
- **이번 회차의 추가 정보:** 오케스트레이터 경고("부분 기록이 무기록보다 위험")가 그대로 재현됐다. 세 도구 모두 `updated: true` 를 반환했고 **각각 파일의 일부는 실제로 썼기 때문에** `git status` 만 봤다면 전부 "갱신됨"으로 오판했을 것이다. 판정은 `git diff` 본문 대조로만 가능했다.

---

**Total deviations:** 6 (1 실산출물 버그, 1 blocking 플랜 모순, 1 정직 표기, 1 요구사항 표기, 2 도구 결함)
**Impact on plan:** 스코프 확대 없음. 잠긴 결정(D-02·D-05·D-06)과 버전 핀(next 16.2.12 / react 19.2.8 / node:24-alpine)은 전부 그대로 집행됐다. Deviation 1 만 **플랜 산출물의 실 결함**이며, 잠긴 결정을 바꾸지 않고 그 결정이 성립하도록 고쳤다.

## Verification Results

플랜 `<verification>` 전량 + 태스크 acceptance 33종 + must_haves 7종 = **40/40 PASS (실패 0)**.

```
npm run build                                   → rc=0 (Turbopack, 3 routes)
test -f .next/standalone/server.js              → PASS
npx tsc --noEmit                                → No errors found
dependencies                                    → next 16.2.12 / react 19.2.8 / react-dom 19.2.8
grep -c 'NEXT_PUBLIC_' next.config.ts           → 0
grep -c 'next/font' app/layout.tsx              → 0
grep -c 'tailwind' package.json                 → 0
ls app/*.tsx | wc -l                            → 3
test -d frontend/pages                          → non-zero (App Router only)
git check-ignore (소스 6종)                      → 전부 non-zero (CP-1 clean)
docker build -t moabom-frontend:p1-verify       → rc=0 (217MB)
이미지 내 routes-manifest 목적지                  → http://app:8080/... | http://nginx:80/...
컨테이너 GET /                                   → 200 (lang="ko" · 모아봄 · data-testid="health" · loading…)
컨테이너 GET /api/health                         → 500 + log: getaddrinfo ENOTFOUND app (구워진 목적지로 다이얼)
이미지 .git 유출                                  → NO-LEAK
클라이언트 HTML 내 오리진/공개접두 문자열           → 0건
control arm (스텁 백엔드 8099 로 빌드)             → GET /api/health = 200 {"status":"FAKE-8099"}
```

**진행 갱신 도구 실측 (4회째 재발 확인):** Deviation 6 참조. 도구 반환값(`updated: true`)이 아니라 `git diff` 본문 대조로 판정했고, 미기록분은 디스크 실측값으로 직접 정정했다.

## Known Stubs

| Stub | 위치 | 이유 / 해소 시점 |
|---|---|---|
| 헬스 스텁 1페이지가 FE 의 전부 | `frontend/app/page.tsx` | **의도된 스텁**(D-05). 행/카드/캐러셀 UI 는 Phase 5 목업-우선 규율 대상 — 이 플랜이 만들면 안 되는 것 |
| `HealthProbe` 가 현재 화면에 `ERROR:` 를 렌더 | `frontend/app/HealthProbe.tsx` | 백엔드가 기동되지 않았기 때문. 계약(`GET /api/health`)은 01-02 에 실재하고, 프록시 경로는 스텁 백엔드로 200 을 실측했다. 실 200 은 Plan 07(full 프로필 실기동)에서 닫힌다 |
| `public/` 이 `.gitkeep` 뿐 | `frontend/public/` | 정적 자산이 아직 없다. HLS 세그먼트는 저장소 밖(D:/moabom-media) + nginx 소관이라 여기 들어오지 않는다 |

## Issues Encountered

- **알려진 결함 (잔여 노름, 비-제로):**
  1. **npm audit high 3건** — `postcss`(XSS/경로순회 4 advisory) + `sharp`(libvips CVE 4건), 둘 다 `next` 의 전이 의존. **수정 경로가 `next@16.3.0` 하나뿐**이라 잠긴 핀(STACK 16.2.x / 플랜 16.2.12)과 정면 충돌한다 → **bump 하지 않았다**. 위협 맥락: 로컬/포트폴리오, postcss 는 빌드 타임, sharp 는 `next/image` 최적화 경로(P1 은 이미지 최적화를 쓰지 않는다 — `next/image` 사용처 0). Plan 06 의 Dependabot 이 이걸 계속 붉게 띄울 것이며, **그때 핀 vs CVE 트레이드오프를 사람이 판단**해야 한다.
  2. **A3 반증의 반대편은 미실측** — `npm run dev` 가 런타임 env 를 반영하는지는 확인하지 않았다. dev 는 기본값(localhost:8080/8081)으로 동작하도록 되어 있어 P1 에서 env 주입이 필요 없기 때문이다. Plan 05 가 dev 용 env 를 `.env.example` 에 넣는다면 **그 경로는 별도 실측이 필요**하다.
  3. **compose 네트워크에서의 실 200 은 여전히 미검증** — 이 플랜은 `app` 호스트가 **해석되지 않는** 환경에서 프록시 다이얼까지만 봤다. `app:8080` 이 실제로 뜨고 응답하는 것은 Plan 07 Task 2.
  4. **로컬 Node 22.17.1 ≠ 핀 24** — Next 16 최소요구(>=20.9.0) 충족이라 비차단. 컨테이너는 `node:24-alpine` 로 고정돼 산출물은 24 에서 만들어진다. 로컬 빌드와 이미지 빌드의 Node 메이저가 다르다는 사실 자체는 남는다.
  5. **`next-env.d.ts` 는 커밋되지 않는다**(생성값 `.gitignore`). fresh-clone 에서 `tsc --noEmit` 통과를 실측했으나, 이는 현재의 얇은 앱 기준이다. `next/image` 등 전역 타입에 의존하는 코드가 들어오면 **CI 는 `tsc` 전에 `next build`(또는 `next typegen`)를 돌려야** 한다 — Plan 06 이 알아야 할 조건.
  6. **8080 포트가 이 머신에서 이미 점유돼 있다**(EnterpriseDB, PID 5140). 백엔드를 로컬 8080 에 띄우는 순간 충돌한다. Plan 07 착수 전에 해당 서비스를 끄거나 포트를 옮겨야 한다 — **이번 프로브가 우연히 드러낸 환경 사실**이다.
  7. **`frontend/.gitignore` 의 `.env*` 는 `.env.example` 도 배제한다** — Plan 05 가 FE 쪽 예시 파일을 `frontend/` 에 두려 하면 조용히 무시된다(루트에 두면 무관). 부정 예외(`!.env.example`)는 필요해질 때 추가.

## User Setup Required

None — 이 플랜은 외부 서비스 설정을 요구하지 않는다.
다른 머신에서 클론할 경우 `cd frontend && npm ci` 1회가 필요하다(락파일 고정 설치).

## Next Phase Readiness

- **Plan 05 (.env.example) 필독:** `BACKEND_ORIGIN`/`MEDIA_ORIGIN` 은 **런타임 변수가 아니다**. `.env` 에 넣어 `next start` 에 주는 구성은 **효과가 없다**. compose 에서는 `build.args` 로 줘야 하고, dev 는 기본값으로 충분하다.
- **Plan 05/07 (compose):** FE 서비스는 `build: { context: ./frontend, args: { BACKEND_ORIGIN: http://app:8080, MEDIA_ORIGIN: http://nginx:80 } }` 형태여야 한다. 인자를 생략해도 **Dockerfile 기본값이 같은 값**이라 안전하게 동작한다(기본값을 full 로 둔 이유). 포트는 `3000:3000`, `profiles: [full]`.
- **Plan 06 (CI):** `npm ci` → `npm run build` 순서로 돌리면 타입 검사까지 포함된다(빌드가 `Running TypeScript` 를 수행). `tsc --noEmit` 을 **단독**으로 먼저 돌리는 구성은 위 결함 5를 밟을 수 있다. Dependabot npm 감시(T-1-13)는 결함 1을 즉시 붉게 만든다 — **예상된 붉음**임을 워크플로/문서에 미리 적어두는 편이 좋다.
- **Plan 07 (검증):** `curl -f http://localhost:3000/api/health` 가 이 phase 의 최종 게이트다. 실패 시 **먼저 볼 것은 rewrites 가 아니라** (a) 8080 포트 점유(결함 6), (b) FE 이미지가 build.args 없이 다른 목적지로 구워졌는지(`docker run --rm <img> node -e "console.log(require('/app/.next/routes-manifest.json').rewrites.afterFiles.map(r=>r.destination))"` 로 1초 만에 확인 가능).
- **Phase 2 (HLS):** hls.js 는 아직 설치하지 않았다(RESEARCH 지시대로 P1 불필요). `/media/*` rewrites 는 이미 배선돼 있으므로, 세그먼트가 동일 오리진으로 오는 구조(STACK §2.6 의 비대칭 CORS 버그 회피)는 준비됐다.
- **Phase 5 (행 UI):** `app/` 에는 layout/page/HealthProbe 3개뿐이다. 목업-우선 규율의 출발선이 깨끗하다.

## Self-Check: PASSED

- 생성 주장 파일 14종 전부 디스크 실재 + 커밋에 포함 확인 (`git show --stat fa01c83` 10종 / `89568ee` 4종)
- 커밋 2종 전부 `git log` 에 실재: `fa01c83`, `89568ee`
- acceptance 33종 + must_haves 7종 = 40/40 PASS (스크립트 파일 판정, 인라인 판정 아님)
- 정직 표기 검증: `.planning/REQUIREMENTS.md` ARCH-05 가 `[ ]` / `Pending` 로 유지됨 (조기 완료 표기 없음)
- 프로브 잔재 0: `docker rmi moabom-frontend:p1-verify` 완료, 검증 컨테이너 제거, `git status --porcelain` 빈 출력
- 반증 결과(A3)를 SUMMARY 서술뿐 아니라 **소스 주석(`frontend/Dockerfile`)에도 남겨** 다음 플랜이 문서를 못 읽어도 마주치게 했다

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
