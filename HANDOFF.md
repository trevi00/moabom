# HANDOFF.md — 모아봄 (moabom) 인수인계

> **위치**: `<clone-root>/HANDOFF.md` (원 머신에서는 `D:/moabom/HANDOFF.md`). 원격 = https://github.com/trevi00/moabom (public, 기본 브랜치 `main`).
> **다음 세션 진입 문장** (`/clear` 직후, 절대경로로): `<clone-root>/HANDOFF.md 읽고 이어서 진행해줘`
> **작성**: 2026-08-16. 정본 우선순위 = 코드/커밋 > `.planning/STATE.md`(GSD 자동 갱신) > 이 문서. 충돌 시 코드·STATE가 이긴다 — **단 §5.4에 열거한 STATE stale 4건은 이 문서가 옳다** (STATE는 GSD 관리 파일이라 이 세션에서 손대지 않았다).
> **문서 규율**: 이 저장소는 public이다. 로컬 비밀값(`.env` 내용)·개인 자격증명은 이 문서에 없다. `.env.example`이 유일한 공개 기준값이다.
> **검증**: 이 문서의 사실 주장은 작성 직후 4-렌즈 적대 검증(사실·재현성·다음단계 정합·공개 안전성)을 거쳐 정정됐다. 잔여 불확실은 §10.

---

## 0. 30초 요약

| 항목 | 상태 (2026-08-16 실측) |
|---|---|
| 프로젝트 | 넷플릭스 클론 포트폴리오 — Netflix TechBlog 패턴의 정직한 축소 이식 (`.planning/PROJECT.md`) |
| 스택 (승인·핀) | Java 17 · Spring Boot **3.2.12** · Spring Modulith **1.1.12** · JPA · MySQL 8.0 · Next.js **16.2.12** · docker compose(mysql·nginx·prometheus·grafana) · GitHub Actions. **hls.js는 P2 도입 예정(현재 미설치)** |
| 로드맵 | 8 phases / v1 요구사항 **58개** 전량 매핑 (`.planning/ROADMAP.md`, `.planning/REQUIREMENTS.md`) |
| 진행 | **Phase 1 완료** (8/8 플랜, 2026-08-06) — 검증 상태 `human_needed` (자동 gap 0, 사람 검수 U-1~U-4 완료, **U-5 잔여**) |
| 다음 | **Phase 2 — 미디어 재생 수직 슬라이스(앱 Java 0줄)**. 착수 전 **사람 선행 1건(H2) + operator 결정 3건(H1·H3·H4)** (§5.2) |
| CI | `ci.yml` main push 트리거, 최근 런 전부 green — 단 **2회차 이후 초록은 테스트 실행 증거가 아님**(build cache, §5.2 H4) |
| 이 문서와 함께 push되는 것 | 로컬 커밋 `897adaa`(UAT U-1~U-4 operator 확인 기록) — 원격에 없던 1커밋 |

---

## 1. 지금까지 한 일 (타임라인)

| 날짜 | 무엇 | 근거 |
|---|---|---|
| 2026-08-05 | `PROJECT-BRIEF.md` 사용자 승인 — M1 = MVP 7종 전체, FE = Next.js+hls.js, 서비스명 모아봄, `.claude/tech-stack.yaml` 선언 | `PROJECT-BRIEF.md` §6, 커밋 `0dbeabd`·`80c7863` |
| 2026-08-06 | `/kha-new-project` (auto 흐름): config → PROJECT.md → 리서치 4차원(STACK/FEATURES/ARCHITECTURE/PITFALLS, Netflix TechBlog 원문 13종 실확보) → SUMMARY → REQUIREMENTS(58) → ROADMAP(8 phases) → 프로젝트 `CLAUDE.md` | 커밋 `af5569d`→`0d07fb3`, `.planning/research/` |
| 2026-08-06 | Phase 1 계획: 리서치(`ea2b9c4`) → 검증전략(`3c2ebb8`) → 컨텍스트/토론 로그(`99007f6`·`83cc7ef`) → 8 플랜 / 5 wave(`306bb95`·`b055576`·`c964e09`) | `.planning/phases/01-foundation-guardrails/01-{RESEARCH,VALIDATION,CONTEXT,DISCUSSION-LOG}.md` |
| 2026-08-06 | Phase 1 실행 8/8 (총 143분): 저장소 가드레일 → Modulith 8모듈 골격+`verify()` → Next 골격+단일 진입 오리진 → Flyway 스키마+한글 골든 시드 → compose 토폴로지 → ADR 2종+CI 게이트+Dependabot → 반사실 프로브+full 실기동 → **GitHub public + CI 첫 런** | 커밋 `3982e76`→`b881329`, 각 `01-0N-SUMMARY.md` |
| 2026-08-06 | 코드리뷰(`01-REVIEW.md`: warning 3·info 2, 미수정) · 검증(`01-VERIFICATION.md`: human_needed) · STATE/PROJECT 진화 | 커밋 `f5cd9e4`·`baf94ce`·`972da92` |
| 2026-08-06 | operator 육안 검수 U-1~U-4 확인 (U-5 잔여) — 파일 수정은 08-06, 커밋은 08-16 | 커밋 `897adaa` (이 문서와 함께 push) |
| 2026-08-06 ~ 08-13 | Dependabot PR: #2(08-06) · #4/#5/#6(08-13) 오픈, #1/#3은 상위 버전 PR로 대체·닫힘 — 사람 판단 대기 (§5.3) | GitHub PRs |
| 2026-08-10 | 원 머신에 ffmpeg/ffprobe **9.0**(WinGet Gyan 빌드) 설치됨 — STATE의 "PATH 부재" 블로커는 그 이전 실측 (§5.2 H1) | `ffmpeg -version` 2026-08-16 재실측 |
| 2026-08-16 | 이 HANDOFF.md 작성 + 4-렌즈 적대 검증 반영. 리포는 **public 유지** 결정 | 본 커밋 |

