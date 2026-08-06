# ADR-0001: 런타임 버전 핀 — EOL 스택의 인지·수용과 CVE 가시성 정책

- **Status**: Accepted (2026-08-06)
- **Deciders**: 프로젝트 오너 (solo)
- **관련**: ADR-0002(미디어·인코딩·규율), `.github/dependabot.yml`, `.github/workflows/ci.yml`, `backend/build.gradle`, `compose.yaml`

---

## Context

모아봄은 **로컬/도커 전용 포트폴리오**다. 인터넷에 노출되지 않고, 실 사용자·실 시크릿·실 결제가 없으며,
저작권-프리 콘텐츠(Blender 오픈무비)만 다룬다. 스택 선택의 목적은 두 가지다:

1. **회사 스택 정합성** — Java 17 + MySQL 계열을 그대로 유지해 실무 전이 비용을 0으로 만든다.
2. **Spring Modulith 학습 목표** — 모듈 경계를 기계적으로 검증하는 경험이 이 포트폴리오의 핵심 논점이다.

이 두 목적이 잠근 조합(Spring Boot 3.2 ↔ Spring Modulith 1.1)은 **2026-08 현재 이미 지원이 끝난 라인**이다.
리뷰어가 저장소를 열고 던질 첫 질문이 "왜 EOL 스택인가"이므로, 그 답을 코드보다 먼저 문서로 둔다.

---

## Decision

### 1. 버전 핀 표 (정본 = `.planning/research/STACK.md`, 아래 값은 실제 파일과 대조된 것)

| 항목 | 핀 | 실제 위치 |
|---|---|---|
| JDK | **Temurin 17** (Boot 3.2 baseline) | `backend/build.gradle` toolchain |
| Spring Boot | **3.2.12** — 3.2 라인 **최종 패치** | `backend/build.gradle` plugins |
| Spring Modulith | **1.1.12** (공식 매트릭스 3.2.x ↔ 1.1.x) | `backend/build.gradle` `springModulithVersion` |
| Gradle | **8.14.5** (8.x 라인 최종) | `backend/gradle/wrapper/gradle-wrapper.properties` |
| MySQL | **8.0** (`mysql:8.0`) | `compose.yaml` |
| Flyway | 9.22.3 (BOM) + **`flyway-mysql`** | `backend/build.gradle` |
| resilience4j-spring-boot3 | **2.2.0** (BOM 미관리 → 명시 핀) | `backend/build.gradle` |
| 이미지 | `nginx:stable-alpine` · `prom/prometheus:v3.13.2` · `grafana/grafana:13.1.2` · `node:24-alpine` | `compose.yaml` · `frontend/Dockerfile` |
| Next.js / React | 16.2.12 / 19.2.8 | `frontend/package.json` |

**Gradle 9.x를 쓰지 않는 이유**: Boot 3.2 Gradle 플러그인 문서가 지원 범위를 *"7.x (7.5 or later) or 8.x"* 로
명시한다 — 9.x는 목록 밖이다. ⚠️ 정직 표기: **9.x에서 실제로 깨지는지는 본 프로젝트가 검증하지 않았다**
(문서상 미지원 ≠ 실측 실패). 채택 근거는 "검증되지 않은 조합에 CI를 걸지 않는다"이다.

### 2. EOL 인지·수용

| 런타임 | 지원 종료 | 의미 |
|---|---|---|
| Spring Boot 3.2 | OSS 지원 종료 **2024-12-31**, 마지막 패치 **3.2.12** | **새 CVE가 3.2 라인에 나오면 백포트가 영영 없다.** 우회로는 라인 이동뿐이다 |
| MySQL 8.0 | Extended Support **2026-04-30** 종료 | `mysql:8.0` 태그는 2026-05-05 갱신을 끝으로 사실상 동결됐다. pull은 계속 가능하고 윈도 함수·CTE·ngram 파서는 그대로 쓸 수 있다 |

**왜 그래도 수용하는가** (완화 3종):

1. **인터넷 미노출** — compose 발행 포트는 전부 `127.0.0.1` 루프백 바인드다(예외 1건은 §5에 명시).
2. **민감 데이터 0** — 실 사용자·실 시크릿·결제·개인정보가 없다.
3. **포트폴리오 수명** — 이 저장소는 데모 수명 내에서만 기동되며 상시 운영 자산이 아니다.

