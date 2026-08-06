---
phase: 1
slug: foundation-guardrails
status: mapped
nyquist_compliant: true
wave_0_complete: false
created: 2026-08-06
updated: 2026-08-06
---

# Phase 1 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.
> Source: `01-RESEARCH.md` §Validation Architecture (V-SC1a~V-CP5, 26 items).
> Per-Task Map은 `01-01-PLAN.md` ~ `01-08-PLAN.md` 의 21개 태스크와 1:1 정합한다.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 (Gradle 8.14.5) — backend / (FE 스텁은 P1 테스트 대상 아님) |
| **Config file** | `backend/build.gradle` — Wave 1 (Plan 02 Task 1)이 설치 |
| **Quick run command** | `cd backend && ./gradlew test --tests "*ModularityTests*"` (Docker 불필요, 초 단위) |
| **Full suite command** | `cd backend && ./gradlew build` (Testcontainers mysql 포함) |
| **비-JUnit 게이트** | git / docker compose / gradle CLI 셸 체크 — 각 플랜의 `<verify><automated>` 및 CI 스텝으로 코드화 |
| **Estimated runtime** | quick ~15s · full ~120s (Testcontainers mysql 포함) |

---

## Sampling Rate

- **After every task commit:** quick run command + pre-commit 훅 자동 발화
- **After every plan wave:** `cd backend && ./gradlew build` + `docker compose --profile full config -q` + 미디어 스캔 3종
- **Before `/kha-verify-uat`:** full suite green + 반사실 프로브 2건 PASS + CI 첫 런 green
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| P1-01-T1 (.gitignore/.gitattributes/.editorconfig + 미디어 루트) | 01 | 1 | ARCH-05 | T-1-01, T-1-03, T-1-04 | 미디어·시크릿 격리, TypeScript 오탐 차단 | smoke | `git check-ignore -q media/probe.mp4 && ! git check-ignore -q frontend/next.config.ts && git check-ignore -q .env` | ❌ W1 | ⬜ pending |
| P1-01-T2 (pre-commit 훅 + hooksPath) | 01 | 1 | ARCH-05 | T-1-01, T-1-02 | 미디어/5MB 스테이징 차단 (defense-in-depth) | smoke | `git ls-files -s .githooks/pre-commit \| grep -q '^100755'` + `[ "$(git config --get core.hooksPath)" = ".githooks" ]` | ❌ W1 | ⬜ pending |
| P1-01-T3 (README UX 계약) | 01 | 1 | ARCH-05 | — | 상표/라이선스 고지 노출 | smoke | `grep -q 'docker compose down -v' README.md && grep -q 'CC BY 3.0' README.md` | ❌ W1 | ⬜ pending |
| P1-02-T1 (Gradle 스캐폴딩 + build.gradle + application.yaml) | 02 | 1 | ARCH-01 | T-1-05, T-1-06, T-1-07, T-1-09 | Actuator 화이트리스트, 핀 강제, ddl-auto=validate | smoke+unit | `cd backend && ./gradlew --version \| grep -q 'Gradle 8.14.5' && ./gradlew compileJava --no-daemon` | ❌ W1 | ⬜ pending |
| P1-02-T2 (named interface + 포트 + shared + HealthController) | 02 | 1 | ARCH-01 | T-1-08 | 경계 계약 선언 (CP-6 해소) | smoke | `cd backend && ./gradlew compileJava --no-daemon` + `[ "$(ls src/main/java/com/moabom/*/spi/package-info.java \| wc -l)" = "4" ]` | ❌ W1 | ⬜ pending |
| P1-02-T3 (@ApplicationModule 8종 + ModularityTests) **tdd** | 02 | 1 | ARCH-01 | T-1-05 | 모듈 경계 정적 강제 | unit | `cd backend && ./gradlew test --tests "*ModularityTests*" --no-daemon` (V-SC2a/b/c) | ❌ W1 | ⬜ pending |
| P1-03-T1 (Next 스캐폴딩 + 헬스 스텁) | 03 | 1 | ARCH-05 | T-1-13 | 정확 버전 핀 (공급망) | smoke | `cd frontend && npx tsc --noEmit` + `node -p "require('./package.json').dependencies.next"` = `16.2.12` | ❌ W1 | ⬜ pending |
| P1-03-T2 (next.config.ts rewrites + Dockerfile) | 03 | 1 | ARCH-05 | T-1-10, T-1-11, T-1-12 | 서버 env 비노출, 빌드 컨텍스트 최소화 | smoke | `cd frontend && npm run build && test -f .next/standalone/server.js` + `[ "$(grep -c 'NEXT_PUBLIC_' next.config.ts)" = "0" ]` (V-SC4c) | ❌ W1 | ⬜ pending |
| P1-04-T1 (V1__init_catalog.sql) | 04 | 2 | ARCH-05 | T-1-15, T-1-18 | 라이선스 컬럼 NOT NULL, 타 phase 선점 금지 | smoke | `grep -qE 'attribution +VARCHAR\(300\) +NOT NULL' V1 && [ "$(grep -cE '^\s*FULLTEXT\|ADD FULLTEXT' V1)" = "0" ]` | ❌ W2 | ⬜ pending |
| P1-04-T2 (V2__seed_golden.sql) | 04 | 2 | ARCH-05 | T-1-15, T-1-17 | CC-BY attribution 의무, 시크릿 부재 | smoke | `[ "$(grep -c 'CC-BY-3.0' V2)" = "5" ] && [ "$(grep -c 'FIXTURE-METADATA-ONLY' V2)" = "5" ]` | ❌ W2 | ⬜ pending |
| P1-04-T3 (SchemaMigrationTest) **tdd** | 04 | 2 | ARCH-05 | T-1-14, T-1-17, T-1-18 | Flyway 단일 진실 + 기동 파라미터 실효 | integration | `cd backend && ./gradlew test --tests "*SchemaMigrationTest*" --no-daemon` (V-SC3a~d, V-G3b) | ❌ W2 | ⬜ pending |
| P1-05-T1 (nginx/prometheus 설정) | 05 | 2 | ARCH-05 | T-1-21, T-1-22 | 경로 순회 차단(root+try_files), 읽기 전용 | smoke | `docker run --rm -v .../default.conf:...:ro nginx:stable-alpine nginx -t` + `promtool check config` | ❌ W2 | ⬜ pending |
| P1-05-T2 (compose.yaml + .env.example) | 05 | 2 | ARCH-05 | T-1-19, T-1-20, T-1-23, T-1-24 | 루프백 바인드, :ro 마운트, 기본값 문서화 | smoke | `docker compose --profile full config -q` + `[ "$(docker compose config \| grep -c 'ngram_token_size=2')" = "1" ]` (V-SC1a, V-G3a) | ❌ W2 | ⬜ pending |
| P1-06-T1 (ADR-0001 + ADR-0002) | 06 | 3 | ARCH-01, ARCH-05 | T-1-07, T-1-30 | EOL 수용 근거·완화 문서화 | smoke | `grep -q '2024-12-31' ADR1 && grep -q 'non-blocking' ADR1 && grep -q 'schema-initialization' ADR2` (V-G1, V-G4b) | ❌ W3 | ⬜ pending |
| P1-06-T2 (CI 워크플로) | 06 | 3 | ARCH-01, ARCH-05 | T-1-25, T-1-27, T-1-28, T-1-29 | 히스토리 스캔(실 boundary), 토큰 최소 권한 | smoke | `python -c "import yaml,io; yaml.safe_load(io.open('.github/workflows/ci.yml',encoding='utf-8'))"` + 로컬 게이트 3종 빈 출력 (V-SC4a/b, V-SC2b/c, V-G2a) | ❌ W3 | ⬜ pending |
| P1-06-T3 (dependabot.yml) | 06 | 3 | ARCH-01 | T-1-26, T-1-30 | 핀 보호 + auto-merge 부재 | smoke | `python -c "...yaml.safe_load('.github/dependabot.yml')"` + `grep -q 'io.github.resilience4j:\*'` (V-G1) | ❌ W3 | ⬜ pending |
| P1-07-T1 (반사실 프로브 2건) ★ | 07 | 4 | ARCH-01 | T-1-31 | **게이트의 작동 증명** | manual-once + 로그 | `grep -c '판정: PASS' 01-COUNTERFACTUAL-LOG.md` = `2` + `git log --all --diff-filter=A --name-only --pretty=format: \| grep -c probe` = `0` (V-SC2d, V-SC4d) | ❌ W4 | ⬜ pending |
| P1-07-T2 (full 프로필 실기동 + A2/A3/A5/A6) | 07 | 4 | ARCH-05 | T-1-32, T-1-34 | Actuator 화이트리스트 실효(=404) | manual-only + curl | `curl -fsS http://localhost:3000/api/health \| grep -q '"status":"UP"'` + `[ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/env)" = "404" ]` (V-SC1b/c, V-D02) | ❌ W4 | ⬜ pending |
| P1-07-T3 (사람 검수) **checkpoint** | 07 | 4 | ARCH-05 | T-1-33, T-1-35 | 브라우저 동일 오리진 육안 확인 | manual-only | (checkpoint — 사용자 승인) | ❌ W4 | ⬜ pending |
| P1-08-T1 (main 전환 + public repo + push) | 08 | 5 | ARCH-01, ARCH-05 | T-1-36, T-1-37, T-1-38, T-1-40 | push protection, 가드레일 선존재 | smoke | `[ "$(gh api repos/trevi00/moabom -q .security_and_analysis.secret_scanning_push_protection.status)" = "enabled" ]` | ❌ W5 | ⬜ pending |
| P1-08-T2 (CI 첫 런 게이트 도달성) | 08 | 5 | ARCH-01, ARCH-05 | T-1-39 | 게이트가 실제로 실행됨 | smoke | `[ "$(gh run list --workflow=ci.yml --limit 1 --json conclusion -q '.[0].conclusion')" = "success" ]` (V-CP5) | ❌ W5 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*
*★ = 반사실 프로브 (게이트의 존재가 아니라 작동을 증명)*