---

## 2. 산출물 지도 (무엇이 어디에)

```
<clone-root>/
├── PROJECT-BRIEF.md              승인된 브리프 (입력 문서, [확정]/[제안]/[열림] 표기)
├── HANDOFF.md                    ← 이 문서
├── README.md                     기동/리셋/클론후1회/포트/미디어 격리 — 운영 정본
├── CLAUDE.md                     GSD 자동생성 프로젝트 지침 (GSD Workflow Enforcement 포함)
├── .claude/tech-stack.yaml       java/springboot 3.2 + mysql 8.0 + jpa (하네스 스킬 필터)
├── .planning/
│   ├── PROJECT.md · REQUIREMENTS.md · ROADMAP.md · STATE.md · config.json
│   ├── research/{STACK,FEATURES,ARCHITECTURE,PITFALLS,SUMMARY}.md
│   └── phases/01-foundation-guardrails/   PLAN×8 · SUMMARY×8 · RESEARCH · VALIDATION · CONTEXT
│                                          · DISCUSSION-LOG · COUNTERFACTUAL-LOG · CI-GATE-LOG
│                                          · REVIEW · VERIFICATION · HUMAN-UAT
├── docs/adr/0001-runtime-versions.md      Boot 3.2.12 EOL 수용(§2)·핀·CVE 가시성·bootBuildImage
├── docs/adr/0002-media-and-encoding.md    미디어 4중 격리·ASCII slug·UTF-8 9지점·이벤트 테이블 소유권·nginx 규약
├── backend/                      Gradle 8.14.5 wrapper · Modulith 8모듈 골격 · Flyway V1/V2 · 테스트 2종(10 케이스)
├── frontend/                     Next 16.2.12 App Router 골격 · rewrites 단일 진입 · standalone Dockerfile
├── compose.yaml                  기본 프로필(인프라 4종) + `--profile full`(app·web)
├── infra/nginx · infra/prometheus
├── .githooks/pre-commit          미디어 확장자·5MB 차단 (core.hooksPath 설정 필요)
└── .github/workflows/ci.yml · .github/dependabot.yml
```

---

## 3. 새 머신 부트스트랩 (재현 절차)

### 3.1 필수 도구

| 도구 | 버전/근거 | 비고 |
|---|---|---|
| JDK | **17** — **미리 설치해 둘 것**. Gradle toolchain이 17을 요구하지만 `settings.gradle`에 foojay resolver가 없어 **자동 프로비저닝되지 않는다** (다른 JDK만 있으면 `No matching toolchains found` 실패). CI = Temurin 17 | Gradle 자체는 wrapper가 자동 다운로드(8.14.5). **Gradle 9.x 사용 금지**(ADR-0001 §1) |
| Docker | Docker Desktop / Compose **v2** (`docker compose`) | 백엔드 테스트(Testcontainers)와 compose 둘 다 Docker 데몬 필요. 최소 버전 핀 없음(ADR 실측 환경 Docker 28.1.1 / Compose v2.35.1) |
| Node/npm | 저장소에 핀 파일 없음(`.nvmrc`/`engines` 부재). 권고 **Node 24 LTS**(컨테이너 이미지 `node:24-alpine`; Next 16 최소 20.9+) | `frontend/package-lock.json` 커밋됨 → `npm ci` |
| ffmpeg/ffprobe | **Phase 2부터 필요**. 원 머신엔 **9.0**(WinGet Gyan)이 2026-08-10 설치됨 — STACK 지목값(8.0.3 / 7.1.5 LTS)과 다르다 → 새 머신은 `ffmpeg -version`으로 확인하고, 어느 버전으로 갈지 Phase 2 계획에서 결정 | Phase 1 코드에 호출부 0 |
| Claude Code + 하네스 | kha-* 워크플로를 계속 쓰려면 §4 | 없이도 코드 빌드/실행은 가능 |

### 3.2 클론 후 1회 (README §클론 후 1회 2줄 + 미디어 루트 생성 1줄)

```bash
git clone https://github.com/trevi00/moabom.git && cd moabom
git config core.hooksPath .githooks     # 미디어/5MB 커밋 차단 훅 — 로컬 설정이라 클론마다 1회. 안 하면 조용히 안 돈다
cp .env.example .env                    # MEDIA_ROOT · 로컬 비밀번호 기본값. 실제 .env는 커밋 금지(gitignored)
# 미디어 루트 = 저장소 밖 (Windows 예시). POSIX 는 mkdir -p ~/moabom-media/{hls,source} 후 .env 의 MEDIA_ROOT 를 절대경로로 —
# 드라이브 문자 경로를 POSIX 셸에서 그대로 치면 저장소 안에 ./D:/ 디렉터리가 생겨 "저장소 밖" 격리 불변식이 깨진다
mkdir -p D:/moabom-media/hls D:/moabom-media/source
```

