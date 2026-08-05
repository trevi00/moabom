# Stack Research

**Domain:** VOD 스트리밍 서비스 (Netflix 참조 아키텍처 축소 이식 포트폴리오)
**Researched:** 2026-08-06
**Confidence:** MEDIUM-HIGH (코어 버전 핀 = HIGH, 미디어 파이프라인/FE 최신 패치 = MEDIUM)

> 규율: 잠긴 코어 스택(Java 17 / Boot 3.2 / Modulith / JPA / MySQL 8.0 / Next.js+React+hls.js /
> docker-compose / GitHub Actions / JUnit·JaCoCo·FixtureMonkey·Playwright)은 **재논의 대상이 아니다**.
> 이 문서는 그 위에서 (a) 서로 물리는 정확한 버전, (b) 아직 열려 있던 보조 라이브러리 결정을 확정한다.
> 각 추천에는 **"이게 실패하는 조건"** 을 1줄로 붙였다 (차용-조사 과낙관 2-Strike 경계).

---

## 0. 먼저 — 잠긴 코어의 지원 상태 (리스크 등록, 재논의 아님)

조사 중 확인된 사실이라 **은폐하지 않고 표기**한다. 결정을 되돌리자는 제안이 아니라, 로드맵이
알고 있어야 할 잔여 리스크다.

| 항목 | 실측 상태 | confidence | 이 프로젝트에서의 실제 영향 |
|---|---|---|---|
| Spring Boot 3.2 | OSS 지원 종료 **2024-12-31**, 마지막 패치 **3.2.12**. 2026-08 현재 3.2.x는 보안 패치 없음. 현행 GA는 4.1.x | HIGH | 로컬/도커 전용 포트폴리오이므로 **운영 리스크는 낮음**. 단 CVE 스캐너(Dependabot/Trivy)가 CI에서 계속 붉게 뜬다 |
| MySQL 8.0 | Extended Support **2026-04-30** 종료. 후속 LTS = 8.4 (2032-04까지) | HIGH | `mysql:8.0` 이미지는 계속 pull 가능. 윈도 함수·CTE·ngram 파서 모두 8.0에서 사용 가능 — 기능 손실 없음 |

**완화 3종 (저비용, 로드맵 Phase 0 권장):**
1. `3.2.0`이 아니라 **`3.2.12`로 핀** — 3.2 라인에서 받을 수 있는 마지막 보안 패치를 전부 받는다.
2. **ADR 1장** (`docs/adr/0001-runtime-versions.md`): "왜 EOL 라인인가 / 인터넷 미노출 / 승격 경로"를
   기록. 포트폴리오 리뷰어의 첫 질문("왜 EOL 스택?")에 대한 답이 코드베이스 안에 있게 한다.
3. CI에 **CVE 스캔은 켜되 non-blocking(advisory)** 으로 — 붉은 CI를 방치하면 신호가 죽는다.

**만약 나중에 사용자가 뒤집는다면 (최소 델타 매핑, 참고용):**

| Spring Boot | Spring Modulith | 비고 |
|---|---|---|
| 3.2.x | **1.1.x** | ← 현재 결정 |
| 3.3.x | 1.2.x | Modulith 1.2는 3.2/3.1 하위호환 유지 |
| 3.4.x | 1.3.x | |

---

## 1. Recommended Stack — Core (정확한 핀)

### Backend 런타임

| Technology | Version | Purpose | Why Recommended | conf |
|---|---|---|---|---|
| JDK | **Temurin 17 LTS** (최신 패치) | 런타임 | 잠긴 결정. Boot 3.2 baseline = 17 | HIGH |
| Spring Boot | **3.2.12** | 앱 프레임워크 | 3.2 라인 **최종 패치**. 이보다 낮게 핀할 이유 없음 | HIGH |
| Spring Framework | 6.1.15 | (BOM 관리) | Boot 3.2.12가 관리 — 직접 핀 금지 | HIGH |
| Spring Modulith | **1.1.12** | 모듈 경계 검증·모듈 이벤트 | Boot 3.2.x ↔ Modulith 1.1.x 공식 매트릭스. 1.1.12가 1.1 라인 최고 패치 | HIGH |
| Spring Data JPA | 3.2.12 / Hibernate **6.4.10.Final** | ORM | Boot BOM 관리값 | HIGH |
| MySQL Connector/J | **8.3.0** (`com.mysql:mysql-connector-j`) | JDBC | Boot BOM 관리값. 구 `mysql:mysql-connector-java` 좌표 사용 금지 | HIGH |
| Spring Security | 6.2.8 | 인증·인가 | Boot BOM 관리값 | HIGH |
| Spring Session | 3.2.6 | 세션 영속(JDBC) | Boot BOM 관리값 | HIGH |
| Micrometer | 1.12.13 (+ `micrometer-registry-prometheus`) | 메트릭 | Boot BOM 관리값. Actuator→Prometheus 배선 | HIGH |
| Flyway | 9.22.3 **+ `org.flywaydb:flyway-mysql`** | 스키마 마이그레이션 | Boot BOM 관리값. **MySQL 8은 별도 모듈 필요** (없으면 `Unsupported Database: MySQL 8.0`) | MEDIUM |
| Jackson | 2.15.4 | JSON | Boot BOM 관리값 | HIGH |

### 빌드/인프라