즉 CVE의 **영향(Impact)** 이 구조적으로 낮다. 우리가 관리하는 것은 확률이 아니라 노출면이다.

**승격 경로 (미리 못박는다)**:
- Boot을 3.3+로 올리면 **Modulith도 1.2+로 동반 이동**해야 한다 (공식 매트릭스 3.3.x ↔ 1.2.x).
  **단독 bump 금지** — 이것이 `.github/dependabot.yml`이 Boot/Modulith의 major·minor를 무시하는 이유다.
- MySQL은 8.4 LTS(2032-04까지)가 후속이다. 이동 시 `collation-server` 기본값 변경을 함께 확인해야 한다.
- 동반 이동은 **사람이 판단하는 한 번의 결정**이지 자동화의 대상이 아니다.

### 3. non-blocking CVE 스캔 — Dependabot 채택

**결정: `.github/dependabot.yml` (gradle / npm / github-actions 3 생태계).**

- Dependabot은 **CI 상태에 영향을 주지 않는다** → **태생적으로 non-blocking**이다. G1이 요구한 성질을
  설정이 아니라 **메커니즘 수준**에서 만족한다.
- Trivy / OWASP dependency-check 잡은 `continue-on-error: true`를 잊는 순간 blocking이 된다.
  "잊으면 위험한 방향으로 기우는" 설계를 EOL 라인 위에 얹지 않는다.

**blocking CVE 게이트를 두지 않는 이유**: 3.2 라인은 패치가 없으므로 blocking 게이트는 **상시 붉다.**
붉은 CI를 방치하기 시작하면 그 시점부터 CI 신호 전체가 죽는다 — 미디어 가드도, 모듈 경계도 같이 죽는다.
**목표는 가시성이지 차단이 아니다.** 차단은 우리가 고칠 수 있는 것(경계·핀·미디어)에만 건다.

⚠️ 알려진 마찰 (실측, 2026-08-06): `frontend/`에 `npm audit` high 3건(postcss XSS·경로 순회, sharp libvips)이
있고 유일한 수정 경로가 `next@16.3.0`이라 잠긴 핀 16.2.12와 충돌한다. Dependabot이 이 PR을 열 것이며,
**머지 여부는 사람이 판단한다**(자동 머지 설정 없음 — §4). 이 항목이 "가시성은 유지하되 차단하지 않는다"의
첫 실사례다.

### 4. resilience4j 2.2.0 핀 — 3중 방어 + 1

`2.3.0`은 Boot 3 + Java 17 조합에서 **기동 단계에서 죽는다**(resilience4j issue #2265, `RxJava3FallbackDecorator`
경로). 즉 "최신으로 올리자"가 곧 앱 정지다. 방어를 층으로 쌓는다:

| 층 | 수단 | 성격 |
|---|---|---|
| a | `backend/build.gradle`의 `resolutionStrategy.force` | **실제 강제** — 전이 의존이 끌어와도 2.2.0으로 되돌린다 |
| b | `.github/dependabot.yml`의 `io.github.resilience4j:*` ignore | **PR 자체를 안 연다** — 사람이 실수할 기회를 없앤다 |
| c | CI `dependencyInsight` 게이트 | **가시성** — a·b를 빠져나간 경우 빌드가 붉어진다 |
| d | auto-merge 미설정 | 자동 머지는 위 셋을 한 번에 무력화하는 경로다. 머지는 사람이 한다 |

이 의존성은 Phase 1에서 **사용처가 없다.** 그럼에도 클래스패스에 두는 이유는 `dependencyInsight` 게이트가
검사할 **피검사체가 필요하기 때문**이다 — 없으면 게이트는 아무것도 안 보면서 green을 보고한다.

### 5. SC1 "한 커맨드 기동"의 정직 재서술

성공 기준 1은 문자 그대로의 "한 커맨드"가 아니다:

```bash
cd backend && ./gradlew bootBuildImage --imageName=moabom-app:local   # 선행 1회 (빌드 스텝)
cd .. && docker compose --profile full up                             # 기동 한 커맨드
```