- 원 머신의 `.env`는 Grafana admin 비밀번호 1개만 기본값에서 바꾼 상태였다 — **값은 이 문서에 없고 새 머신엔 없다**. `.env.example`(admin)로 시작하면 된다.
- 원 머신의 `D:/moabom-media/{hls,source}`는 **비어 있었다**(실 미디어 0개). 옮길 데이터 없음. Blender 오픈무비 원본(mp4)은 Phase 2가 내려받는다 — 다운로드 URL·해시·해상도 선택 기준은 아직 어디에도 없다(Phase 2 계획 결정 사항).

### 3.3 실행·검증 커맨드 (각 줄은 `<clone-root>` 기준 — 블록을 통째로 붙여 넣지 말 것)

```bash
# 인프라만
docker compose up            # mysql · nginx · prometheus · grafana
docker compose down -v       # 리셋 = 볼륨 삭제 (Flyway가 스키마/시드 단일 진실 — initdb가 아님)

# 백엔드 빌드+테스트 (Docker 필요 — SchemaMigrationTest = Testcontainers mysql:8.0)
(cd backend && ./gradlew build)
#   ⚠️ 2회차부터 :test 가 UP-TO-DATE/FROM-CACHE 로 0건 실행하며 green 이 될 수 있다.
#      실행 증거 = 콘솔의 `> Task :test` 에 FROM-CACHE/UP-TO-DATE 접미사가 없음
#                 + backend/build/test-results/test/TEST-*.xml 의 tests=/failures= 합계 (기대 4+6 = 10, failures 0)
#      — Gradle 콘솔은 전부 통과하면 개수를 출력하지 않는다. 'tests=' 는 XML 에서만 읽힌다.
#      강제 재실행: (cd backend && ./gradlew cleanTest test --no-build-cache)

# 평시 개발 (두 터미널)
(cd backend && ./gradlew bootRun)          # spring-boot-docker-compose 가 인프라 4종 자동 기동
(cd frontend && npm ci && npm run dev)     # http://localhost:3000  (/api→:8080, /media→:8081 rewrite)

# 전체 데모(full 프로필) — 앱 이미지 선행 빌드 1회 (compose는 Gradle을 못 돌린다)
(cd backend && ./gradlew bootBuildImage --imageName=moabom-app:local)
docker compose --profile full up           # 브라우저는 http://localhost:3000 만 (단일 진입 오리진)
curl -s localhost:3000/api/health          # {"status":"UP","app":"moabom",...} 200 이면 성공
```

**호스트 포트 6개**: 3000(web) · 3306(mysql) · 8080(app, 유일한 0.0.0.0 바인드) · 8081(nginx 미디어) · 9090(prometheus) · 3001(grafana). 기동 전 점유 확인 — 원 머신에선 8080(무관 httpd)·3001(다른 프로젝트 grafana)이 점유돼 있었다(§5.2 H3). **새 머신에선 재실측**하고 다르면 STATE의 블로커를 갱신할 것.

### 3.4 반드시 알아야 할 함정 (README/ADR/STATE에 실측 기록됨)

| 구분 | 함정 | 대응 |
|---|---|---|
| 크로스플랫폼 | JDK 17 자동 프로비저닝 없음 (§3.1) | 17을 먼저 설치 |
| 크로스플랫폼 | `BACKEND_ORIGIN`/`MEDIA_ORIGIN`은 **런타임 변수가 아니다** — `next build` 시 rewrites 목적지가 구워진다 | full = compose `web.build.args`가 정본; dev에서 바꾸려면 `frontend/.env` |
| 크로스플랫폼 | `.gitignore`에 전역 `*.ts` 넣지 말 것 — MPEG-TS와 TypeScript 확장자 충돌 | 경로 스코프(`media/**/*.ts`)만 |
| 크로스플랫폼 | Gradle build cache로 테스트 0건 green (로컬·CI 동일) | XML로 tests= 확인 (§3.3, §5.2 H4) |
| 크로스플랫폼 | `bootBuildImage` 빌더 `builder-jammy-base:latest` — 재현성 없음(다이제스트 기록만) | 날짜별 빌드 갈림 사고 1회 시 핀 승격 (ADR-0001 §6) |
| 크로스플랫폼 | 미디어 루트 기본값 `D:/moabom-media`는 Windows 경로 | POSIX는 `.env` MEDIA_ROOT를 절대경로로 (§3.2) |
| Windows | **8080 이중 LISTEN**: 다른 프로세스와 Docker가 동시 LISTEN에 성공해 출처별 응답자가 갈린다(localhost=Spring / 컨테이너 gateway=Apache) | `netstat -ano \| grep ':8080 '` LISTEN 줄이 2개인지 |
| Windows | Git Bash MSYS 경로 변환 — `/`로 시작하는 grep 패턴·docker `-v` 컨테이너측 경로가 변환됨. 역방향: `MSYS_NO_PATHCONV=1` 켠 채 `-f /c/Users/...`를 주면 이번엔 변환이 안 돼 실패 | 컨테이너측 경로엔 켜고, 호스트 경로 인자는 `C:/...` 형식 |
| Windows | Python 검증 스크립트 stdout cp949 UnicodeEncodeError | `sys.stdout.reconfigure(encoding='utf-8')` |
| Windows | 파일명·ProcessBuilder 인자 cp949 사고 | ADR-0002 §2 **ASCII slug 정책**(`[A-Za-z0-9._-]`), 한글은 DB `title.title_ko`에만; `sun.jnu.encoding=UTF-8` |
| Windows | 바인드 마운트 sendfile 절단 | `infra/nginx/default.conf` `sendfile off;` (이미 반영) |
| Windows | `gradlew` CRLF로 커밋되면 ubuntu 러너에서 `sh\r` 사망 | `.gitattributes` `eol=lf` + exec bit 100755 (이미 반영) |

