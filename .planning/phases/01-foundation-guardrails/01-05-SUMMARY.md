---
phase: 01-foundation-guardrails
plan: 05
subsystem: infra
tags: [docker-compose, profiles, nginx, prometheus, grafana, mysql, ngram, hls-cache, host-gateway, counterfactual-probe, msys-pathconv]

# Dependency graph
requires:
  - phase: 01-01
    provides: "저장소 밖 미디어 루트 D:/moabom-media + .gitignore(.env 차단) + README(선행 빌드 1줄·클론 후 1회 절차)"
  - phase: 01-02
    provides: "backend/application.yaml — management.endpoints.web.exposure.include=health,info,prometheus (Actuator 노출) + spring.docker.compose.file=../compose.yaml"
  - phase: 01-03
    provides: "frontend/Dockerfile — BACKEND_ORIGIN/MEDIA_ORIGIN 을 ARG 로 받는 standalone 이미지 (rewrites 목적지 build-time 고정)"
  - phase: 01-04
    provides: "SchemaMigrationTest.withCommand — compose 가 미러링해야 할 mysql 기동 파라미터 3종의 정본"
provides:
  - "compose.yaml — 단일 파일 + profiles 이원화 (무프로필 4종 / --profile full 6종), name: moabom"
  - "mysql 기동 파라미터 3종 핀 (ngram_token_size=2 · utf8mb4 · utf8mb4_0900_ai_ci) — SchemaMigrationTest 와 문자 그대로 일치 실측"
  - "app: image + pull_policy never (CP-3 A안) — 이미지 부재를 레지스트리 인증 오류가 아닌 '이미지 없음' 으로 즉시 실패"
  - "web: build.args 로 목적지 주입 (01-03 실측 반영 — 런타임 environment 는 무효)"
  - "infra/nginx/default.conf — sendfile off + root /srv + m3u8/세그먼트 캐시 정책 분리 + /healthz"
  - "infra/prometheus/prometheus.yml — host.docker.internal:8080 단일 스크레이프 타깃 (/actuator/prometheus)"
  - ".env.example — MEDIA_ROOT/비밀번호/오리진 템플릿 (실 .env 는 .gitignore 가 차단, check-ignore 실측)"
  - "발행 포트 전량 127.0.0.1 루프백 바인드 (app:8080 만 문서화된 예외)"
  - "compose↔test 핀 대조 커맨드 (수동 실측 완료, 자동화는 Plan 06 CI 로 인계)"
affects: [01-06-ci, 01-07-verification, 01-08-adr, 02-transcoding-hls, 06-observability, 08-ingest]

# Tech tracking
tech-stack:
  added: [nginx:stable-alpine, prom/prometheus:v3.13.2, grafana/grafana:13.1.2]
  patterns:
    - "단일 compose.yaml + profiles — override 파일 분리 금지(-f 인자 강요 = D-03 위반)"
    - "발행 포트 기본값은 루프백 바인드, 비-루프백은 근거를 주석에 남기고 위협 등록부에 accept 로 기록"
    - "컨테이너 설정 파일은 해당 도구의 공식 문법 검사기(nginx -t / promtool)를 도커로 돌려 게이트화"
    - "게이트 green 뒤 반사실 control arm 으로 판별력 실측 (01-02/01-04 확립 패턴의 3회차)"

key-files:
  created:
    - compose.yaml
    - .env.example
    - infra/nginx/default.conf
    - infra/prometheus/prometheus.yml
  modified:
    - README.md

key-decisions:
  - "web 서비스의 오리진을 런타임 environment 가 아니라 build.args 로 — 플랜 명세와 01-03 실측이 충돌할 때 실측 우선 (rewrites 목적지는 next build 시점에 routes-manifest 로 구워진다)"
  - "플랜 <action> 주석의 `alias`·`docker-entrypoint-initdb.d` 리터럴을 재작성 — 같은 태스크 acceptance 가 그 리터럴 0건을 요구하는 자기모순 (동형 결함 4·5회째)"
  - "nginx -t 의 최초 PASS 를 신뢰하지 않고 nginx -T 로 우리 conf 가 실제 마운트됐는지 확인 — MSYS 경로 변환 때문에 vacuous PASS 가 가능한 구조였다"
  - "compose↔SchemaMigrationTest 핀 대조를 수동 diff 로 실측(일치)하고, 자동 게이트는 CI 를 소유한 Plan 06 으로 인계 — 백엔드 테스트 파일은 이 플랜의 files_modified 밖"
  - "ARCH-05 mark-complete 미실행 — compose.yaml 은 생겼으나 요구사항 문구('한 번으로 전 스택 기동')의 실기동 검증이 Plan 07 소관 (선례 5회째)"

