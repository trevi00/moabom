# ADR-0002: 미디어 격리 · 인코딩 규약 · 이벤트 테이블 소유권

- **Status**: Accepted (2026-08-06)
- **Deciders**: 프로젝트 오너 (solo)
- **관련**: ADR-0001(런타임 버전 핀), `.gitignore`, `.gitattributes`, `.editorconfig`, `.githooks/pre-commit`, `.github/workflows/ci.yml`, `compose.yaml`, `infra/nginx/default.conf`

> ADR-0001이 "왜 이 버전인가"를 답하는 **포트폴리오 문서**라면, 이 문서는 매일 지켜야 하는 **운영 규약**이다.
> 독자와 수명이 다르므로 분리했다.

---

## Context

이 프로젝트는 트랜스코딩 산출물(HLS 세그먼트 수백~수천 개)을 만들고, 한글 메타데이터를 다루며,
Windows에서 개발하고 ubuntu CI에서 검증한다. 이 세 가지가 각각 조용한 사고를 낳는다:

- 미디어 blob이 git 히스토리에 한 번 들어가면 **되돌리는 비용이 비대칭적으로 크다**(이력 세탁).
- 한글/공백 경로가 Windows 로케일에서 외부 프로세스로 넘어가면 **파일을 못 찾는다**.
- 인코딩이 어딘가 한 곳만 빠지면 **한쪽 환경에서만** 깨진다.

전부 "나중에 고치면 되는 것"이 아니라 "들어가기 전에 막아야 하는 것"이다.

---

## Decision

### 1. 미디어 격리 — 4중 방어와 각 층의 실제 강도

| 층 | 수단 | 실제 강도 |
|---|---|---|
| 1 | 미디어 루트가 **저장소 밖**: `D:/moabom-media/` | **물리적 격리.** git이 볼 수 없으면 사고가 성립하지 않는다. 이것이 가장 강한 층이다 |
| 2 | `.gitignore` 경로 스코프 패턴 | 우발적 `git add .` 차단. 패턴을 벗어난 확장자는 못 막는다 |
| 3 | `.githooks/pre-commit` (미디어 확장자 + 5MB 크기 게이트) | ⚠️ **`git commit --no-verify`로 우회된다. defense-in-depth이지 boundary가 아니다.** 게다가 `core.hooksPath` 설정을 안 한 클론에서는 아예 돌지 않는다 |
| 4 | CI의 `git rev-list --objects --all` 히스토리 전체 스캔 | **실 boundary.** push 시점에 발화하고 우회 경로가 없다. 워킹트리가 아니라 **전 이력**을 본다 |

**층 3과 층 4의 구분이 이 표의 핵심이다.** 로컬 훅은 값싸게 빨리 잡을 뿐이고, "미디어가 저장소에 들어가지
못한다"는 주장의 근거는 층 1과 층 4다.

⚠️ 층 4의 함정: CI 체크아웃에 `fetch-depth: 0`이 없으면 얕은 클론만 스캔해 **조용히 통과**한다.
게이트가 존재하는데 아무것도 보지 않는 상태가 되므로, 그 옵션은 CI acceptance에 명시 검사로 고정돼 있다.

**전역 `*.ts`를 `.gitignore`에 쓰지 않는 이유**: `.ts`는 MPEG-TS 세그먼트이자 TypeScript 확장자다.
`frontend/`가 같은 저장소에 살기 때문에 전역 배제는 Next.js 소스를 통째로 삼킨다 — `git status`가 조용해서
몇 시간 뒤에나 발견된다. 대체 수단은 **경로 스코프**(`media/**/*.ts`, `**/hls/**/*.ts`) + **5MB 크기 게이트**다.
CI가 `git check-ignore frontend/next.config.ts`가 **매칭되지 않음**을 회귀 게이트로 지킨다(라벨이 아니라 결과로 판정).

**5MB 임계값 근거**: GitHub의 하드 리젝은 100MB지만 **회복 비용이 비대칭**이라 훨씬 앞에서 끊는다.
HLS 세그먼트는 개별로는 5MB보다 작아도 수백 개가 한 번에 들어온다 — 그래서 크기 게이트만으로는 부족하고
확장자/경로 규칙이 그 구멍을 메운다. 두 규칙은 대체재가 아니라 보완재다.

### 2. Windows 파일명 ASCII-safe 정책

**규칙**: 파일시스템에 쓰이는 산출물 경로 — 트랜스코딩 결과, HLS 세그먼트/플레이리스트, 디렉터리명 — 은
**`[A-Za-z0-9._-]` 만** 사용한다. 원본 제목의 한글·공백·괄호·`#`·`&` 등은 **DB 메타데이터(`title.title_ko`)로만
보존**하고, 경로에는 그로부터 생성한 ASCII slug를 쓴다.