---

## 4. Claude Code 하네스 의존 — 저장소와 함께 **가지 않는** 것

이 프로젝트는 operator의 Claude Code 하네스(`~/.claude`)의 **kha-\* GSD 워크플로**로 진행됐다. 코드는 하네스 없이도 빌드·실행되지만, `/kha-plan-phase`·`/kha-execute-phase` 등을 계속 쓰려면 새 머신에 하네스가 있어야 한다.

| 항목 | 이동 여부 | 새 머신 조치 |
|---|---|---|
| kha-* 커맨드 + `get-shit-done/bin/gsd-tools.cjs` | ✗ (하네스 소속) | **(a) 권장** 공개판 https://github.com/trevi00/khaness 를 `~/.claude`로 clone 후 `bash install.sh` (kha 스킬 69종 + gsd-tools 1.34.2 번들 확인, 2026-06-22 스냅샷). ⚠️ **Claude Code를 한 번이라도 실행한 머신은 `~/.claude`가 이미 존재해 `git clone … ~/.claude`가 실패한다** — 첫 실행 전에 clone하거나, 기존 디렉터리에서 `git init && git remote add origin … && git fetch && git checkout -f main`으로 합칠 것. `install.sh`와 생성되는 훅은 `python`(3.10+; 원 머신 3.13) 명령을 전제한다 — `python3`만 있으면 alias 필요(없으면 훅이 조용히 안 돈다). **(b)** 플러그인 `claude plugin marketplace add github:trevi00/khaness && claude plugin install khaness@khaness` — kha 워크플로가 `$HOME/.claude/get-shit-done/bin/gsd-tools.cjs` **리터럴 경로**를 호출하므로 플러그인 모드에서 이 경로가 채워지는지 **미실측**. **(c)** operator 본인이면 운영본(private, 최신)을 clone — 원 머신엔 미커밋 수정이 있었고 moabom에 필수인지는 미확인 |
| `settings.json` 훅 (원 머신 25종; 공개판 템플릿 21종 — 차이 4종은 moabom 워크플로에 필수 아님) | ✗ (절대경로 `C:/Users/user/...`) | 원본 복사 금지 — `install.sh`가 템플릿에서 재생성. Python 3.10+ · PyYAML 필요 |
| 하네스 L4 메모리(`~/.claude/projects/.../memory/project_moabom_netflix_clone.md`) | ✗ (gitignore, brain 스냅샷 대상 아님) | **이 문서 §5·§6이 그 내용을 대체한다** — 별도 복원 불필요 |
| brain 스냅샷(L1/L2) | △ (하네스 private 원격 접근 전제) | 선택: `cd ~/.claude/scripts && python -m cli.brain_snapshot restore`. moabom 특정 정보는 없음 |
| `.planning/config.json` (자동화 자세) | ✓ 커밋됨 | 그대로 재현: `mode=yolo · granularity=standard · model_profile=quality · research/plan_check/verifier/nyquist/code_review 전부 on · auto_advance on · ui_phase on · ui_safety_gate on · discuss_mode=discuss · branching=none`. **auto_advance/ui_phase의 함의는 §6 참고** |
| 프로젝트 `CLAUDE.md` GSD Workflow Enforcement | ✓ 커밋됨 | 파일 변경 전 `/kha-run-adhoc` · `/kha-debug` · `/kha-execute-phase` 로 진입 (사용자 명시 우회 지시 없으면 직접 편집 금지) |
| `.env` · `core.hooksPath` · `D:/moabom-media` | ✗ | §3.2 |

---

## 5. Phase 1 결과와 잔여 (전수)

### 5.1 검증 상태