**compose는 Gradle을 실행할 수 없다.** `bootBuildImage`는 Docker 데몬에 직접 이미지를 만들고, compose의
`build:`는 Dockerfile만 안다. 따라서 이미지 생성은 compose 밖의 **빌드 스텝**이며, 이것은 "기동·리셋 UX
래퍼를 만들지 않는다"는 결정(D-03)의 위반이 아니다.

`compose.yaml`의 `app` 서비스에 `pull_policy: never`를 둔 이유: 이미지가 없을 때 compose가 레지스트리
pull을 시도하면 **인증 오류**로 나타나 "이미지를 안 만들었다"는 진짜 원인을 가린다. `pull_policy: never`는
"이미지 없음"으로 즉시·명확히 실패시킨다.

### 6. bootBuildImage 빌더 기록 (재현성 이월)

Boot 3.2.12의 `bootBuildImage` 기본 빌더는 `paketobuildpacks/builder-jammy-base:latest`다. `:latest`는
**재현성이 없다** — 같은 커밋이 다른 날 다른 이미지를 만든다. 해당 태그는 활발히 갱신 중이라(2026-08-04
실측) 죽은 태그는 아니지만, 첫 성공 빌드의 다이제스트를 남겨 필요 시 핀할 수 있게 한다.

```
- builder image: paketobuildpacks/builder-jammy-base:latest
- first successful build digest: sha256:814d7522dcf92cc9dee31a21a3950ef6ddca93bb49f161b32eb9602d4e9a0bd9   (빌드일 2026-08-06)
- run image:     paketobuildpacks/run-jammy-base@sha256:daa7fb1845d9b47e3f6ab5f38550340128f435fb2ae21d21aea663f8931b2aa5
- 산출 이미지:   moabom-app:local = sha256:c1bb9b569ab920441d1251315986d5837eb3a9df6ae26712b00f45256666106a
- 빌드 시간:     3분 8초 (빌더·run 이미지 pull 포함, 최초 1회)
```

✅ Plan 07 실측(2026-08-06)으로 채워졌다 — **가정 A5 해소**. 위 줄이 비어 있던 동안 근거가 없던
"빌더가 정상 동작한다"는 주장은 이제 실 빌드 1회로 뒷받침된다.

⚠️ 남는 정직 표기: **핀을 건 것이 아니라 기록한 것이다.** `build.gradle`은 여전히 `:latest`를 쓰므로
다음 빌드는 다른 다이제스트를 쓸 수 있다. 위 값은 "재현이 필요해지면 여기로 되돌린다"는 이월 기록이며,
재현성 자체는 아직 확보되지 않았다. 핀으로 승격하는 조건 = 빌드가 날짜에 따라 갈리는 사고가 1회라도 나면.

### 7. 위협모델 — accept 항목 (차단하지 않고 받아들이는 것)

| 항목 | 실체 | 왜 accept |
|---|---|---|
| MySQL root 기본 비밀번호 `moabom-local` | `compose.yaml`의 `${MYSQL_ROOT_PASSWORD:-moabom-local}` 기본값 | **로컬 전용 값**이며 실 시크릿이 아니다. 3306은 `127.0.0.1` 바인드라 LAN에 열리지 않는다. 실 시크릿은 `.env`(gitignore)로만 들어오고 저장소에 커밋되는 것은 `.env.example`뿐이다 |
| `app:8080`만 `0.0.0.0` 바인드 | 다른 발행 포트는 전부 루프백인데 이것만 예외 | 원 근거: Prometheus가 `host.docker.internal:8080`(host-gateway)로 스크레이프하려면 이 경로가 필요하다. **잔여 위험은 비-제로다** — 같은 LAN에서 8080에 접근 가능하다. ⚠️ **이 근거는 §9(A2 대안 적용)로 약화됐다** — full 의 메트릭은 이제 compose 네트워크(`app:8080`)로 들어오므로 발행이 메트릭의 전제가 아니다. 그럼에도 dev 워크플로가 호스트 8080 을 직접 쓰므로 바인드 변경은 보류하고 Phase 3 재검토로 이월 |
| GitHub Actions 메이저 태그 핀(`@v7` 등, SHA 핀 아님) | 태그가 이동하면 다른 코드가 실행된다 | 이 위협모델(공개 포트폴리오·시크릿 0·배포 없음·`GITHUB_TOKEN` read-only)에 SHA 핀은 과하다. Dependabot의 `github-actions` 감시가 월 1회 가시성을 준다. **시크릿이나 배포 잡이 생기는 phase에서 SHA 핀으로 승격한다** |
| Actuator 노출 | `health,info,prometheus` 화이트리스트 | `"*"` 노출은 `/env`·`/heapdump`·`/configprops` 유출 표면을 만든다. 화이트리스트는 `application.yaml`에 명시돼 있다 |

