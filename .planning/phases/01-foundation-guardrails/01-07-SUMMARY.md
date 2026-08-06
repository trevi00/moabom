---
phase: 01-foundation-guardrails
plan: 07
subsystem: infra
tags: [docker-compose, prometheus, spring-modulith, paketo, buildpacks, git-hooks, counterfactual-testing]

requires:
  - phase: 01-01
    provides: ".githooks/pre-commit (미디어·5MB 게이트), .gitignore, README"
  - phase: 01-02
    provides: "Modulith 8모듈 골격 + ModularityTests(verify) + catalog allowedDependencies={}"
  - phase: 01-03
    provides: "Next 골격 + rewrites + FE Dockerfile(ARG 목적지) — A3 반증분"
  - phase: 01-05
    provides: "compose profiles 이원화 + prometheus 단일 스크레이프 타깃(A2 피검증체)"
  - phase: 01-06
    provides: "ADR-0001 §6 빌더 다이제스트 빈자리 + CI 게이트"
provides:
  - "게이트의 *작동* 증명 2건 (V-SC2d 경계 위반 / V-SC4d 미디어·대용량 커밋) — 커맨드·종료코드·오류 원문 인용"
  - "full 프로필 6서비스 실기동 실측 (앱 헬스·브라우저 단일 오리진·Actuator 화이트리스트·nginx·prometheus)"
  - "RESEARCH 가정 4건 판정: A5·A6 확인 / A2 반증→대안 적용 / A3 결과 확인"
  - "prometheus scrape job 2분리 (moabom-app-container = 결정론적 full 경로)"
  - "Paketo 빌더·run·산출 이미지 다이제스트 기록 (재현 이월)"
  - "operator UAT 잔여 대장 (01-HUMAN-UAT.md, status: partial)"
affects: [01-08, phase-02-hls-spike, phase-06-observability]

tech-stack:
  added: []
  patterns:
    - "반사실 프로브 manual-once: 위반 주입 → 게이트 붉어짐 확인 → 완전 원복. 위반 상태는 절대 커밋하지 않고 실행 기록으로 검증 가능성을 남긴다"
    - "DOWN 의 원인 귀속: 실패 코드가 아니라 **응답자를 식별**해 환경 요인과 설계 결함을 분리한다"
    - "대리 검증(HTTP)과 육안 검증을 분리해 후자를 status: partial 대장으로 영속화"

key-files:
  created:
    - .planning/phases/01-foundation-guardrails/01-COUNTERFACTUAL-LOG.md
    - .planning/phases/01-foundation-guardrails/01-HUMAN-UAT.md
  modified:
    - docs/adr/0001-runtime-versions.md
    - infra/prometheus/prometheus.yml
    - README.md

key-decisions:
  - "A2 반증 → scrape job 2분리. 측정은 포트 충돌로 오염됐으나 분리 자체는 충돌과 독립적으로 옳다(컨테이너 경로는 호스트 포트 발행에 무의존=결정론적, dev 는 host-gateway 가 유일 경로 — 애초에 다른 토폴로지)"
  - "호스트 8080 이중 LISTEN = (c) 현 상태 수용 + Phase 2 이월. (a) 무관 서비스 정지는 operator 소관, (b) 앱 포트 이동은 설계 변경이라 뒤집을 근거 없음"
  - "빌더 다이제스트는 기록만 하고 핀하지 않음 — 재현이 필요해지는 사고가 1회라도 나면 그때 승격"
  - "app 의 0.0.0.0:8080 발행은 근거를 잃었으나 바인드는 변경하지 않음 — dev 워크플로·README 동반 변경이라 사람 판단 몫(ADR §7·§9 에 근거 소실 기록)"
  - "ARCH-01/ARCH-05 mark-complete 미실행 (선례 7회째) — CI 0회 실행 + 문서대로의 한 커맨드 기동이 이 머신에서 미충족"

patterns-established:
  - "게이트 가치는 라벨이 아니라 결과로: '존재한다' 가 아니라 '위반을 실제로 막았다'"
  - "vacuous-PASS 이중 배제: 낡은 산출물 잔존 + 테스트 캐시 서빙 두 경로를 각각 실측으로 닫는다"
  - "귀속 arm: 게이트가 여러 개면 종료코드만으로 어느 게이트가 발화했는지 알 수 없다 → 격리 실행 추가"

requirements-completed: []

duration: 28min
completed: 2026-08-06
---

# Phase 1 Plan 07: 반사실 프로브 + full 프로필 실기동 Summary

**경계 위반이 `verify()` 를 실제로 죽이고 미디어·5MB 스테이징이 실제로 커밋을 거부시킴을 원문 인용으로 증명했고, full 프로필 6서비스를 실기동해 RESEARCH 가정 4건을 닫았다 — 그 과정에서 A2 단일 스크레이프 전략을 반증하고 호스트 8080 이중 LISTEN 이라는 Windows 특유의 조용한 충돌을 적발했다.**