patterns-established:
  - "검증 커맨드의 실패는 산출물 결함과 구분해 기록한다 — MSYS 경로 변환은 파일이 아니라 실행 환경의 결함"
  - "마운트 기반 검사기는 '검사기가 우리 파일을 봤는가' 를 먼저 증명한 뒤 rc 를 읽는다"

requirements-completed: []
requirements-contributed: [ARCH-05]

# Metrics
duration: 11 min
completed: 2026-08-06
---

# Phase 1 Plan 05: compose 토폴로지 + nginx/prometheus 설정 Summary

**무프로필 4종 / `--profile full` 6종으로 갈라지는 단일 compose.yaml 착지 — mysql 기동 파라미터 3종이 SchemaMigrationTest 와 문자 그대로 일치함을 diff 로 실측했고, nginx/promtool 문법 게이트는 반사실 control arm(깨진 conf → rc=1)으로 판별력까지 증명했다**

## Performance

- **Duration:** 약 11분 (14:21+9 착수 ~ 14:32+9)
- **Tasks:** 2/2
- **Files created:** 4 · **modified:** 1
- **Commits:** 3 (task 2 + Rule 1 doc 정정 1)
- **Docker:** `prom/prometheus:v3.13.2` 최초 pull 포함

## Accomplishments

- **ARCH-05 의 토폴로지가 실재한다.** `docker compose config -q` / `docker compose --profile full config -q` 둘 다 rc=0, 무프로필 서비스가 정확히 `grafana,mysql,nginx,prometheus` 4종, full 에서 6종(`app`,`web` 추가). D-01(평시 인프라만 / 데모 전체)이 파일 구조로 강제된다.
- **G3 핀이 compose↔테스트 양쪽에서 같은 문자열임을 실측했다.** 종전엔 "같아야 한다"가 산문이었다. 이제 측정치가 있다 — `diff` 로 3/3 일치, 반사실(2→3 드리프트)에서 `rc=1` 로 갈라짐 확인. 다만 **자동 회귀는 아직 없다**(Plan 06 인계, 아래 잔여 결함).
- **문법 게이트가 vacuous 하지 않음을 증명했다.** `nginx -t` 는 컨테이너 기본 conf 만 봐도 PASS 하므로 rc 만으로는 무의미하다 — `nginx -T | grep -c 'sendfile off'` = 1 로 **우리 conf 가 실제로 로드됐음**을 먼저 확인하고, 고의로 깨뜨린 conf 가 rc=1 로 붉어지는 것까지 봤다. promtool 도 동일(정상 rc=0 / `targetz` 오타 rc=1).
- **LAN 노출 표면을 기본값으로 닫았다 (T-1-19).** 렌더된 config 실측에서 mysql·nginx·prometheus·grafana·web 전부 `host_ip: 127.0.0.1`. 비-루프백은 `app:8080` 하나뿐이고 그 이유(prometheus host-gateway 스크레이프)가 주석과 위협 등록부(T-1-20 accept)에 남았다.
- **핀한 이미지 태그 4종이 레지스트리에서 실제로 해결된다.** `docker manifest inspect` 4/4 OK (`mysql:8.0` · `nginx:stable-alpine` · `prom/prometheus:v3.13.2` · `grafana/grafana:13.1.2`) — 롤링 태그 0건.
- **Windows 바인드 경로가 깨지지 않고 파싱된다.** `${MEDIA_ROOT:-D:/moabom-media}:/srv/media:ro` 는 콜론 4개짜리 short syntax 라 드라이브 문자에서 쪼개질 위험이 있었다. 렌더 결과 실측: `source: D:/moabom-media` / `target: /srv/media` / `read_only: true`.