### 8. 아키텍처 진화 서사 (이 문서가 미래에 뒤집힐 지점)

Spring **Modulith** 모듈리스 단일 배포로 시작한다. 물리 마이크로서비스 분리·**Redis** 캐시/세션·DB 읽기
복제본은 "처음부터 넣는 것"이 아니라 **부하를 주입해 병목을 실측한 뒤** 증설한다(M1 후반 phase 9~).

**Phase 1은 이 증설을 선반영하지 않는다** — `compose.yaml`에 Redis도 복제본도 없다. 대신 Modulith
경계 검증(`ApplicationModules.verify()`)이 그 분리를 위한 준비물이다: 경계가 이미 강제돼 있으면 모듈
하나를 떼어내는 비용이 리팩토링이 아니라 배포 변경이 된다.

이 순서 자체가 Netflix가 실제로 밟은 경로의 재현이며, "처음부터 MSA"가 아니라는 것이 이 포트폴리오의 논점이다.

### 9. 실측으로 닫힌 가정 (Phase 1)

RESEARCH `Assumptions Log`가 `[ASSUMED]`로 남긴 항목 중 Phase 1 소관 4건을, Plan 07의 full 프로필
**실기동**으로 판정했다 (2026-08-06, Windows 11 / Docker 28.1.1 / Compose v2.35.1).

| # | 가정 | 판정 | 근거 (실측) |
|---|---|---|---|
| **A2** | `host.docker.internal:host-gateway` **단일** 스크레이프 타깃이 dev·full 양쪽에서 동작한다 | **반증 → 대안 적용** | full 에서 DOWN, `lastError: server returned HTTP status 404 Not Found`. 404 는 연결 거부가 아니라 **누군가 응답했다**는 뜻이라 응답자를 확인했다: 컨테이너→`host.docker.internal:8080/` 은 `Server: Apache`, 호스트→`localhost:8080/` 은 Spring. 대안(scrape job 2개 분리) 적용 후 `moabom-app-container` **UP** + `jvm_memory_used_bytes` 8 시리즈 실수집 확인 |
| **A3** | Next `rewrites()`가 서버 기동 시 평가되어 `next start` **런타임 env** 를 반영한다 | **반증 (01-03에서 이미) — 결과는 확인** | 메커니즘은 01-03 반사실 2-arm 프로브가 이미 반증했다(목적지는 `next build` 시점 `routes-manifest.json` 으로 구워진다). 그 반증에 따라 채택한 구조(Dockerfile `ARG` + compose `build.args`)가 **실동작함**을 이번에 확인: `curl :3000/api/health` → `{"ts":"...","status":"UP","app":"moabom"}`. 즉 A3 의 *주장*은 거짓이고, *그 자리를 대체한 D-06 구조*는 참이다 |
| **A5** | Paketo `builder-jammy-base:latest` 가 현 시점 정상 동작한다 | **확인** | `bootBuildImage` exit 0, 3분 8초. 다이제스트 §6 에 기록. 핀은 걸지 않았다(위 정직 표기) |
| **A6** | `mysqladmin ping --silent` 헬스체크가 인증 없이 성공한다 | **확인** | `docker compose ps` → `mysql Up (healthy)`. `up --wait` 가 `Container moabom-mysql-1 Healthy` 로 통과했고 `app` 의 `depends_on: service_healthy` 게이트가 실제로 열렸다 — 헬스체크가 장식이 아님이 기동 순서로 증명됨 |

**A2 판정의 정직한 한정 (중요)**: 이 머신의 8080 은 무관한 `httpd.exe`(PID 5140)가 점유 중이고,
Windows 소켓 의미론상 Docker 가 **같은 포트에 동시 LISTEN 하는 데 성공**했다. 따라서 관측된 DOWN 에는
환경 요인이 섞여 있다 — 깨끗한 머신에서는 단일 타깃도 동작했을 수 있다. 그럼에도 대안을 적용한 이유는
**충돌과 무관하게 분리가 옳기 때문**이다:

- full 에서 `app:8080`(compose 네트워크)은 호스트 포트 발행에 **전혀 의존하지 않는다** → 결정론적이다.
- dev 에서 앱은 호스트 JVM 이라 컨테이너 이름으로 닿을 수 없다 → host-gateway 가 유일한 경로다.
- 두 모드는 애초에 **다른 토폴로지**이고, 타깃 하나로 둘 다 덮으려던 것이 무리였다.

즉 포트 충돌은 원인이 아니라 **드러내 준 계기**다. 다만 "단일 타깃 전략이 깨끗한 환경에서 실패한다"는
주장은 본 프로젝트가 **증명하지 않았다**.

**A2 대안이 §7 의 accept 하나를 약화시킨다 (열린 항목)**: `app` 을 `0.0.0.0:8080` 으로 발행하는 유일한
근거가 "Prometheus 가 host-gateway 로 스크레이프해야 한다"였는데, 이제 full 의 메트릭은
compose 네트워크로 들어온다. 즉 **LAN 노출을 감수할 이유가 사라졌다.** 그럼에도 이 ADR 은 바인드를
바꾸지 않는다 — 발행 포트 변경은 dev 워크플로(호스트 8080 직접 접근)와 README 를 함께 건드리는
결정이라 사람이 판단할 몫이다. Phase 3(인증 표면 등장) 재검토 항목에 이 근거 약화를 명시해 이월한다.

**부수 발견 (Phase 2+ 이월, 이 ADR 의 결정 사항 아님)**: 호스트 8080 에 두 프로세스가 동시에 LISTEN 하면
**출처에 따라 응답자가 갈린다**(localhost→Spring / 컨테이너 gateway→Apache). Linux 라면 바인드 실패로
즉시 드러났을 조건이 Windows 에서는 조용히 통과한다. 로컬에서 "분명히 떴는데 왜 저쪽에선 404 인가"가
나오면 `netstat -ano | grep ':8080 '` 로 **LISTEN 이 2줄인지** 먼저 볼 것.

---

## Consequences

**좋아지는 것**
- 리뷰어의 첫 질문("왜 EOL인가")에 코드를 읽기 전에 답이 있다.
- 핀 이탈이 4개 층(force · ignore · CI 게이트 · 사람 머지)에서 막힌다.
- CVE 가시성이 유지되면서 CI 신호는 죽지 않는다.

**나빠지는 것 / 비용**
- Boot 3.2 라인의 신규 CVE는 **영원히 미패치**로 남는다. 이 문서는 그것을 고치지 않고 **기록**한다.
- Dependabot이 무시하지 않는 의존성(예: `next`)에서 핀 vs CVE 충돌 PR이 계속 열린다 — 사람의 판단 비용.
- 빌더 `:latest`는 재현성이 없다. Plan 07이 다이제스트를 **기록**했으나 **핀하지는 않았다**(§6).

**잔여 리스크 (비-제로, 정직 표기 — 2026-08-06 Plan 07 실측 후 갱신)**
- 알려진 결함 N=3:
  (a) `npm audit` high 3건 미해결 — 유일 수정 경로 `next@16.3.0`이 잠긴 핀 16.2.12와 충돌(§3).
  (b) 빌더가 `:latest`라 재현성 없음 — 다이제스트는 기록됐으나 핀은 아니다(§6). *A5 자체는 해소.*
  (c) `app`의 `0.0.0.0:8080` 발행이 근거를 잃은 채 남아 있다(§7·§9) — Phase 3 재검토로 이월.
- **환경 결함 1건(저장소 밖, 이 ADR이 고칠 수 없음)**: 개발 머신의 호스트 8080을 무관한
  `httpd.exe`(PID 5140)가 점유 중이며, 3001은 다른 프로젝트의 grafana 컨테이너가 점유 중이다.
  전자는 dev 모드(`bootRun`)와 host-gateway 스크레이프를 조용히 오염시키고, 후자는
  `docker compose --profile full up`을 **바인드 실패로 중단시킨다**(실측). 해소는 operator 소관이다.
- 자동 회귀 커버: 핀 이탈은 CI가 잡는다(G2) / EOL 자체·CVE 등장·호스트 포트 점유는
  **어떤 게이트도 잡지 않는다** — 사람이 본다.