## Performance

- **Duration:** 28 min
- **Started:** 2026-08-06T05:58:00Z
- **Completed:** 2026-08-06T06:26:00Z
- **Tasks:** 3 (2 auto + 1 checkpoint)
- **Files modified:** 5 (created 2 / modified 3)

## Accomplishments

- **SC2 의 문장이 증명됐다.** `catalog → playback::spi` 위반을 주입하니 `verify()` 가 exit 1 로 죽었고, 원문이 위반 클래스·대상 named interface·`Allowed targets: none` 까지 지목했다. 원복 후 green.
- **SC4 의 문장이 증명됐다.** `.gitignore` 를 `-f` 로 우회해도 훅이 2차 방어로 발화했고(`BLOCKED (media extension)`), 5MB 게이트는 **격리 arm 에서 단독으로도** 커밋을 거부했다(`BLOCKED (>5MB: 6291456B)`). 프로브는 워킹트리·스테이징·이력 어디에도 남지 않았다.
- **full 프로필 6서비스 실기동.** `:3000/api/health` → `{"status":"UP","app":"moabom","ts":...}` (D-02 단일 오리진 실동작), `/actuator/env` → **404**(화이트리스트 실효), `/actuator/prometheus` → 200, nginx `/healthz` → ok, mysql healthy.
- **가정 4건 판정 + ADR-0001 §9 신설.** A5·A6 확인 / A2 반증→대안 적용 / A3 는 메커니즘 반증(01-03)의 대체 구조가 실동작함을 확인.
- **A2 DOWN 의 원인을 귀속했다.** `lastError: 404` 는 연결 거부가 아니라 *누군가 응답했다* 는 뜻 → 응답자를 식별하니 `Server: Apache` 였다. 같은 호스트 포트가 출처에 따라 다른 서버로 라우팅되고 있었다.

## Task Commits

1. **Task 1: 반사실 프로브 2건 + 실행 로그** — `7904a0b` (docs)
2. **Task 2: full 프로필 실기동 + A2/A3/A5/A6 실측 + 다이제스트** — `0565b71` (feat)
3. **Task 3: 사람 검수 승인(조건부) + operator 잔여 대장** — `f05329c` (docs)

## Files Created/Modified

- `.planning/phases/01-foundation-guardrails/01-COUNTERFACTUAL-LOG.md` — V-SC2d/V-SC4d 실행 기록(커맨드·종료코드·오류 원문·vacuous-PASS 배제·대조군·잔여 4종)
- `.planning/phases/01-foundation-guardrails/01-HUMAN-UAT.md` — operator 잔여 U-1~U-5, `status: partial`
- `docs/adr/0001-runtime-versions.md` — §6 다이제스트 3종 기록, **§9 실측으로 닫힌 가정 신설**, §7 accept 근거 약화 표기, 잔여 리스크 N=2→N=3 + 환경 결함 1건
- `infra/prometheus/prometheus.yml` — 단일 타깃 → `moabom-app-container` / `moabom-app-host` 2분리 + 반증 근거 주석
- `README.md` — 호스트 포트 충돌 진단 절차 + Windows 이중 LISTEN 함정

## Decisions Made

- **A2 대안 적용의 근거를 측정이 아니라 구조에 둔다.** 관측된 DOWN 에는 환경 요인(8080 점유)이 섞여 있어 "단일 타깃 전략이 깨끗한 환경에서 실패한다"는 증명이 **아니다**. 그럼에도 분리한 이유는 full 의 `app:8080` 이 호스트 포트 발행에 무의존이라 결정론적이고, dev 는 host-gateway 가 유일 경로여서 **두 모드가 애초에 다른 토폴로지**이기 때문이다. 포트 충돌은 원인이 아니라 드러내 준 계기다.
- **8080 = (c) 현 상태 수용, Phase 2 이월.**
- **다이제스트는 기록이지 핀이 아니다** — `build.gradle` 은 여전히 `:latest`. 재현성은 아직 확보되지 않았음을 ADR 에 명시.
- **ARCH-01/ARCH-05 mark-complete 미실행 (선례 7회째).**

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] grafana 3001 바인드 실패 → 저장소 밖 임시 오버레이로 우회**
- **Found during:** Task 2
- **Issue:** 무관 프로젝트의 `exoplanet-grafana` 가 `0.0.0.0:3001` 점유 → `docker compose --profile full up -d --wait` 가 `address already in use` 로 **중단**(exit 1). 외부 워크로드라 종료 금지.
- **Fix:** `compose.yaml` 을 수정하지 않고, **저장소 밖 scratchpad** 의 오버레이(`ports: !override ["127.0.0.1:3002:3000"]`)로 grafana 만 재매핑. app 은 재매핑하지 **않았다** — A2 가 시험하는 host-gateway 경로를 오염시키지 않기 위해.
- **Verification:** 6서비스 up, 작업 트리 clean(오버레이가 저장소 밖이므로 흔적 0)
- **Committed in:** — (커밋하지 않음. 의도적)