- `01-VERIFICATION.md`(원문): `status: human_needed`, `score: 4/4 roadmap 성공기준 verified (+ ARCH-01/ARCH-05 요구사항 2/2 satisfied)`, 코드 레벨 gap 0. human_needed의 유일 사유 = `01-HUMAN-UAT.md` (status: partial).
- `01-HUMAN-UAT.md`: U-1(`:3000` 육안)·U-2(`:9090/targets`)·U-3(Grafana 로그인)·U-4(반사실 로그 잔여 표기 수용) = ✅ operator 2026-08-06 (커밋 `897adaa`). **U-5 = ADR-0001의 EOL 스택 수용 근거가 리뷰어에게 설득력 있는지 판단 — 미확인.** ⚠️ 상류 문서(HUMAN-UAT·VERIFICATION)는 이를 "ADR-0001 §9"로 가리키지만 **§9는 "실측으로 닫힌 가정"이고 EOL 수용은 §2 "EOL 인지·수용"이다** — U-5를 닫을 때 §2를 읽고, HUMAN-UAT의 포인터도 함께 고칠 것.
- **U-5를 닫는 실제 경로**: `01-HUMAN-UAT.md` 체크 + frontmatter `partial→complete`를 `/kha-run-adhoc`로 편집. **`/kha-verify-uat 1`은 별도 UAT 세션 파일(`01-UAT.md`)을 만들 뿐 `01-VERIFICATION.md`의 `human_needed`를 `passed`로 바꾸는 경로가 GSD 워크플로 어디에도 없다** (HUMAN-UAT는 체크박스 형식이라 verify-uat의 `result: [pending]` 탐색에도 걸리지 않는다). VERIFICATION 상태를 바꾸려면 kha-verifier 재실행 또는 근거를 붙인 명시 편집 — 또는 `human_needed`를 최종 상태로 수용.
- ROADMAP 성공기준 4종 ✓ (한 커맨드 기동+health 200 · Modulith 8모듈 verify green+반사실 · Flyway 재현+한글 시드+attribution/license · 미디어 격리 4중+push protection). ARCH-01·ARCH-05 체크 완료(58 중 2). 리뷰어가 반대할 수 있는 지점: ARCH-05는 원 머신 3001 점유 때문에 **오버레이 재매핑(3002)으로 검증**됐다 — caveat로 체크됨.
- 반사실 프로브 2건 PASS(`01-COUNTERFACTUAL-LOG.md`): 경계 위반 주입 → `verify()` 실패 / 미디어 확장자·5MB 커밋 → 훅 거부. 알려진 한계 = `--no-verify` 우회 가능 · 실 boundary는 CI 히스토리 스캔 · `core.hooksPath` 미설정 클론엔 훅 없음.
- CI 첫 런(run 31077663272) 게이트 8종 실 실행 로그 증명(`01-CI-GATE-LOG.md`). CI 층 참 양성 관측은 0회(의도적 — 공개 이력 오염 회피).

### 5.2 Phase 2 착수 전 처리 대상 (STATE 태그 `Phase 2 착수 전, HIGH` — H2만 `Phase 2 착수 조건, HIGH`)

| # | 항목 | 성격 | 상태 (2026-08-16) |
|---|---|---|---|
| H1 | **ffmpeg/ffprobe 버전 결정** — 원 머신엔 **9.0**(WinGet Gyan, 08-10 설치)이 있어 "PATH 부재"는 해소됐으나 STACK 지목값(8.0.3 / 7.1.5 LTS)과 다르다 | operator 결정 (9.0 수용 여부) → STATE 블로커 갱신 | 새 머신은 설치 여부부터 재확인 |
| H2 | **Apple HLS Authoring Spec 원문 + ffmpeg 공식 HLS muxer 문서 확보** — 세그먼트 길이·비트레이트 사다리·GOP/키프레임 수치 근거. 리서치 4문서 전부 WebFetch 실패(JS 렌더 페이지) | **사람 선행** (로드맵 명시 착수 조건) | 미확보. STACK 예시의 `-hls_time 6`도 미검증 값 |
| H3 | **호스트 8080 이중 LISTEN + 3001 점유** 정리 (httpd 정지 or 앱 포트 이동) | operator 결정 | 원 머신 환경 조건 — 새 머신에서 재실측 후 결정/해소 |
| H4 | **CI 2회차 이후 `:test FROM-CACHE`** → 초록이 실행 증거 아님. 해소안 (a) 테스트만 캐시 배제(`test { outputs.upToDateWhen { false } }` 등, +~1.5분/런) (b) test-results XML 개수 하한(10) 단언 (c) 수용. **권장 (a)+(b)** | operator 결정 → Phase 2 첫 CI 증설(FE 빌드·E2E 게이트 후보)과 함께 | `ci.yml` 미수정 |

### 5.3 operator 대기 (Phase 2와 무관하게 열려 있음)