| Technology | Version | Purpose | Why | conf |
|---|---|---|---|---|
| Gradle | **8.14+ (8.x 라인)** | 빌드 | Boot 3.2 Gradle 플러그인 문서 원문: *"requires Gradle 7.x (7.5 or later) or **8.x**"* → **Gradle 9.x는 지원 목록 밖**(실측 미검증) | HIGH |
| Docker Compose | v2 (`docker compose`) | mysql·nginx·prometheus·grafana | 잠긴 결정 | HIGH |
| `spring-boot-docker-compose` | (BOM) `developmentOnly` | dev 시 compose 자동 기동 + ConnectionDetails 자동 주입 | Boot 3.1+ 기능. `spring.datasource.url` 수기 작성 제거 | MEDIUM |
| nginx | `nginx:stable-alpine` | HLS 세그먼트 정적 서빙 + 캐시 | Open Connect 축소 이식 | MEDIUM |
| ffmpeg | **8.0.3** (또는 7.1.5 LTS 성격) | 트랜스코딩·HLS 패키징 | 8.0.3 = 2026-06-18 릴리즈, 8.0 브랜치 최신 안정 | MEDIUM |

### Frontend

| Technology | Version | Purpose | Why | conf |
|---|---|---|---|---|
| Node.js | **24.x (Active LTS)** | FE 런타임/빌드 | 2026-08 현재 Active LTS(2028-04까지). Next 16은 Node **20.9+** 요구 | MEDIUM |
| Next.js | **16.2.x** (App Router) | FE 프레임워크 | Pages Router는 유지보수 모드 → App Router가 유일한 정답. Turbopack 기본 | MEDIUM |
| React / React DOM | **19.2.x** | UI | Next 16 App Router가 전제하는 버전 | MEDIUM |
| TypeScript | 5.x (최신) | 타입 | Next 16 최소 요구 **5.1+** | MEDIUM |
| hls.js | **1.6.16** | HLS 재생 | npm `latest` 실측(2026-08-06). 1.7은 alpha — 프로덕션 금지 | HIGH |

---

## 2. 열려 있던 결정 — 확정안 (근거 + 실패 조건)

### 2.1 복원력: `resilience4j-spring-boot3:2.2.0` — **2.3.0 쓰지 말 것**