```
DB   : title_ko = "인터스텔라 (2014)"
경로 : D:/moabom-media/hls/interstellar-2014/master.m3u8
```

**근거**: Windows 로케일에서 ffmpeg / `ProcessBuilder`에 넘기는 인자가 cp949로 인코딩되면 프로세스가
파일을 못 찾는다. 이 사고는 **짧은 클립에서는 재현되지 않고 장편·다렌디션에서 확실히 걸린다** — 즉
"로컬에서는 됐는데"가 가장 잘 나오는 종류다. URL 경로에 들어가는 순간 인코딩 레이어가 하나 더 붙는 것도
같은 방향의 위험이다.

**소비자**: Phase 2(HLS 수직 슬라이스)와 Phase 8(인제스트 제품화)이 이 정책의 구속을 받는다.
slug 생성 규칙 자체(중복 처리·길이 상한)는 첫 소비자인 Phase 2가 확정한다.

### 3. UTF-8 명시 지점 — 목록과 실제 위치

한 곳만 빠져도 한쪽 환경에서만 깨지므로 **전 지점을 열거하고 각각이 실제 파일에 있는지**를 기준으로 삼는다.

| # | 지점 | 값 | 파일 |
|---|---|---|---|
| 1 | 에디터 | `charset = utf-8` | `.editorconfig` |
| 2 | 자바 컴파일 | `options.encoding = 'UTF-8'` | `backend/build.gradle` |
| 3 | 테스트 JVM | `systemProperty 'file.encoding'` / `'sun.jnu.encoding'` = UTF-8 | `backend/build.gradle` |
| 4 | Gradle 데몬 | `org.gradle.jvmargs=... -Dfile.encoding=UTF-8` | `backend/gradle.properties` |
| 5 | 앱 컨테이너 | `JAVA_TOOL_OPTIONS: "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"` | `compose.yaml` (app) |
| 6 | DB 서버 | `--character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci` | `compose.yaml` (mysql) |
| 7 | JDBC | `characterEncoding=UTF-8` | `compose.yaml` · `application.yaml` |
| 8 | 개행 정규화 | `* text=auto eol=lf` (+ `gradlew` lf / `*.bat` crlf) | `.gitattributes` |
| 9 | nginx | `charset utf-8;` | `infra/nginx/default.conf` |

`sun.jnu.encoding`이 별도로 필요한 이유: `file.encoding`은 파일 **내용**의 기본 인코딩이고,
`sun.jnu.encoding`은 **파일명·프로세스 인자**의 인코딩이다. 우리가 겪는 사고(§2)는 후자 쪽이다.
둘 다 명시하지 않으면 Windows에서 한쪽만 UTF-8이 된다.