| 항목 | 내용 | 기록 위치 |
|---|---|---|
| U-5 | ADR-0001 **§2** EOL 수용 근거의 설득력 판단 (청중 판정) — 닫는 경로는 §5.1 | `01-HUMAN-UAT.md` |
| LICENSE | 파일 부재 — public 리포가 all-rights-reserved로 해석됨. 라이선스 선택은 사용자 결정(임의 추가 안 함) | STATE, `01-08-SUMMARY.md` |
| Dependabot PR 4건 (2026-08-16 기준 OPEN: #2 08-06 · #4/#5/#6 08-13 오픈; #1/#3은 상위 버전 PR로 대체·CLOSED) | #5 next 16.2.12→16.3.0 (**npm audit high 3건의 유일 수정 경로 vs 잠긴 핀 충돌** — 의도적으로 ignore 안 함, 사람이 판단) · #4 gradle-wrapper 8.14.5→9.7.0 (**ADR-0001은 Gradle 9.x 미지원 판정 → 머지 금지 후보**) · #6 @types/node 20→26 · #2 typescript 5.9→7.0. 자동 머지 없음. 결정 미기록 | GitHub PRs, `.github/dependabot.yml`, ADR-0001 §3 |
| 코드리뷰 warning 3 (미수정) | WR-01 `.githooks/pre-commit` `$staged` 따옴표 없음 → 공백 파일명이면 5MB 게이트 무력화 · WR-02 `ci.yml` 히스토리 스캔이 mpg/mpeg 누락 + `.ts` 경로스코프 부재 · WR-03 `frontend/.dockerignore`에 `.env*` 미제외. Fix 코드 제안 포함 | `01-REVIEW.md` (`/kha-remediate-code-review` 대상) |
| 코드리뷰 info 2 | IN-01 compose app healthcheck 부재(web depends_on 준비 미보장) · IN-02 ci.yml 글롭 0건 시 원시 오류 | `01-REVIEW.md` |
| Phase 9~ 증설 미등록 | D-00 진화 서사(부하 주입 → 병목 실측 → Redis → 읽기 복제본 → 모듈 1개 물리 분리)가 ROADMAP에 없음 — `/kha-add-phase` 로 반영("지금이 그 시점") | `01-CONTEXT.md`, `01-08-SUMMARY.md` |
| 3001 해소 후 재확인 | 오버레이 없는 `docker compose --profile full up -d --wait` 성공 확인 | `01-08-SUMMARY.md` ARCH-05 caveat |

### 5.4 이월 항목 (소유 phase) + STATE stale 목록

- **P2**: nginx 캐시 헤더(m3u8 `max-age=10` / 세그먼트 `immutable`) 실검증 · ASCII slug 규칙(중복·길이 상한) 확정 + slug 검증 코드 게이트 승격 · CI 증설 시작점 — FE 빌드 게이트 · 테스트 개수 하한 단언 · **E2E(Playwright)는 P2부터**(01-CONTEXT D-09)
- **P3**: `spring-modulith-starter-jpa` + `event_publication` 마이그레이션(같은 커밋) · `spring-boot-starter-security` · `spring-session-jdbc` · Modulith 1.1.x 이벤트 프로퍼티 키 이름 실측 · app `0.0.0.0:8080` 발행 근거 상실 → 루프백 조임 재검토
- **P3~P5**: `springdoc-openapi-starter-webmvc-ui:2.3.0`
- **P6**: Grafana 대시보드는 `moabom-app-container`/`-host` 두 job 합집합(하나는 항상 DOWN이 설계) · resilience4j 2.2.0 / Boot 3.2.12 핀 재확인(STATE 체크포인트)
- **P7**: title FULLTEXT ngram 인덱스(측정 전 선점 금지)
- **P8**: resilience4j/Boot 핀 재확인(STATE 체크포인트는 P6·**P8** 둘 다) · `-progress pipe:1` `out_time_ms` 단위 · 이벤트 재발행 키 · JobRunr–Boot 3.2 대안 트립와이어 미검증 · **필수 예방책 3종**(ffmpeg stderr 드레인+`waitFor(timeout)` · 인자 SSRF/주입 방어 · Windows cp949)
- **STATE.md stale 4건** (커밋 `897adaa`가 HUMAN-UAT만 고치고 STATE는 안 고쳤다 — 다음 세션이 GSD 커맨드로 정리할 것):
  1. `## Session Continuity` 의 `Next: /kha-execute-phase 1 (01-08-PLAN.md …)` — 01-08 완료 이전 문구. **따르지 말 것**(`/kha-status`는 frontmatter `phase_1_complete` 기준으로 정상 안내한다)
  2. "UAT **5항목** operator 이월" 표기 3곳(frontmatter `stopped_at` · Current Position · Blockers `U-1~U-5 미확인`) → 실제는 **U-5 1항목**
  3. Blockers `[Plan 08, MEDIUM] ubuntu CI 첫 런이 Flyway 한글 인코딩의 실검증 지점` → 01-08에서 `koreanTitlesRoundTrip` 통과로 **해소됨**
  4. Blockers `[Plan 08, MEDIUM] CI 2회차 … 가능성(미실측)` → `[Phase 2 착수 전, HIGH · 01-08 신규 실측]`로 **대체됨**(둘 다 열려 있음)
  - (+ H1 ffmpeg 블로커는 "PATH 부재"에서 "버전 9.0 수용 여부"로 갱신 필요)

---

## 6. 다음 할 일 — Phase 2 진입 패키지

**Phase 2: 미디어 재생 수직 슬라이스 (앱 Java 0줄)** — `.planning/ROADMAP.md` §Phase 2
- **Goal**: 손으로 만든 HLS 자산이 브라우저에서 어댑티브 재생되고 새로고침 후 그 지점부터 재개된다 — 최고 위험(키프레임 정렬·CORS·Windows 경로·hls.js 라이프사이클)을 DB·관리자 UI·검색 없이 선제 해소.
- **Requirements**: PLAY-01(1080/720/480 3렌디션 ABR) · PLAY-02(hls.js + Safari 네이티브 폴백) · ARCH-03(nginx 정적 HLS + m3u8/세그먼트 캐시 분리) · INGST-01(배치 인제스트 스크립트 — 전 페이즈 중 가장 이른 산출물)
- **Success Criteria 5**: ① 대역폭 조이면 1080→720→480 전환이 끊김 없이 ② 같은 URL이 Chrome(hls.js)·Safari(네이티브) 양쪽 재생 ③ 15초 재생 후 새로고침 → 그 지점 재개(이 phase는 **클라 저장**, 서버 저장은 P4) ④ `EXTINF` 정렬 검증 스크립트가 3렌디션 세그먼트 경계 일치를 자동 보고(`mediastreamvalidator`는 macOS 전용) ⑤ 배치 스크립트로 오픈무비 N편을 동일 사다리로 일괄 산출
- **Research 플래그 HIGH**: H2(§5.2). 리서치 SUMMARY의 phase 번호는 9-phase 기준이라 로드맵과 1씩 어긋난다(리서치 P1=로드맵 P2, P6=P7, P7=P8).
- **구속 규약(ADR-0002)**: 산출물 경로 ASCII slug만 · 미디어는 저장소 밖 · nginx `:ro` + `sendfile off` + 캐시 수명 분리 · 이벤트 테이블은 Flyway 소유 · starter-jpa/security/session/springdoc은 P3까지 미도입(**앱 Java 0줄 유지**)
- **자산**: Blender 오픈무비 5편(Big Buck Bunny·Sintel·Tears of Steel·Elephants Dream·Cosmos Laundromat, CC BY 3.0) 메타는 `V2__seed_golden.sql`에 이미 있음. 원본/HLS 파일은 0개 — 다운로드 출처·해상도·해시는 P2 계획이 확정.
- **STACK 참고 커맨드**(미검증 값 포함): `-f hls -hls_time 6 -hls_playlist_type vod -hls_segment_type fmp4 -master_pl_name master.m3u8 -var_stream_map "v:0,a:0 v:1,a:1 v:2,a:2"`; 실패 조건 = `-hls_time`을 GOP와 맞추지 않으면 세그먼트 경계가 키프레임에 안 맞음.
- **진입 커맨드와 자동 연쇄 주의**: 착수 조건 충족 후 `/kha-clarify-phase 2`. **`config.json`이 `auto_advance: true`라 이 한 커맨드가 CONTEXT 작성 직후 `kha-plan-phase 2 --auto` → `kha-execute-phase 2 --auto`까지 자동으로 이어지고, auto 모드에서는 `checkpoint:human-verify`가 자동 승인된다** — Phase 1이 정확히 그렇게 진행돼 UAT 5항목이 operator로 이월됐다. Phase 2 성공기준 ①②(끊김 없는 전환·Safari 재생)는 사람 눈/브라우저 판정이므로, **사람이 직접 게이트하려면 진입 전 `node ~/.claude/get-shit-done/bin/gsd-tools.cjs config-set workflow.auto_advance false`** 로 끄거나, 실행 후 UAT를 계획에 넣을 것. 또한 `ui_phase`/`ui_safety_gate`가 on이고 ROADMAP P2가 `UI hint: yes`라 plan 단계의 UI 게이트가 `kha-spec-ui-phase 2`를 (auto면 자동, 수동이면 프롬프트로) 발화한다 — ROADMAP은 P2를 "행 UI가 아니라 기능 스파이크, 목업 sign-off 대상 아님"으로 명시했으니 그때 최소로 처리.

---

## 7. 로드맵 전체 (P2~P8) + 확정 결정

| Phase | 이름 | Goal(요약) | Req | Depends | Research |
|---|---|---|---|---|---|
| 2 | 미디어 재생 수직 슬라이스 | 손 HLS 자산 ABR 재생 + 새로고침 재개 | 4 | P1 | **HIGH** |
| 3 | 카탈로그 + 계정/프로필 | 1계정 N프로필 격리 축 + 발행 상태 카탈로그, P2 자산 시딩 | 8 | P1,P2 | skip |
| 4 | playback 도메인 | 서버 저장 이어보기 + 완료 임계값 + 자막 + 오류 복구 (Core Value 폐곡선) | 7 | P3 | skip |
| 5 | BFF v1 + 행 기반 홈 | `/api/home` 완성 lolomo JSON + 넷플릭스 문법 행 홈 (**목업-우선 사람 sign-off**, `/kha-spec-ui-phase 5` 권장) | 12 | P4 | skip |
| 6 | 추천 v1 + 복원력 + 관측 | 이력 기반 개인화 + 추천 실패 시 비개인화 폴백 + Grafana | 6 | P5 | skip (resilience4j 핀 체크포인트) |
| 7 | 검색 + 마이리스트 | 즉시 검색(**ngram vs LIKE 실측 후 채택**) + 마이리스트 3진입점 + 행 세트 6종 완성 | 8 | P6 | MEDIUM |
| 8 | 인제스트 제품화 | 관리자 잡 파이프라인 + lease/reconciler/재시도 상한 + 산출물 검증 | 11 | P3,P5 | MEDIUM |
| 9~ | (미등록) D-00 진화 서사 | 부하 주입 → 병목 실측 → Redis → 읽기 복제본 → 모듈 물리 분리 | — | — | `/kha-add-phase` 대기 |

**순서 불변식**: P5 비개인화 조립기가 P6 서킷브레이커의 **폴백 구현체 그 자체** — 뒤집기 금지.
**확정 결정 (ROADMAP §Ordering Decisions)**: **D1** 인제스트 제품화 = P8 유지 + 스코프는 PITFALLS 기준(lease reconciler·재시도 상한 3회·DLQ·산출물 검증을 첫 구현에 포함, 증분 패치 금지; 받아들인 리스크 = `TitleAssetPublished` 이벤트 계약 실 배선이 P8까지 미검증). **D2** 검색 = P7(추천 이후, 마이리스트와 묶음; 받아들인 리스크 = P5~6 구간 "검색 없는 데모").
**Key Decisions 진화(PROJECT.md)**: Modulith 모듈리스 = ✅ P1 실증(8모듈 verify green + 경계 위반 반사실 차단) · MySQL 8.0+JPA = ✅ P1 착지(Flyway 단일 진실 + 파라미터 실효 Testcontainers 실측).

---

## 8. 다음 세션 진입 프롬프트 (3-tier)

**1순위 (권장)** — 환경 검증 → STATE 정리 → Phase 2 착수:
```
<clone-root>/HANDOFF.md 읽고 이어서 진행해줘.
먼저 §3.2~3.3을 이 머신에서 실행해 부트스트랩을 검증하고
(gradlew build 는 콘솔의 :test 캐시 접미사 부재 + backend/build/test-results/test/TEST-*.xml 의 tests= 합계 10 으로 판정,
 docker compose --profile full config -q, 포트 6개 점유, ffmpeg -version),
§5.2 H1~H4 와 §5.4 의 STATE stale 4건을 이 머신 기준으로 GSD 커맨드(/kha-run-adhoc)로 정리해줘.
H2(HLS 스펙 원문)는 사람 몫이니 준비됐는지 나에게 묻고, 충족되면 §6 의 auto_advance 주의를 확인한 뒤 /kha-clarify-phase 2 로 진입.
```

**Fallback A** — U-5 닫기: ADR-0001 **§2** 판단 → `/kha-run-adhoc`로 `01-HUMAN-UAT.md` 체크 + frontmatter complete (+ §9→§2 포인터 정정). `/kha-verify-uat 1`은 VERIFICATION 상태를 바꾸지 않음(§5.1)
**Fallback B** — 로드맵 증설: `/kha-add-phase` 로 Phase 9~ (D-00 진화 서사) 등록 — 스코프는 논의 필요
**Fallback C** — 소규모 하드닝: `/kha-remediate-code-review` (WR-01~03) 또는 `/kha-run-adhoc` 로 CI 캐시 구멍 (a)+(b) 적용 + Dependabot PR 4건 판단 기록
**트리거 키워드(최소)**: `<clone-root>/HANDOFF.md 읽고 이어서 진행해줘`

---

## 9. Reference commits (`main`)

| 범위 | 커밋 |
|---|---|
| 브리프·tech-stack | `0dbeabd` · `80c7863` |
| /kha-new-project | `af5569d`(config) · `e9820cb`(PROJECT) · `d7de29a`(REQUIREMENTS) · `2b77349`(research) · `0d07fb3`(ROADMAP 8 phases + CLAUDE.md) |
| Phase 1 계획 | `ea2b9c4`(RESEARCH) · `3c2ebb8`(VALIDATION) · `99007f6`(CONTEXT/DISCUSSION-LOG) · `83cc7ef` · `306bb95` · `b055576` · `c964e09` |
| Phase 1 실행 (wave 1→5) | `3982e76`…`f61d710`(01-02) … `fb0b8a7`(CI 게이트) … `0565b71`(full 실기동) … `b881329`(public+CI 첫 런) · `2ce31df` |
| 리뷰·검증·상태 | `f5cd9e4` · `baf94ce` · `972da92` |
| UAT U-1~U-4 · HANDOFF | `897adaa` · (이 문서 커밋 — `git log -1 -- HANDOFF.md`) |

---

## 10. 알려진 결함 N · 잔여 리스크 (정량, 비-제로)

- **알려진 결함 N = 4** (`01-08-SUMMARY.md` 잔여 리스크): CI 2회차+ `:test` 캐시 서빙(HIGH) · 테스트 개수 단언 부재 · Modulith OPEN guard는 1.1.12에서 발화 불가(전방 게이트, 1.2+ 상향 시 활성) · ARCH-05 "문서대로 한 커맨드 기동"이 원 머신에서 오버레이 없이는 미충족(포트 점유).
- **미수정 리뷰 항목 5** (warning 3 + info 2), **operator 판단 대기 3** (U-5 · LICENSE · Dependabot 4 PR), **STATE stale 4** (§5.4).
- **CI 게이트 실행 관측 1/1 런**, CI 층 참 양성 0회. 어떤 게이트도 잡지 않는 것: 호스트 포트 점유 · EOL 도래 · 신규 CVE · ASCII slug 정책 · UTF-8 9지점 · nginx 캐시 헤더 · 워크플로 파일 자체의 삭제/약화.
- **문서 신뢰도**: 이 문서의 사실은 2026-08-16 원 머신 실측(파일·git·`gh` 라이브·`ffmpeg -version`)이며 4-렌즈 적대 검증으로 정정됐다. **미실측으로 남긴 것**: 하네스 플러그인 모드의 gsd-tools 경로 충족 여부 · 새 머신 고유 상태(포트·Docker 파일 공유·JDK) · Gradle 9.x가 실제로 빌드를 깨는지(ADR 스스로 미검증 표기). → §8 1순위 프롬프트가 첫 일로 재실측한다.
- **하네스 자체 개선 항목(프로젝트 밖)**: 플래너 자기모순 6회 재발 → 플랜 생성 단계 정적 게이트 필요(`kha-plan-phase`) · auto_advance 하의 human-verify 자동 승인이 UAT를 사람 이월로 만드는 구조(§6).

---
*Handoff written 2026-08-16 · project state as of commit `897adaa` (Phase 1 complete, Phase 2 not started)*