---

## RESEARCH V-매핑 커버리지 (26종)

| V-ID | 커버 태스크 | V-ID | 커버 태스크 |
|------|------------|------|------------|
| V-SC1a | P1-05-T2, P1-06-T2 | V-SC4a | P1-06-T2, P1-08-T1 |
| V-SC1b | P1-07-T2 | V-SC4b | P1-06-T2, P1-08-T1 |
| V-SC1c | P1-07-T2 | V-SC4c | P1-01-T1, P1-03-T2, P1-06-T2 |
| V-SC2a | P1-02-T3 | V-SC4d ★ | P1-07-T1 |
| V-SC2b | P1-02-T2/T3, P1-06-T2 | V-SC4e | P1-01-T1 |
| V-SC2c | P1-02-T3, P1-06-T2 | V-G1 | P1-06-T1, P1-06-T3 |
| V-SC2d ★ | P1-07-T1 | V-G2a | P1-02-T1, P1-06-T2 |
| V-SC3a | P1-04-T3 | V-G2b | P1-02-T1 |
| V-SC3b | P1-04-T3 | V-G3a | P1-05-T2 |
| V-SC3c | P1-04-T3 | V-G3b | P1-04-T3 |
| V-SC3d | P1-04-T3 | V-G4a | P1-02-T1, P1-01-T1 |
| V-D02 | P1-07-T2, P1-07-T3 | V-G4b | P1-06-T1 |
| V-CP5 | P1-08-T2 | | |