⚠️ 개행 정규화(#8)가 없으면 Windows에서 커밋한 `gradlew`가 CRLF로 들어가 ubuntu 러너에서
`/usr/bin/env: 'sh\r': No such file or directory`로 죽는다. 실행 비트(`git update-index --chmod=+x`)도 같은 계열의 사고다.

### 4. `event_publication` 테이블 소유권 규율

이 절이 **D-14의 산출물 그 자체**다.

- `event_publication` 테이블은 **Flyway가 소유한다.** 스키마의 단일 진실은 하나여야 하고, 그것은 Flyway다.
- Modulith의 자동 스키마 생성을 쓰지 않는다 — `backend/src/main/resources/application.yaml`에
  `spring.modulith.events.jdbc.schema-initialization.enabled: false`를 **명시**했다(기본값이지만 규율을 코드로 남긴다).
- `spring-modulith-starter-jpa`는 **Phase 1에 넣지 않는다.** 이 starter-jpa 아티팩트는 `JpaEventPublication`
  `@Entity`를 클래스패스에 올리는데, `ddl-auto=validate`(Flyway 단일 진실의 표준 짝) 하에서 테이블이 없으면
  **기동이 죽는다**. 넣는 시점은 `@ApplicationModuleListener` 첫 사용 phase(P3 예상)이며,
  **해당 V 마이그레이션과 같은 커밋**에 들어간다. 둘을 분리하면 그 사이 커밋들이 전부 기동 불가다.
- ⚠️ 정직 표기 1: 위 "기동 실패" 시나리오는 **문서 근거이며 본 프로젝트가 실측하지 않았다**(RESEARCH 가정 A1).
  틀렸더라도 조치(P1은 `starter-core`만)는 D-14와 독립적으로 옳으므로 그대로 둔다.
- ⚠️ 정직 표기 2: Modulith 1.1.x의 이벤트 프로퍼티 **키 이름은 버전 간 변경 이력이 있다.**
  P3 착수 시 실측 재확인 대상이며, 지금 이 값이 무시되고 있어도 P1에서는 증상이 없다(소비자가 없으므로).

### 5. Phase 1 의존성 델타 — 의도적으로 제외한 것과 그 이유

"아직 안 넣었다"가 아니라 **"넣으면 지금 깨진다"** 는 기록이다.

| 제외 대상 | 넣으면 생기는 일 | 언제 |
|---|---|---|
| `spring-boot-starter-security` | 이 starter-security 아티팩트가 컨텍스트에 올라오는 순간 기본 `SecurityFilterChain`이 **전 경로에 HTTP Basic**을 건다 → `curl http://localhost:3000/api/health`가 **401**이 되고, 단일 진입 오리진(D-02)의 실동작 검증이 불가능해진다. 임시 `permitAll` 체인을 만들면 Phase 3에서 지워야 할 **죽은 설정**이 남는다 | **Phase 3** (AUTH-01/02) |
| `spring-session-jdbc` | 소비자가 없는데 `SPRING_SESSION` DDL을 P1 Flyway에 선점하게 만든다(D-11 위반) | **Phase 3** |
| `springdoc-openapi-starter-webmvc-ui` | 문서화할 API가 헬스 스텁 1개뿐 | **Phase 3~5** (첫 실 API) |
| `spring-modulith-starter-jpa` | §4 참조 | **Phase 3** |

**원칙**: 소비자가 없는 의존성은 넣지 않는다. 소비자가 생기는 phase가 그 의존성과 스키마를 **같은 커밋**에 가져온다.

### 6. nginx 미디어 서빙 운영 규약

- 미디어 마운트는 **`:ro`** — nginx는 읽기 전용이다. 쓰기 주체는 인제스트(P8)뿐이다.
- **`sendfile off;`** — Windows 바인드 마운트의 가상화 파일시스템에서 `sendfile`이 잘린/낡은 파일을 내보내는
  사례가 보고돼 있다. "가끔 세그먼트가 깨진다"로 나타나 트랜스코딩 버그로 오진하기 딱 좋다. 비용 0이므로 선제 차단.
- **캐시 수명 분리** — 플레이리스트(`.m3u8`)는 `max-age=10`, 세그먼트는 `immutable`.
  재트랜스코딩·렌디션 추가 시 낡은 마스터 플레이리스트가 삭제된 세그먼트를 요청하면 404 →
  hls.js fatal NETWORK_ERROR로 끝난다.
- ⚠️ 정직 표기: 위 캐시 정책의 **실검증은 Phase 2 소관이다.** 지금은 실 미디어 파일이 없어 헤더가
  실제로 붙는지 확인할 방법이 없다 — 현재 근거는 설정 파일의 존재뿐이다.

---

## Consequences

**좋아지는 것**
- 미디어 사고의 실 boundary가 어디인지 팀(=미래의 나)이 헷갈리지 않는다 — 훅은 편의, CI가 방어선.
- Windows/ubuntu 혼합 개발에서 인코딩 사고의 원인 후보가 9개 지점으로 열거돼 있다.
- 이벤트 테이블의 소유자가 하나(Flyway)로 확정돼 P3가 "누가 만드나"를 재논의하지 않는다.

**나빠지는 것 / 비용**
- ASCII slug 정책 때문에 경로만 봐서는 한글 제목을 알 수 없다 — DB를 봐야 한다.
- 의존성을 미루는 만큼 Phase 3의 커밋 하나가 커진다(스타터 + 마이그레이션 + 설정이 함께 들어온다).

**잔여 리스크 (비-제로, 정직 표기)**
- 알려진 결함 N=3: (a) starter-jpa 실패 모드 미실측(A1), (b) Modulith 이벤트 프로퍼티 키 이름 미확정,
  (c) nginx 캐시 헤더 실검증 미수행(P2 소관).
- 자동 회귀 커버: 미디어 히스토리·`.gitignore` 회귀는 CI가 잡는다 / **ASCII 파일명 정책과 UTF-8 지점 9종은
  어떤 게이트도 검사하지 않는다** — 사람이 리뷰에서 본다. Phase 2가 첫 실 산출물을 만들 때
  slug 검증을 코드 게이트로 승격하는 것이 자연스러운 다음 수순이다.