## Task Commits

1. **Task 1: infra/nginx/default.conf + infra/prometheus/prometheus.yml** — `8483669` (feat)
2. **Task 2: compose.yaml + .env.example** — `0b3ffdb` (feat)
3. *(Rule 1 정정)* **README 미래형 표기 + FE 오리진 사실 명시** — `d79568d` (docs)

## Files Created/Modified

- `compose.yaml` — profiles 이원화 토폴로지. mysql 기동 파라미터 3종, `pull_policy: never`, 루프백 바인드, `mysql-data` named volume
- `.env.example` — `MEDIA_ROOT` · `MYSQL_ROOT_PASSWORD` · `GRAFANA_PASSWORD` · dev 오리진 2종 (실 `.env` 는 `git check-ignore -q .env` rc=0 으로 차단 확인)
- `infra/nginx/default.conf` — `sendfile off`(CP-11) · `root /srv`(T-1-22) · m3u8 `max-age=10` / 세그먼트 `immutable`(Pitfall 4) · `/healthz` · 그 외 404
- `infra/prometheus/prometheus.yml` — `host.docker.internal:8080` 단일 타깃 + `metrics_path: /actuator/prometheus` + self-scrape
- `README.md` — `.env.example` 미래형 표기 정정 + FE 오리진이 빌드-시점 값이라는 실측 명시

## Decisions Made

1. **`web` 의 오리진 주입 경로를 플랜 명세(런타임 `environment:`)에서 `build.args` 로 바꿨다.** 01-03 이 반사실 2-arm 프로브로 "rewrites destination 은 `next build` 시점에 `.next/routes-manifest.json` 으로 구워진다"를 실측했다(RESEARCH A3 반증). 런타임 env 를 그대로 두면 **읽는 사람이 그 값을 바꾸면 목적지가 바뀐다고 믿게 되는** 거짓 배선이 된다. 값 자체는 Dockerfile 기본값과 동일하므로 동작은 같고, 바뀐 것은 "어디가 정본인가"의 정직성이다.
2. **compose↔테스트 핀 대조 게이트를 여기서 자동화하지 않았다.** 이 플랜의 산출물은 설정 파일 4종이고 테스트/CI 파일은 각각 Plan 04·06 소유다. 대신 (a) 지금 일치함을 diff 로 실측하고 (b) Plan 06 이 그대로 붙일 수 있는 커맨드를 아래 인계 항목에 적었다. 측정 없는 인계는 하지 않았다.
3. **ARCH-05 를 mark-complete 하지 않았다** (01-01·01-02·01-03·01-04 선례에 이어 5회째). compose.yaml 이 생겼지만 요구사항 문구는 "docker-compose 한 번으로 전 스택 **기동**"이고, 이 플랜이 가진 증거는 `config -q`(스키마 검증)까지다. 기동 검증은 Plan 07 Task 2 소관이며, 지금 체크하면 뜨지 않은 스택을 떴다고 선언하는 것이다.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] nginx 주석의 `alias` 리터럴이 같은 태스크의 acceptance 를 깼다**
- **Found during:** Task 1
- **Issue:** 플랜 `<action>` 의 nginx conf 주석에 "alias 가 아니라 root 를 쓴다" 가 있는데, 같은 태스크 acceptance 가 `grep -c 'alias' infra/nginx/default.conf` = `0` 을 요구한다. 그대로 쓰면 1건이 되어 자기 게이트에 걸린다.
- **Fix:** 의도(경로 접두 절단 방식의 순회 표면 회피)를 보존하며 리터럴만 제거 — "경로 접두를 치환하는 지시자가 아니라 root 를 쓴다".
- **Files modified:** `infra/nginx/default.conf`
- **Verification:** `grep -c 'alias'` = 0, `grep -c 'root /srv;'` = 1
- **Committed in:** `8483669`