- **결정**: `io.github.resilience4j:resilience4j-spring-boot3:2.2.0` (+ `spring-boot-starter-aop`, `spring-boot-starter-actuator`).
- **근거**: Resilience4j는 **Boot BOM이 관리하지 않는다** → 명시 핀 필수. 2.3.0은 Boot 3 + Java 17에서
  RxJava를 클래스패스에 두지 않으면 기동 자체가 실패한다
  (`ClassNotFoundException: io.github.resilience4j.spring6.fallback.RxJava3FallbackDecorator`,
  이슈 #2265). 2.2.0이 마지막 무사고 버전으로 보고됨.
- **이게 실패하는 조건**: 누군가 "최신으로 올리자"며 2.3.x로 bump → 앱이 기동 단계에서 죽는다.
  회귀 방지책: `build.gradle`에 버전 옆 주석 + 의존성 lock 또는 `dependencyInsight` 게이트.
- **사용처(포트폴리오 데모 포인트)**: `recommendation` 모듈 호출에 CircuitBreaker + `fallbackMethod`로
  **비개인화 lolomo 행** 반환. Actuator `/actuator/circuitbreakerevents`로 상태 전이를 Grafana에 노출.
  → "Hystrix→Resilience4j 축소 이식"이 **실측 가능한 데모**가 된다.

### 2.2 ffmpeg 호출: **자체 `ProcessBuilder` 래퍼** (라이브러리 도입 안 함)

- **결정**: `ingest` 모듈 안에 얇은 `FfmpegCommand` / `FfprobeCommand` 어댑터 (ProcessBuilder 기반).
  Jaffree·bramp 등 래퍼 라이브러리 **미도입**.
- **근거**:
  - `bramp/ffmpeg-cli-wrapper` (0.9.1) — 사실상 **유지보수 중단** 상태로 보고됨. EOL 스택 위에 EOL
    라이브러리를 또 얹지 않는다.
  - `Jaffree` — 가장 완성도 높은 Java 래퍼지만, 차별점(SeekableByteChannel 상호작용)이 **내부 FTP 서버**
    구동을 전제한다. 우리 워크로드는 `파일 → 파일` 배치라 그 기능이 필요 없고, 컨테이너 안에서
    FTP 포트가 뜨는 부작용만 남는다.
  - 우리가 필요한 건 "인자 조립 + stderr/stdout 파싱 + 종료코드"뿐이다. 이건 100줄짜리 어댑터다.
    그리고 **그 어댑터 자체가 포트폴리오의 보여줄 거리**(프로세스 격리·타임아웃·진행률)다.
- **진행률**: `-progress pipe:1 -nostats` 로 `key=value` 스트림을 stdout에서 파싱 → `IngestJob.progress`
  갱신. ⚠️ `out_time_ms` 필드는 이름과 달리 **마이크로초**로 보고된다는 커뮤니티 보고가 있다 —
  Phase 스파이크에서 실측 확인 후 확정할 것 (**confidence: LOW**, 공식 문서 미확인).
- **이게 실패하는 조건**:
  (a) ffmpeg 실행 파일 경로를 하드코딩 → Windows 개발 셸(`ffmpeg.exe`)과 Linux 컨테이너(`/usr/bin/ffmpeg`)가
      갈린다. **반드시 설정 프로퍼티 `moabom.ffmpeg.binary`로 외부화**.
  (b) `Process`의 stderr를 읽지 않으면 파이프 버퍼가 차서 **ffmpeg가 조용히 멈춘다** (고전적 데드락).
      → stdout/stderr 둘 다 별도 스레드로 배수(drain)하거나 `redirectErrorStream(true)`.
  (c) 타임아웃 없이 `waitFor()` → 손상 입력 하나가 워커 스레드를 영구 점유.

### 2.3 비동기 잡 큐: **도메인 `IngestJob` 상태머신 + Modulith 이벤트** (Spring Batch 아님)

- **결정**:
  1. `IngestJob` 엔티티가 상태머신 (`UPLOADED → PROBING → TRANSCODING → PACKAGING → PUBLISHED / FAILED`)
     — 진행률·시도횟수·실패사유를 컬럼으로 가진다.
  2. 트리거는 Modulith `@ApplicationModuleListener` (= `@Async` + `@Transactional(REQUIRES_NEW)` +
     `@TransactionalEventListener` 합성 어노테이션 — Context7 소스로 확인).
  3. 내구성은 Modulith **Event Publication Registry (JDBC)** — 이벤트가 `event_publication` 테이블에
     기록되고 완료 시 completion date가 찍힌다. 재기동 시 미완료 건 재발행 옵션 존재.
  4. 실행은 **bounded `ThreadPoolTaskExecutor`** (core=1~2, queue 제한, `CallerRunsPolicy` 금지 →
     `AbortPolicy` + 잡 테이블로 재시도).
- **근거 (왜 Spring Batch 5.1.2가 아닌가)**: Spring Batch는 **chunk-oriented ETL**(read-process-write)
  모델이다. 우리 작업 단위는 "외부 프로세스 1개를 10분간 돌리고 진행률을 받는 것"으로 chunk에 매핑되지
  않는다. JobRepository 스키마 9종·JobParameters 식별성 규칙 등 **개념 부채만 지고 얻는 게 없다**.
  반면 자체 상태머신은 그 자체가 관리자 UI에 그려지는 **포트폴리오 산출물**이다.
- **이게 실패하는 조건**: ffmpeg 실행 중 앱이 죽으면 잡이 `TRANSCODING`에 **영구 고착**된다.
  Spring Batch/JobRunr였다면 공짜로 얻었을 복구를 **직접 써야 한다** — 부팅 시
  `ApplicationRunner`로 "`TRANSCODING` + `updatedAt` 5분 초과" 건을 `FAILED`(재시도 가능)로 되돌리는
  **reconciler를 반드시 Phase 범위에 포함**시킬 것. 이걸 빼면 조용한 좀비 잡이 생긴다.
- ⚠️ Modulith 재발행 프로퍼티 이름은 버전에 따라 다르다
  (`spring.modulith.events.republish-outstanding-events-on-restart` vs 구 `spring.modulith.*`).
  **1.1.12 기준 실측 후 확정** (confidence: LOW-MEDIUM). 스키마는
  `spring.modulith.events.jdbc.schema-initialization.enabled=true`로 자동 생성하거나, Flyway로 관리하려면
  Modulith 레퍼런스 부록의 MySQL 스크립트를 복사한다(부록에 `schema-mysql.sql` 존재 확인).
- **대안 트립와이어**: 인제스트를 **멀티 인스턴스로 분산**하거나 재시도/백오프 대시보드가 필요해지는
  순간 → `org.jobrunr:jobrunr-spring-boot-3-starter` (MySQL 스토리지 지원, 대시보드·헬스·마이크로미터 내장).
  단 JobRunr 7.x는 Boot 3.3/3.4 대응 릴리즈가 이후에 나왔으므로 **Boot 3.2와의 조합은 별도 실측 필요**.

### 2.4 HLS 패키징: **ffmpeg HLS muxer 단독** (Shaka Packager / Bento4 미도입)

- **결정**: 인코딩과 패키징을 ffmpeg 한 방에.
  ```
  ffmpeg -i input.mp4 \
    -filter_complex "[0:v]split=3[v1][v2][v3];[v1]scale=w=1920:h=1080[v1out];[v2]scale=w=1280:h=720[v2out];[v3]scale=w=854:h=480[v3out]" \
    -map "[v1out]" -c:v:0 libx264 -b:v:0 5000k -map "[v2out]" -c:v:1 libx264 -b:v:1 2800k \
    -map "[v3out]" -c:v:2 libx264 -b:v:2 1400k \
    -map a:0 -map a:0 -map a:0 -c:a aac -b:a 128k -ac 2 \
    -f hls -hls_time 6 -hls_playlist_type vod -hls_segment_type fmp4 \
    -master_pl_name master.m3u8 \
    -var_stream_map "v:0,a:0 v:1,a:1 v:2,a:2" \
    "out/%v/stream.m3u8"
  ```
  핵심 옵션: `-var_stream_map`(변형 스트림 매핑) + `-master_pl_name`(마스터 플레이리스트) +
  `-hls_time`(세그먼트 길이) + `-hls_playlist_type vod`.
- **근거**: Shaka Packager / Bento4의 존재 이유는 **DRM(Widevine/PlayReady) 암호화와 CMAF 정밀 제어**다.
  우리 범위는 저작권-프리 콘텐츠 + DRM 명시적 out-of-scope → **툴체인 2개를 늘릴 근거가 0**이다.
  (Bento4가 세그먼팅은 더 빠르다는 보고가 있으나, 우리 규모에서 의미 없는 최적화.)
- **이게 실패하는 조건**: (a) `-hls_time`을 GOP 길이와 맞추지 않으면 세그먼트 경계가 키프레임에
  안 떨어져 ABR 전환 시 끊긴다 → `-g`(GOP) = fps × hls_time, `-sc_threshold 0`, `-keyint_min` 정렬 필수.
  (b) 렌디션마다 오디오를 중복 인코딩하면 저장이 3배 — MVP는 그대로 두고, 개선 여지로 남겨 문서화.
- **트립와이어**: DRM/암호화가 요구되면 → Shaka Packager 도입 (그때 ffmpeg는 인코딩만, 패키징은 위임).

### 2.5 검색: **MySQL 8.0 FULLTEXT + `WITH PARSER ngram`** (Elasticsearch 미도입)

- **결정**:
  ```sql
  ALTER TABLE title ADD FULLTEXT INDEX ft_title (title_ko, title_en, synopsis) WITH PARSER ngram;
  ```
  `ngram_token_size=2` (기본값)를 **docker-compose의 my.cnf / command에 명시** —
  이 변수는 **read-only, 기동 시에만 설정 가능**하다 (공식 문서 확인). 런타임 `SET`으로 못 바꾼다.
  문자셋은 `utf8mb4`.
- **근거**: MySQL의 ngram 파서는 **CJK(중·일·한) 전용으로 만들어진 것**이며 InnoDB에서 지원된다.
  우리 카탈로그는 저작권-프리 오픈무비 수십~수백 건 규모 — Elasticsearch/OpenSearch를 띄우면
  docker-compose에 JVM 컨테이너가 하나 더 붙고(메모리 1GB+), 인덱싱 동기화라는 **새 정합성 문제**가
  생긴다. 얻는 건 이 규모에서 체감 0.
- **알려진 한계 (공식 문서 실측)**:
  - **한국어 스톱워드 리스트가 없다** — 필요하면 직접 만들어야 한다.
  - 검색어의 **공백은 제거**된다 ("ab cd" → "ab","cd").
  - **자연어 모드 vs 불리언 모드 의미가 다르다**: 자연어 모드는 ngram의 **합집합**("ab bc"),
    불리언 모드는 ngram **구(phrase)** 검색(`"ab bc"`). → 정밀도를 원하면 **BOOLEAN MODE**를 쓴다.
  - **와일드카드 검색이 직관과 다르게 동작**한다 (인덱스에 단어 시작 정보가 없음).
  - `ngram_token_size`보다 짧은 검색어(1글자)는 **매칭 자체가 안 된다** → UI에서 최소 2자 안내 필요.
- **이게 실패하는 조건**: 사용자가 **초성 검색**("ㅅㅌ" → 신세계)이나 오타 허용을 기대하면 ngram은 답을
  못 준다. 그 요구가 생기면 → `title.search_normalized` 컬럼(자모 분해/초성 추출)을 애플리케이션에서
  채우고 그 컬럼에 ngram 인덱스를 추가하는 것이 다음 최소 단계. (Elasticsearch nori는 그 다음.)
- **참고**: MySQL의 MeCab 파서 플러그인은 **일본어 전용**이다 — 한국어 대안이 아니다.

### 2.6 인증/세션: **Spring Security 6.2.8 + Spring Session JDBC 3.2.6 + Next.js `rewrites` 동일 오리진**

- **결정**:
  - 서버: 폼/JSON 로그인 → **HttpOnly, SameSite=Lax, Secure(prod) 세션 쿠키**.
    세션 저장소 = `spring-session-jdbc` (MySQL) — 재기동해도 로그인 유지, docker-compose에 Redis 불필요.
  - 프로필 선택(가구 1계정 N프로필)은 **세션 속성 `activeProfileId`** 로 — JWT claim으로 굽지 않는다
    (프로필 전환마다 토큰 재발급이 필요해지므로).
  - FE-BE 결합: `next.config.ts`의 **`rewrites`로 `/api/:path*` → `http://backend:8080/api/:path*` 프록시**.
    브라우저 입장에서 **동일 오리진**이 되어 CORS·SameSite 문제가 구조적으로 사라진다.
- **근거**: 별도 오리진(`localhost:3000` ↔ `localhost:8080`)을 유지하면 `SameSite=Strict/Lax` 쿠키가
  전송되지 않아 결국 `SameSite=None; Secure`로 완화하게 되고, CSRF 표면이 넓어진다. 프록시 1줄이
  그 전체 문제군을 제거한다. 이 패턴이 2025-2026 Next.js BFF 문헌의 표준 처방이다.
- **CSRF**: `CookieCsrfTokenRepository.withHttpOnlyFalse()` + **모든 mutating 요청에 "현재" 토큰을 매번 읽어
  헤더로** 붙인다. (프로젝트 선례: 토큰을 한 번 캐시해두면 로그인·세션 갱신 후 stale 토큰으로 403이 난다.)
- **이게 실패하는 조건**: HLS 세그먼트를 nginx가 **다른 오리진**(예: `localhost:8081`)으로 서빙하면,
  hls.js는 XHR/fetch로 세그먼트를 받으므로 **CORS가 적용된다** — `<video src>` 네이티브 재생(Safari)은
  통과하는데 hls.js 경로만 깨지는 비대칭 버그가 난다. → nginx에 `Access-Control-Allow-Origin` +
  `Access-Control-Expose-Headers: Content-Length, Content-Range` 명시하거나, 미디어도 rewrites로 프록시.
  ⚠️ 단 **프록시 경유는 Range 요청/대용량 스트리밍 성능을 죽일 수 있다** — 서버 200 OK만 보고 "작동"으로
  결론내지 말고 **브라우저 E2E(Playwright)로 실제 seek·비트레이트 전환을 실측**할 것(프로젝트 선례).
- **NOT**: JWT를 `localStorage`에 저장하는 구성. XSS 1회 = 계정 탈취이고, 로그아웃/무효화가 불가능하다.

### 2.7 FE 재생: hls.js 1.6.16 — **client-only + 네이티브 폴백**

- **결정 패턴** (App Router):
  ```tsx
  'use client';
  // hls.js는 window/MediaSource를 참조 → 서버 렌더 시 크래시.
  const Hls = (await import('hls.js')).default;      // 동적 import
  if (Hls.isSupported())      { hls.loadSource(src); hls.attachMedia(video); }
  else if (video.canPlayType('application/vnd.apple.mpegurl')) { video.src = src; }  // Safari/iOS 네이티브
  ```
- **근거**: Safari/iOS는 **MSE 없이 네이티브 HLS**를 재생한다 → `Hls.isSupported()`가 false. 폴백이 없으면
  iPhone에서 재생이 통째로 죽는다. 이건 hls.js 공식 README가 명시하는 표준 분기다.
- **번들**: full 빌드 ≈ **70KB gzip**, `hls.light.*` 빌드(자막·대체 오디오·EME 제외) ≈ **50KB gzip**.
  → **기본값은 full 빌드**로 간다. 차이가 ~20KB gzip뿐인데, light 빌드는 자막(접근성)이나 다국어 오디오가
  필요해지는 순간 **기능 절벽**이 된다. "M1 범위 밖"은 지금 20KB를 아낄 근거가 되지 못한다 —
  light 전환은 **번들 예산 초과를 실측한 뒤**의 최적화로 미룬다 (전환 비용 = import 경로 1줄로 되돌릴 수 있음).
  ⚠️ 일부 2차 자료는 "254~300KB gzip"이라 주장하는데 **README 기반 수치(70/50KB)와 모순**한다 —
  스캐폴딩 시 실제 번들 분석으로 확정할 것 (confidence: MEDIUM).
- **이게 실패하는 조건**: `next/dynamic`이나 `useEffect` 밖에서 hls.js를 import 하면
  RSC/SSR 단계에서 `window is not defined`로 빌드가 깨진다. 반드시 `'use client'` + effect 내부 동적 import.
- **NOT**: `react-player` / `react-hls-player` 래퍼. 우리는 **버퍼 상태·레벨 전환·ERROR 이벤트를 직접
  다뤄야** ABR 데모와 이어보기(position 저장)를 만들 수 있는데, 래퍼는 그 이벤트를 가린다.
  (게다가 Netflix 스타일 커스텀 컨트롤을 어차피 직접 그려야 한다.)

### 2.8 API 문서: `springdoc-openapi-starter-webmvc-ui:2.3.0`

- **결정**: **2.3.0** 고정. 2.3.0이 Spring Boot **3.2.0** 대응으로 릴리즈된 버전(2023-12-03,
  swagger-core 2.2.19 / swagger-ui 5.10.3).
- **이게 실패하는 조건**: `3.x`(Boot 4 전용)나 최신 `2.8.x`(Boot 3.5 타겟)로 올리면 Boot 3.2와의 조합이
  **테스트되지 않은 영역**이다. 특히 3.x는 공식적으로 Boot 4 전용이라 **명백한 비호환**.
- confidence: MEDIUM (2.3.0↔3.2 정렬은 릴리즈 노트로 확인, 2.8.x가 3.2에서 실제로 깨지는지는 미검증).

### 2.9 테스트/품질

| Library | Version | 용도 | 실패 조건 | conf |
|---|---|---|---|---|
| JUnit Jupiter | 5.10.5 (BOM) | 단위/슬라이스 | — | HIGH |
| Mockito | 5.7.0 (BOM) | 목 | Java 17 inline mock-maker 기본 | HIGH |
| **FixtureMonkey** | **1.1.15** (`fixture-monkey-starter`, testImplementation) | 랜덤 픽스처 | 1.0 이전(0.6.x) API와 **호환 안 됨** — 블로그 예제 대부분이 0.x 문법이라 복붙하면 컴파일 실패 | MEDIUM |
| Testcontainers | 1.19.8 (BOM) + `mysql` 모듈 | 통합 테스트 | `@ServiceConnection`으로 URL 자동 주입. **CI(GitHub Actions)에서 Docker 필요** — ubuntu-latest는 OK, Windows 러너는 불가 | HIGH |
| Awaitility | 4.2.2 (BOM) | 비동기 잡 검증 | 트랜스코딩 완료 폴링에 필수 (sleep 금지) | HIGH |
| **JaCoCo** | **0.8.12** | 커버리지 | 0.8.13 존재 여부는 **자료가 엇갈림** — 빌드 시 실측 확인. Java 17 클래스는 0.8.11+에서 안정 | MEDIUM |
| ArchUnit | (Modulith가 전이 제공) | 모듈 경계 | `spring-modulith-starter-test`의 `ApplicationModules.verify()`로 대체 — ArchUnit 직접 핀 불필요 | MEDIUM |
| **Playwright** | **1.62.x** (`@playwright/test`) | 브라우저 E2E | 재생 E2E는 **실제 미디어 파일**이 필요 → CI에 소형 샘플(10초 클립) 커밋. 전체 오픈무비를 CI에 올리면 러너가 죽는다 | MEDIUM |

---

## 3. Installation

### `build.gradle` (Gradle 8.x, Groovy DSL)

```groovy
plugins {
  id 'java'
  id 'org.springframework.boot' version '3.2.12'
  id 'io.spring.dependency-management' version '1.1.7'
  id 'jacoco'
}

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

ext { set('springModulithVersion', '1.1.12') }

dependencies {
  implementation 'org.springframework.boot:spring-boot-starter-web'
  implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
  implementation 'org.springframework.boot:spring-boot-starter-security'
  implementation 'org.springframework.boot:spring-boot-starter-validation'
  implementation 'org.springframework.boot:spring-boot-starter-actuator'
  implementation 'org.springframework.boot:spring-boot-starter-aop'

  // 모듈리스
  implementation 'org.springframework.modulith:spring-modulith-starter-core'
  implementation 'org.springframework.modulith:spring-modulith-starter-jpa'   // event publication registry
  runtimeOnly    'org.springframework.modulith:spring-modulith-actuator'
  runtimeOnly    'org.springframework.modulith:spring-modulith-observability'

  // 세션 (JDBC)
  implementation 'org.springframework.session:spring-session-jdbc'

  // 복원력 — BOM 미관리, 명시 핀. 2.3.x 금지 (기동 실패 이슈 #2265)
  implementation 'io.github.resilience4j:resilience4j-spring-boot3:2.2.0'

  // 문서 — Boot 3.2 정렬 버전
  implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'

  // 관측
  runtimeOnly 'io.micrometer:micrometer-registry-prometheus'

  // DB
  runtimeOnly 'com.mysql:mysql-connector-j'
  implementation 'org.flywaydb:flyway-core'
  implementation 'org.flywaydb:flyway-mysql'        // MySQL 8은 이 모듈 없으면 실패

  compileOnly 'org.projectlombok:lombok'
  annotationProcessor 'org.projectlombok:lombok'

  developmentOnly 'org.springframework.boot:spring-boot-docker-compose'

  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  testImplementation 'org.springframework.security:spring-security-test'
  testImplementation 'org.springframework.modulith:spring-modulith-starter-test'
  testImplementation 'org.springframework.boot:spring-boot-testcontainers'
  testImplementation 'org.testcontainers:mysql'
  testImplementation 'com.navercorp.fixturemonkey:fixture-monkey-starter:1.1.15'
}

dependencyManagement {
  imports { mavenBom "org.springframework.modulith:spring-modulith-bom:${springModulithVersion}" }
}

jacoco { toolVersion = '0.8.12' }
```

### Frontend

```bash
npx create-next-app@16 moabom-web --typescript --app --eslint
cd moabom-web
npm i hls.js@1.6.16
npm i -D @playwright/test@latest && npx playwright install --with-deps chromium
```

### docker-compose 발췌 (검색 파서 설정이 핵심)

```yaml
services:
  mysql:
    image: mysql:8.0
    command: >
      --ngram_token_size=2
      --character-set-server=utf8mb4
      --collation-server=utf8mb4_0900_ai_ci
    # ngram_token_size 는 read-only 변수 — 기동 인자/my.cnf 로만 설정 가능
```

---

## 4. Alternatives Considered

| Recommended | Alternative | 대안이 나은 조건 |
|---|---|---|
| 자체 ProcessBuilder ffmpeg 어댑터 | **Jaffree** | 스트림(SeekableByteChannel) 입출력이 필요하거나, 진행률 파싱을 직접 짜기 싫을 때. FFmpeg 4~7 검증됨(8.x는 미검증) |
| `IngestJob` 상태머신 + Modulith 이벤트 | **JobRunr 7.x** (`jobrunr-spring-boot-3-starter`) | 인제스트 워커를 **2대 이상**으로 늘리거나, 재시도/백오프 대시보드가 필요해질 때. MySQL 스토리지 지원 |
| 〃 | **Spring Batch 5.1.2** | 카탈로그 **대량 CSV/메타데이터 임포트**처럼 진짜 chunk-oriented 작업이 생길 때 (트랜스코딩에는 부적합) |
| ffmpeg HLS muxer | **Shaka Packager / Bento4** | DRM 암호화(Widevine/PlayReady)나 정밀 CMAF 제어가 필요할 때 |
| MySQL ngram FULLTEXT | **OpenSearch + nori 분석기** | 카탈로그가 10⁵건을 넘거나, 오타 허용·동의어·초성 검색이 요구사항이 될 때 |
| 로컬 파일시스템 + nginx | **MinIO (S3 호환)** | "오브젝트 스토리지 오리진" 자체를 포트폴리오로 보여주고 싶을 때. M1엔 compose 서비스 1개 추가 비용 |
| Spring Session JDBC | **Spring Session Redis** | 세션 쓰기가 병목이 되거나 이미 Redis를 캐시로 띄운 경우 (M1은 Redis 미도입 → JDBC가 맞다) |

---

## 5. What NOT to Use

| Avoid | 구체적 문제 | Use Instead |
|---|---|---|
| **Gradle 9.x** | Boot 3.2 플러그인 문서가 지원 버전을 "7.x(7.5+) 또는 **8.x**"로 명시 → 9.x는 **지원 목록에 없다**. ⚠️ 정직 표기: 9.x에서 *실제로 깨지는지는 본 조사 미검증* (문서상 미지원 ≠ 실측 실패). 채택 근거는 "검증되지 않은 조합에 CI를 걸지 않는다" | Gradle 8.14+ (8.x 라인) |
| **resilience4j 2.3.0** | Boot 3 + RxJava 미포함 시 `ClassNotFoundException`으로 **기동 실패** | 2.2.0 핀 |
| **springdoc 3.x** | Spring Boot **4 전용** | springdoc 2.3.0 |
| `mysql:mysql-connector-java` (구 좌표) | Boot 3에서 deprecated 좌표. BOM이 관리 안 함 | `com.mysql:mysql-connector-j` |
| **`flyway-core` 단독** | MySQL 8에서 `Unsupported Database: MySQL 8.0` | `flyway-core` + `flyway-mysql` |
| **bramp/ffmpeg-cli-wrapper** | 유지보수 중단 보고. 마지막 0.9.1 | 자체 ProcessBuilder 어댑터 |
| **Next.js Pages Router** | 유지보수 모드 진입 — 신규 프로젝트에 채택할 이유 없음 | App Router (Next 16) |
| **hls.js 1.7 alpha** | 프로덕션 미준비 명시 | 1.6.16 |
| **`react-player` / `react-hls-player`** | ABR 레벨 전환·버퍼·ERROR 이벤트를 감춰서 "이어보기·복원력 데모"를 못 만든다 | hls.js 직접 + 자체 `useHls` 훅 |
| **JWT in `localStorage`** | XSS 1회 = 계정 탈취, 서버측 무효화 불가, 프로필 전환마다 재발급 | HttpOnly 세션 쿠키 + Spring Session JDBC |
| **Elasticsearch (M1)** | 수백 건 카탈로그에 컨테이너 1GB + 인덱스 동기화 정합성 문제 | MySQL FULLTEXT `WITH PARSER ngram` |
| **`SET GLOBAL ngram_token_size`** | read-only 변수 — 런타임 변경 불가 | compose `command:` / `my.cnf` |
| **JPQL로 Top10 랭킹** | JPA/JPQL은 **윈도 함수(`ROW_NUMBER`) 미지원** | 네이티브 쿼리 또는 `title_daily_rank` 집계 테이블 + 스케줄러 |

---

## 6. Stack Patterns by Variant

**카탈로그가 수백 건 이하 (= M1 실제):**
- MySQL ngram FULLTEXT + BOOLEAN MODE, 별도 검색 인프라 없음.
- lolomo 행 조립은 요청 시 계산 + Caffeine/Spring Cache 인메모리 캐시로 충분.

**Top10 / 랭킹 행이 필요할 때:**
- MySQL 8.0 윈도 함수(`ROW_NUMBER() OVER (PARTITION BY genre ORDER BY view_count DESC)`)를
  **네이티브 쿼리**로. JPQL은 지원하지 않는다 — 이걸 모르고 설계하면 Phase 중반에 리팩터링이 터진다.

**인제스트 워커를 분리 배포하고 싶어질 때 (트립와이어):**
- Modulith `@ApplicationModuleListener` → 이벤트 **externalization**(아웃박스)으로 전환하고
  `ingest`만 별도 프로세스로. Modulith는 이 전환을 위한 API(`EventExternalizationConfiguration`)를
  이미 제공한다 — 즉 **지금 구조가 물리 분리의 준비 상태**다. 이게 "모듈리스로 시작" 결정의 회수 지점.

**Safari/iOS 지원이 필요할 때 (= 기본):**
- `Hls.isSupported()` false 분기 → `video.src = m3u8` 네이티브 경로. Playwright에 **webkit 프로젝트 추가**.

---

## 7. Version Compatibility

| Package A | Compatible With | Notes |
|---|---|---|
| Spring Boot 3.2.12 | Spring Modulith **1.1.12** | 공식 매트릭스: 3.2.x↔1.1.x / 3.3.x↔1.2.x / 3.4.x↔1.3.x |
| Spring Boot 3.2.x | **Gradle 7.5+ 또는 8.x** | 플러그인 문서가 명시한 지원 범위. 9.x는 미지원 표기이며 실패 여부는 미검증 |
| Spring Boot 3.2.12 | Hibernate 6.4.10.Final / mysql-connector-j 8.3.0 / Flyway 9.22.3 / Testcontainers 1.19.8 / JUnit 5.10.5 / Micrometer 1.12.13 / Spring Security 6.2.8 / Spring Session 3.2.6 / Jackson 2.15.4 | 전부 BOM 관리 — **직접 핀 금지** |
| Spring Boot 3.2.x | resilience4j-spring-boot3 **2.2.0** | BOM 미관리 → 명시 핀. 2.3.0 회귀 |
| Spring Boot 3.2.x | springdoc-openapi **2.3.0** | 2.3.0이 Boot 3.2.0 대응 릴리즈 |
| Flyway 9.22.3 | MySQL 8.0 | **`flyway-mysql` 모듈 필수** |
| Next.js 16.2.x | Node **20.9+** (권장 24 LTS), TypeScript **5.1+**, React 19.2 | Node 18 지원 종료 |
| hls.js 1.6.16 | 브라우저 MSE / Safari는 네이티브 폴백 | peer/engine 제약 없음 (npm 메타 실측) |
| MySQL 8.0 | ngram 파서 (InnoDB) | `ngram_token_size` 기본 2, 범위 1–10, **read-only** |

---

## 8. 실패 조건 요약 (한 줄씩)

| 추천 | 이게 실패하는 조건 |
|---|---|
| Boot 3.2.12 핀 | 신규 CVE가 3.2 라인에 나오면 **패치가 영영 없다** (백포트 없음) |
| Modulith 1.1.12 | Boot를 3.3+로 올리는 순간 Modulith도 같이 올려야 함 (동반 이동) |
| resilience4j 2.2.0 | 무심코 최신으로 bump → 기동 실패 |
| 자체 ffmpeg 어댑터 | 바이너리 경로 하드코딩 / stderr 미배수 데드락 / 타임아웃 부재 |
| IngestJob 상태머신 | 크래시 시 좀비 잡 — **reconciler를 직접 안 쓰면 조용히 고착** |
| ffmpeg HLS muxer | GOP와 `hls_time` 미정렬 시 ABR 전환에서 끊김 |
| MySQL ngram | 1글자 검색·초성 검색·오타 허용 요구가 나오면 즉시 한계 |
| 세션 쿠키 + rewrites 프록시 | 미디어를 다른 오리진에서 서빙하면 hls.js만 CORS로 깨짐 (네이티브는 통과 → 비대칭 버그) |
| hls.js client-only | SSR 단계 import 시 `window is not defined` 빌드 실패 |
| Testcontainers | Docker 없는 CI 러너에서 전부 실패 |
| Playwright 재생 E2E | 실제 미디어 없으면 무의미 — 소형 샘플 클립을 리포에 커밋해야 함 |

---

## 9. Confidence & 미검증 항목 (정직 표기)

**HIGH** — Boot 3.2.12 BOM 관리 버전 전체, Modulith↔Boot 매트릭스 및 1.1.12 최고 패치,
Gradle 8.x 제약, MySQL ngram 파서 동작·제약, hls.js 1.6.16.

**MEDIUM** — Next.js 16.2.x / Node 24 LTS / Playwright 1.62.x / FixtureMonkey 1.1.15 / JaCoCo 0.8.12 /
ffmpeg 8.0.3 (2차 자료 기반, 릴리즈 페이지 원문 미대조), springdoc 2.3.0↔3.2 정렬,
flyway-mysql 필요성, resilience4j 2.2.0 안전성(이슈 리포트 1건 기반).

**LOW — 스캐폴딩 시 실측 필수:**
1. `-progress pipe:1`의 `out_time_ms` 단위(ms vs μs) — 공식 문서 미확인.
2. Modulith 1.1.12의 재발행 프로퍼티 **정확한 키 이름** (버전 간 변경 이력 있음).
3. hls.js 번들 크기 (70/50KB vs 254KB — 출처 간 모순).
4. JaCoCo 0.8.13 존재 여부 (자료 상충).
5. Docker 이미지 태그(prometheus/grafana/nginx)의 현행 안정 버전 — **본 조사 미검증**, 스캐폴딩 시 핀.
6. JobRunr 7.x ↔ Boot **3.2** 조합 (7.2는 Boot 3.3, 7.4는 3.4 대응 — 3.2 하위 호환은 미확인).
7. Gradle 9.x + Boot 3.2 플러그인이 **실제로 실패하는지** — 문서상 미지원만 확인, 반사실 실행 미수행.

---

## Sources

**Context7**
- `/spring-projects/spring-modulith` — `@ApplicationModuleListener` 합성 정의(@Async + REQUIRES_NEW +
  @TransactionalEventListener), Event Publication Registry MySQL 스키마, `EventExternalizationConfiguration`. (HIGH)

**공식 문서 (WebFetch 실측)**
- https://docs.spring.io/spring-boot/docs/3.2.12/reference/html/dependency-versions.html — Boot 3.2.12 관리 버전 전체 (HIGH)
- https://docs.spring.io/spring-boot/docs/3.2.12/gradle-plugin/reference/htmlsingle/ — "requires Gradle 7.x (7.5 or later) or 8.x" (HIGH)
- https://dev.mysql.com/doc/refman/8.0/en/fulltext-search-ngram.html — ngram_token_size read-only/기본2, WITH PARSER 문법, CJK 제약 (HIGH)
- https://search.maven.org/solrsearch/select?q=g:org.springframework.modulith+AND+a:spring-modulith-bom — 1.1.12가 1.1 라인 최고 패치 (HIGH)
- https://registry.npmjs.org/hls.js/latest — 1.6.16 (HIGH)
- https://github.com/spring-projects/spring-modulith/issues/971 — Boot↔Modulith 호환 매트릭스 (HIGH)
- https://github.com/resilience4j/resilience4j/issues/2265 — 2.3.0 기동 실패(RxJava3FallbackDecorator), 2.2.0 안전 (MEDIUM)
- https://springdoc.org/ — v3=Boot4 / v2=Boot3 분기 (MEDIUM)

**WebSearch (교차 검증)**
- https://www.herodevs.com/blog-posts/spring-boot-versions-eol-dates-and-latest-releases-april-2026 · https://www.danvega.dev/blog/spring-boot-end-of-life — Boot 3.2 OSS EOL 2024-12-31, 최종 3.2.12 (MEDIUM)
- https://endoflife.date/mysql · https://www.percona.com/blog/mysql-8-0-end-of-life-date/ — MySQL 8.0 EOL 2026-04-30 (MEDIUM)
- https://nextjs.org/blog/next-16 · https://nextjs.org/docs/app/guides/upgrading/version-16 — Node 20.9+, TS 5.1+, App Router 기본 (MEDIUM)
- https://endoflife.date/nodejs — Node 24 = Active LTS (2026-08 기준) (MEDIUM)
- https://ffmpeg.org/download.html — 8.0.3 (2026-06-18) (MEDIUM)
- https://github.com/video-dev/hls.js/blob/master/README.md — `Hls.isSupported()` 네이티브 폴백, light 빌드 (MEDIUM)
- https://github.com/naver/fixture-monkey/blob/main/README.md — 1.1.15 (MEDIUM)
- https://www.npmjs.com/package/@playwright/test — 1.62.x (MEDIUM)
- https://github.com/flyway/flyway/issues/3885 — MySQL 8 `flyway-mysql` 필요 (MEDIUM)
- https://www.jobrunr.io/en/documentation/configuration/spring/ — MySQL 스토리지·Boot 3 스타터 (MEDIUM)
- https://hlsbook.net/creating-a-master-playlist-with-ffmpeg/ · https://www.mux.com/articles/how-to-convert-mp4-to-hls-format-with-ffmpeg-a-step-by-step-guide — `-var_stream_map` / `-master_pl_name` (MEDIUM)
- https://www.bento4.com/ · https://developers.google.com/widevine/open-source/shaka-packager — 패키저의 존재 이유 = DRM/CMAF (MEDIUM)
- https://www.chintristan.io/blog/how-to-use-samesite-strict-cookies-with-next-js-and-a-separate-backend · https://u11d.com/blog/nextjs-16-proxy-vs-middleware-bff-guide/ — rewrites 동일 오리진 BFF (MEDIUM)

---
*Stack research for: VOD 스트리밍 서비스 (모아봄)*
*Researched: 2026-08-06*