**2. [Rule 2 - Missing Critical] README 에 호스트 포트 충돌 진단 절차 부재**
- **Found during:** Task 2
- **Issue:** README 는 "한 커맨드 기동"만 말하고, 그것이 실패했을 때 무엇을 봐야 하는지 침묵. 특히 Windows 이중 LISTEN 은 **조용히 통과**해 오진을 유발한다.
- **Fix:** 점유자 식별 절차(`netstat` → PID → `tasklist`) + 이중 LISTEN 함정 경고 추가
- **Verification:** 실측 오류 문자열을 그대로 인용해 검색 가능하게 함
- **Committed in:** `0565b71`

**3. [Rule 1 - Bug] `MSYS_NO_PATHCONV=1` 이 `-f` 인자를 역방향으로 깨뜨림**
- **Found during:** Task 2
- **Issue:** 인계받은 MSYS 규율대로 `MSYS_NO_PATHCONV=1` 을 걸었더니 `/c/Users/...` 오버레이 경로가 **변환되지 않아** docker 가 `D:\c\Users\...` 로 해석 → `The system cannot find the path specified`
- **Fix:** 인자를 Windows 형식(`C:/Users/...`)으로 전달
- **Verification:** `test -f "$OV"` 선-확인 후 up exit 0
- **Committed in:** — (커맨드 수준 수정, 산출물 없음)

---

**Total deviations:** 3 auto-fixed (1 blocking, 1 missing critical, 1 bug)
**Impact on plan:** 스코프 확대 없음. 1번은 검증을 완수하기 위한 최소 우회이며 저장소에 흔적을 남기지 않았다.

## Issues Encountered

- **호스트 8080 이중 LISTEN (신규 발견, HIGH).** `httpd.exe`(PID 5140)와 Docker 가 **동시에** `0.0.0.0:8080` 에 LISTEN 하는 데 성공했고(Windows SO_REUSEADDR 의미론), 출처에 따라 응답자가 갈렸다 — `localhost:8080` → Spring / 컨테이너 gateway → Apache. **Linux 였다면 바인드 실패로 즉시 드러났을 조건이 조용히 통과했다.** 이것이 A2 측정을 오염시킨 실체다. 결정 = (c) 이월.
- **`--wait` acceptance 는 조건부 충족.** 플랜의 `docker compose --profile full up -d --wait` exits 0 은 **문서 그대로는 exit 1**(grafana 3001)이고, 오버레이를 얹었을 때만 0이다. 3001 점유 해소 시 재확인 필요.
- **Gradle `compileJava UP-TO-DATE` 가 원복 검증을 의심스럽게 만듦.** 프로브 소스를 지웠는데 컴파일이 UP-TO-DATE 로 보고돼, 낡은 `.class` 잔존/테스트 캐시 서빙 두 위조 경로를 각각 실측(`ls` + XML `tests="4" failures="0"`)으로 배제했다.

## User Setup Required

None — 외부 서비스 설정 없음. 다만 **operator 잔여 UAT 5항목**이 `01-HUMAN-UAT.md` 에 `status: partial` 로 열려 있다.

## Next Phase Readiness

**01-08 로 넘어갈 준비:**
- 반사실 로그가 CI 게이트 도달성 확인(01-08)의 전제를 제공한다 — 로컬 훅이 defense-in-depth 임이 문서화됐고, 실 boundary 인 CI 히스토리 스캔의 첫 발화가 01-08 관측 대상이다.
- 이미지 `moabom-app:local` 이 존재하므로 full 프로필 재기동 비용이 낮다.

**이월되는 것:**
- **[Phase 2 착수 전, HIGH]** 호스트 8080 정리 (operator: httpd 정지 or 포트 이동 결정). dev 모드(`bootRun`)가 이 문제를 그대로 받는다.
- **[operator, MEDIUM]** `01-HUMAN-UAT.md` U-1~U-5.
- **[Phase 3, MEDIUM]** `app` 의 `0.0.0.0:8080` 발행 근거 소실 — 루프백 조임 재검토.
- **[Phase 6, LOW]** Grafana 대시보드는 두 scrape job 을 **합집합으로** 질의해야 한다.

**정직 표기:** 알려진 결함 N=3(ADR §Consequences) + 환경 결함 1건 + UAT 잔여 5항목. 자동 회귀 커버는 핀 이탈·모듈 경계·미디어 히스토리에 한정되며, 호스트 포트 점유·EOL·CVE 등장은 **어떤 게이트도 잡지 않는다**. 잔여 리스크는 비-제로다.

## Self-Check: PASSED

- 산출물 6종 전부 디스크에 존재 (created 2 / modified 3 / SUMMARY 1)
- 태스크 커밋 3건 전부 이력에 존재: `7904a0b` · `0565b71` · `f05329c`
- 프로브 잔여물 0: `git log --all --diff-filter=A --name-only | grep -c probe` → `0`

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