**2. [Rule 1 - Bug] compose 주석의 `docker-entrypoint-initdb.d` 리터럴이 `initdb` 0건 게이트를 깼다**
- **Found during:** Task 2
- **Issue:** 플랜 `<action>` 의 compose 주석("docker-entrypoint-initdb.d 를 쓰지 않는다")과 acceptance(`grep -c 'initdb' compose.yaml` = `0`)가 정면 충돌.
- **Fix:** "MySQL 이미지의 부트스트랩 SQL 디렉터리를 쓰지 않는다 (Pitfall 14 — 볼륨에 데이터가 있으면 통째로 건너뛰어 '고쳤는데 안 바뀐다' 가 된다)" 로 재작성. Pitfall 14 의 규율은 그대로 남았다.
- **Files modified:** `compose.yaml`
- **Verification:** `grep -c 'initdb' compose.yaml` = 0
- **Committed in:** `0b3ffdb`

> 이 두 건은 **플래너 자기모순 패턴의 4·5회째**다 (01-01 Dev1 · 01-02 Dev4 · 01-04 Dev3 선례). 산문/주석의 리터럴이 자기 grep acceptance 를 깨는 형태로 반복된다. 2-Strike 를 이미 넘겼으므로 **플랜 생성 단계에 기계 게이트가 필요하다**: "acceptance 가 `grep -c X` = 0 을 요구하면, 같은 태스크의 `<action>` 코드블록에 X 리터럴이 없어야 한다"는 정적 검사로 코드화 가능하다 (Plan 06 이나 phase 회고 대상).

**3. [Rule 1 - Bug] `web` 서비스의 오리진이 무효 경로(런타임 env)로 배선돼 있었다**
- **Found during:** Task 2
- **Issue:** 플랜의 compose 는 `web.environment` 로 `BACKEND_ORIGIN`/`MEDIA_ORIGIN` 을 준다. 01-03 실측상 이 값은 이미지 빌드 시점에만 유효하고 런타임 주입은 무시된다 — 동작은 (Dockerfile 기본값 덕에) 맞지만 배선이 거짓말을 한다.
- **Fix:** `build: {context: ./frontend, args: {BACKEND_ORIGIN, MEDIA_ORIGIN}}` 으로 이동 + 근거 주석.
- **Files modified:** `compose.yaml`, `.env.example`(같은 취지의 주석 정정)
- **Verification:** 렌더된 config 실측 — `build.context: D:\moabom\frontend`, `args: BACKEND_ORIGIN: http://app:8080`, `MEDIA_ORIGIN: http://nginx:80`
- **Committed in:** `0b3ffdb`

**4. [Rule 1 - Bug] README 가 방금 생긴 파일을 "나중에 추가됩니다" 로 설명하고 있었다**
- **Found during:** Task 2 직후 정합성 확인
- **Issue:** `README.md:63` — "현재 시점 정직 표기: `.env.example`은 compose·앱 설정이 들어오는 단계에서 추가됩니다." 이 플랜이 `.env.example` 을 만든 순간 거짓 진술이 된다. 같은 문단이 `cp .env.example .env` 를 "건너뛰어도 된다" 고 안내하고 있었다.
- **Fix:** 현재 사실로 재작성 + FE 오리진이 런타임 변수가 아니라는 01-03 실측을 README 에도 명시.
- **Files modified:** `README.md`
- **Verification:** `grep -n 'bootBuildImage\|MEDIA_ROOT' README.md` — CP-3 A안 선행 빌드 1줄(36-37행)과 `.env` 절차가 서로 정합
- **Committed in:** `d79568d`

---

**Total deviations:** 4 auto-fixed (Rule 1 × 4 — 2건은 플래너 자기모순, 1건은 선행 플랜 실측 반영, 1건은 문서 currency)
**Impact on plan:** 잠긴 결정(D-00/D-01/D-03/D-06)과 must_haves 는 전부 그대로다. 바뀐 것은 주석 리터럴 2개, 오리진 주입 경로 1개, README 문장 1개다. 스코프 확대 없음.

## Issues Encountered

**1. MSYS 경로 변환이 acceptance 커맨드 2종을 조용히 무력화했다 (환경 결함, 산출물 결함 아님)**

Git Bash 에서 `/` 로 시작하는 인자는 Windows 경로로 변환된다. 실측:

```
$ python -c "import sys; print(sys.argv[1:])" '/actuator/prometheus'
['C:/Program Files/Git/actuator/prometheus']
```