**커버리지: 26/26.** 고아 V-항목 0건.

---

## Wave 0 Requirements

RESEARCH가 "테스트 인프라 전무"로 기록한 11종은 **Wave 1의 Plan 01·02·03**이 전부 설치한다 (별도 Wave 0 플랜 불필요):

- [ ] `backend/settings.gradle` · `build.gradle` · `gradle.properties` — Plan 02 T1
- [ ] `backend/gradlew` + wrapper 8.14.5 — Plan 02 T1
- [ ] `.gitattributes` (eol=lf) + `gradlew` exec bit — Plan 01 T1 + Plan 02 T1
- [ ] `.gitignore` (CP-1 준수) — Plan 01 T1
- [ ] `.githooks/pre-commit` + `core.hooksPath` — Plan 01 T2
- [ ] `compose.yaml` + `.env.example` + `infra/{nginx,prometheus}/` — Plan 05 (Wave 2)
- [ ] `ModularityTests` — Plan 02 T3
- [ ] `SchemaMigrationTest` (+ Testcontainers command 미러링) — Plan 04 T3
- [ ] `.github/workflows/ci.yml` — Plan 06 T2
- [ ] `.github/dependabot.yml` — Plan 06 T3
- [ ] `D:/moabom-media/` 생성 — Plan 01 T1

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions | 태스크 |
|----------|-------------|------------|-------------------|--------|
| full 프로필 실기동 (V-SC1b/c, V-D02) | ARCH-05 | `bootBuildImage`(Paketo, 수 분) + FE 이미지 빌드를 요구 — D-09가 P1 CI를 최소 코어로 못박았다. 대신 `docker compose --profile full config -q`를 **자동 프록시**로 매 push마다 돌린다 | 선행 빌드 → `docker compose --profile full up -d --wait` → `curl -fsS :3000/api/health` → `docker compose ps` | P1-07-T2 |
| 브라우저 동일 오리진 렌더 | ARCH-05 (D-02) | 클라이언트 fetch의 브라우저 실행은 curl로 대체 불가 (CORS 프리플라이트 유무가 관측 대상) | http://localhost:3000 에서 헬스 JSON 렌더 + Network 탭 Origin 확인 | P1-07-T3 |
| 반사실 프로브 2건 (V-SC2d, V-SC4d) | ARCH-01, SC4 | 자동화하면 "일부러 깨진 커밋"이 이력에 남는다. **이 둘을 빼면 SC2·SC4의 문장이 증명되지 않는다** — 게이트의 존재는 게이트의 작동이 아니다 | 경계 위반 파일 주입 → verify() FAIL 확인 → 삭제 / 미디어·6MB 스테이징 → commit 거부 확인 → unstage. 종료코드와 오류 원문을 로그에 인용 | P1-07-T1 |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies — 21/21 (checkpoint 1건 제외 20/20 자동)
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references — Wave 1~2 플랜이 11종 전부 설치
- [x] No watch-mode flags — `--no-daemon` 사용, watch 없음
- [x] Feedback latency < 180s — quick 15s / full 120s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** planner (2026-08-06). `wave_0_complete`는 Plan 02·04 실행 후 true로 갱신.