결과로 (a) `grep -q '/actuator/prometheus' infra/prometheus/prometheus.yml` 이 **파일에 그 줄이 있는데도 0건**을 반환했고, (b) promtool 의 컨테이너 내부 인자가 `C:/Program Files/Git/etc/prometheus/prometheus.yml` 로 변환돼 `path does not exist` 로 실패했다. `MSYS_NO_PATHCONV=1` 을 주면 둘 다 통과한다(각각 1건 / rc=0).

**더 위험했던 것은 (b)가 아니라 (a) 유형의 조용한 실패다.** 같은 변환이 `docker run -v` 의 컨테이너-측 경로에도 적용될 수 있어, **nginx 가 우리 conf 를 안 보고도 `nginx -t` 가 rc=0 을 주는 vacuous PASS** 가 구조적으로 가능했다. 그래서 rc 를 믿지 않고 `nginx -T` 렌더 출력에서 `sendfile off` 1건을 먼저 확인했다.

**2. `nginx -t` 는 그 자체로는 판별력이 없다** — 컨테이너 기본 conf 만 있어도 항상 rc=0 이다. control arm(세미콜론 누락 conf)이 rc=1 로 붉어지는 것을 확인해서야 게이트가 성립했다.

## 실측 기록 (반사실 control arm)

| 게이트 | 정상 arm | 반사실 arm | 판별 |
|---|---|---|---|
| `nginx -t` | rc=0 (+ `nginx -T` 에 `sendfile off` 1건) | `listen 80` 세미콜론 누락 → rc=1 `[emerg] invalid parameter "sendfile"` | ✅ |
| `promtool check config` | rc=0 SUCCESS | `targetz` 오타 → rc=1 `field targetz not found` | ✅ |
| D-00 증설 서비스 grep | 0건 | `redis:` + `catalog-replica:` 주입 → 2건 | ✅ |
| `ngram_token_size=2` 렌더 카운트 | 1 | command 줄 삭제 → 0 | ✅ |
| `:latest` 태그 grep | 0건 | `nginx:latest` 주입 → 1건 | ✅ |
| compose↔test 핀 diff | rc=0 (3/3 일치) | compose 만 `=3` 으로 드리프트 → rc=1 | ✅ |

## Acceptance 실측 결과

**Task 1** (11/11 PASS): 파일 2종 존재 · `sendfile off` 1 · `root /srv;` 1 · `alias` 0 · `max-age=10` 1 · `immutable` 1 · `nginx -t` rc=0 · `host.docker.internal:8080` 1 · `/actuator/prometheus` 1(pathconv 해제 후) · `promtool` rc=0 · application.yaml 에 `prometheus` 노출 존재(41행).

**Task 2** (14/14 PASS): `config -q` rc=0 · `--profile full config -q` rc=0 · 무프로필 서비스 = `grafana,mysql,nginx,prometheus` · full 6종 · `ngram_token_size=2` 1 · `utf8mb4_0900_ai_ci` 1 · `pull_policy: never` 1 · `srv/media:ro` 1 · 증설 서비스 0 · `:latest` 0 · 이미지 핀 2종 존재 · `initdb` 0 · `.env.example` 존재 + `MEDIA_ROOT=D:/moabom-media` 1 · `git check-ignore -q .env` rc=0 · `0.0.0.0:3306` 0.

플랜의 `<verify><automated>` 2줄도 그대로 실행 — `INFRA_CONF_OK` / `COMPOSE_OK` 출력 확인.

## 알려진 결함 / 잔여 리스크 (정량)

**알려진 결함 N=1** (자동 회귀 부재 1건), **잔여 미실측 3건**:

1. **[결함 — Plan 06 로 인계]** compose↔SchemaMigrationTest 핀 대조에 **자동 게이트가 없다**. 지금은 일치(수동 diff 실측)지만 둘 중 하나만 고치면 아무도 못 잡는다. Plan 06 CI 에 아래를 그대로 넣을 수 있다(반사실로 판별력 확인 완료):
   ```bash
   diff <(grep -oE '\-\-(ngram_token_size|character-set-server|collation-server)=[^"]*' compose.yaml | sort) \
        <(grep -oE '\-\-(ngram_token_size|character-set-server|collation-server)=[^",]*' backend/src/test/java/com/moabom/SchemaMigrationTest.java | sort)
   ```
2. **[미실측 — A2]** `host.docker.internal:8080` 단일 타깃이 dev(호스트 JVM)/full(컨테이너) **양쪽에서** UP 인지는 실기동 전까지 모른다. 틀리면 한쪽 모드에서 Grafana 가 빈다. 대안은 scrape job 2개 분리(하나는 상시 DOWN 감수).
3. **[미실측 — 기동]** 이 플랜의 증거는 `config -q`(스키마 검증)까지다. 이미지 pull·볼륨 마운트·healthcheck·의존 순서는 Plan 07 이 닫는다. 특히 `${MEDIA_ROOT}` 바인드가 **Docker Desktop 의 파일 공유 설정**에 걸릴 수 있다(D: 드라이브 공유 미허용 시 기동 실패) — 파싱은 확인됐지만 마운트는 미확인.
4. **[미실측 — 캐시 헤더]** m3u8/세그먼트 캐시 정책은 실 파일이 없어 응답 헤더로 검증하지 못했다(P2 소관). 지금 확인된 것은 문법과 location 매칭 구조뿐이다.

## 인계 사항 (다음 플랜)

- **[Plan 06, HIGH]** 위 §1 의 compose↔test 대조 커맨드를 CI 게이트로 추가. `ubuntu-latest` 에서는 MSYS 경로 변환 이슈가 **없다**(Git Bash 전용 현상).
- **[Plan 07, HIGH]** 로컬 검증 스크립트를 Git Bash 로 돌린다면 `MSYS_NO_PATHCONV=1` 이 필요하다. 특히 `docker run -v ... :/컨테이너/경로` 와 `/` 로 시작하는 grep 패턴. **없으면 조용히 통과하는(vacuous PASS) 경로가 있다.**
- **[Plan 07, HIGH]** 8080 을 EnterpriseDB(PID 5140)가 점유 중이다(01-03 발견). `--profile full up` 은 `app` 의 `ports: 8080:8080` 에서 **바인드 실패**한다. 해제하거나, T-1-20 을 재검토해 포트를 옮기고 prometheus 타깃도 같이 바꿔야 한다.
- **[Plan 07/08, MEDIUM]** `bootBuildImage` 로 `moabom-app:local` 을 만들지 않으면 `pull_policy: never` 가 즉시 실패한다 — 이건 **의도된 동작**이다(CP-3 A안). Plan 07 검증 절차의 첫 줄이 선행 빌드여야 한다.
- **[Plan 08(ADR), MEDIUM]** ADR-0001 에 기록할 accept 3종이 이 플랜에서 확정됐다: T-1-20(app:8080 비-루프백), T-1-23(compose 기본 비밀번호 공개), T-1-24(mysql:8.0 EOL). 이미지 태그 4종은 `docker manifest inspect` 4/4 OK 로 실측됐다.

## User Setup Required

없음 — 이 플랜은 설정 파일만 만든다. 다만 클론한 사람은 `cp .env.example .env` 를 하면 되고, 하지 않아도 `${MEDIA_ROOT:-D:/moabom-media}` 기본값으로 동작한다(README 에 명시).

## Next Phase Readiness

- **Plan 06(CI) 준비 완료** — `docker compose --profile full config -q` 가 저비용 프록시로 실재하고, 핀 대조 커맨드까지 실측된 상태로 넘어간다.
- **Plan 07(검증) 준비 완료** — 검증 대상 4파일이 전부 있고, 위 §알려진 결함의 4건이 그대로 Plan 07 의 실측 목록이다.
- **차단 요소:** 없음. 다만 Plan 07 의 실기동은 8080 점유 해제와 Docker Desktop 의 D: 드라이브 공유 설정에 의존한다.

## Self-Check: PASSED

- 파일 6/6 존재 (created 4 + modified 1 + 이 SUMMARY)
- 커밋 3/3 존재 (`8483669` · `0b3ffdb` · `d79568d`)

---
*Phase: 01-foundation-guardrails*
*Completed: 2026-08-06*
