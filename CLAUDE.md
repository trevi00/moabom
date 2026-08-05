<!-- GSD:project-start source:PROJECT.md -->
## Project

**모아봄 (moabom)**

넷플릭스 클론 — 스트리밍 서비스 본체. Netflix **테크 블로그의 공개 아키텍처 패턴**을 참조 설계로 삼아,
그들이 실제로 푼 문제(스트리밍·개인화·복원력·실험)를 축소 스케일로 재구현한다.
목적: 대규모 서비스 아키텍처의 실전 구현 포트폴리오 (Netflix 채용 트랙 지식베이스와 연계).

**Core Value:** **행(row) 기반 홈에서 콘텐츠를 골라 HLS 어댑티브 스트리밍으로 끊김 없이 재생하고, 보던 위치에서 이어본다** —
브라우즈→재생→이어보기 루프가 전부 실패해도 이것만은 작동해야 한다.

### Constraints

- **Tech stack (BE)**: Java 17 + Spring Boot 3.2 + Spring Modulith + JPA + MySQL 8.0 — 포트폴리오 표준 스택 (회사 5.7/mybatis 제약 없음; 8.0 윈도 함수·CTE 활용)
- **Tech stack (FE)**: Next.js + React + hls.js — 넷플릭스 UI 문법 (행 스크롤·호버 확장·상세 모달)
- **인프라**: docker-compose (mysql·nginx·prometheus·grafana), CI = GitHub Actions (파이프라인 스테이지)
- **테스트**: JUnit + JaCoCo + FixtureMonkey + Playwright (파이프라인 22 스테이지 그대로)
- **저장소**: `D:/moabom` — 대용량 미디어 산출물 때문에 D 드라이브 배치 (2026-08-05 사용자 확정)
- **법적**: 상표 회피 (Netflix 로고/워드마크 금지) + 저작권-프리 콘텐츠만
- **콘텐츠**: Blender 오픈무비 (Sintel, Big Buck Bunny 등) — 트랜스코딩 파이프라인 실측용 원본
<!-- GSD:project-end -->

<!-- GSD:stack-start source:research/STACK.md -->
## Technology Stack

## 0. 먼저 — 잠긴 코어의 지원 상태 (리스크 등록, 재논의 아님)
| 항목 | 실측 상태 | confidence | 이 프로젝트에서의 실제 영향 |
|---|---|---|---|
| Spring Boot 3.2 | OSS 지원 종료 **2024-12-31**, 마지막 패치 **3.2.12**. 2026-08 현재 3.2.x는 보안 패치 없음. 현행 GA는 4.1.x | HIGH | 로컬/도커 전용 포트폴리오이므로 **운영 리스크는 낮음**. 단 CVE 스캐너(Dependabot/Trivy)가 CI에서 계속 붉게 뜬다 |
| MySQL 8.0 | Extended Support **2026-04-30** 종료. 후속 LTS = 8.4 (2032-04까지) | HIGH | `mysql:8.0` 이미지는 계속 pull 가능. 윈도 함수·CTE·ngram 파서 모두 8.0에서 사용 가능 — 기능 손실 없음 |
| Spring Boot | Spring Modulith | 비고 |
|---|---|---|
| 3.2.x | **1.1.x** | ← 현재 결정 |
| 3.3.x | 1.2.x | Modulith 1.2는 3.2/3.1 하위호환 유지 |
| 3.4.x | 1.3.x | |
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
## 2. 열려 있던 결정 — 확정안 (근거 + 실패 조건)
### 2.1 복원력: `resilience4j-spring-boot3:2.2.0` — **2.3.0 쓰지 말 것**
- **결정**: `io.github.resilience4j:resilience4j-spring-boot3:2.2.0` (+ `spring-boot-starter-aop`, `spring-boot-starter-actuator`).
- **근거**: Resilience4j는 **Boot BOM이 관리하지 않는다** → 명시 핀 필수. 2.3.0은 Boot 3 + Java 17에서
- **이게 실패하는 조건**: 누군가 "최신으로 올리자"며 2.3.x로 bump → 앱이 기동 단계에서 죽는다.
- **사용처(포트폴리오 데모 포인트)**: `recommendation` 모듈 호출에 CircuitBreaker + `fallbackMethod`로
### 2.2 ffmpeg 호출: **자체 `ProcessBuilder` 래퍼** (라이브러리 도입 안 함)
- **결정**: `ingest` 모듈 안에 얇은 `FfmpegCommand` / `FfprobeCommand` 어댑터 (ProcessBuilder 기반).
- **근거**:
- **진행률**: `-progress pipe:1 -nostats` 로 `key=value` 스트림을 stdout에서 파싱 → `IngestJob.progress`
- **이게 실패하는 조건**:
### 2.3 비동기 잡 큐: **도메인 `IngestJob` 상태머신 + Modulith 이벤트** (Spring Batch 아님)
- **결정**:
- **근거 (왜 Spring Batch 5.1.2가 아닌가)**: Spring Batch는 **chunk-oriented ETL**(read-process-write)
- **이게 실패하는 조건**: ffmpeg 실행 중 앱이 죽으면 잡이 `TRANSCODING`에 **영구 고착**된다.
- ⚠️ Modulith 재발행 프로퍼티 이름은 버전에 따라 다르다
- **대안 트립와이어**: 인제스트를 **멀티 인스턴스로 분산**하거나 재시도/백오프 대시보드가 필요해지는
### 2.4 HLS 패키징: **ffmpeg HLS muxer 단독** (Shaka Packager / Bento4 미도입)
- **결정**: 인코딩과 패키징을 ffmpeg 한 방에.
- **근거**: Shaka Packager / Bento4의 존재 이유는 **DRM(Widevine/PlayReady) 암호화와 CMAF 정밀 제어**다.
- **이게 실패하는 조건**: (a) `-hls_time`을 GOP 길이와 맞추지 않으면 세그먼트 경계가 키프레임에
- **트립와이어**: DRM/암호화가 요구되면 → Shaka Packager 도입 (그때 ffmpeg는 인코딩만, 패키징은 위임).
### 2.5 검색: **MySQL 8.0 FULLTEXT + `WITH PARSER ngram`** (Elasticsearch 미도입)
- **결정**:
- **근거**: MySQL의 ngram 파서는 **CJK(중·일·한) 전용으로 만들어진 것**이며 InnoDB에서 지원된다.
- **알려진 한계 (공식 문서 실측)**:
- **이게 실패하는 조건**: 사용자가 **초성 검색**("ㅅㅌ" → 신세계)이나 오타 허용을 기대하면 ngram은 답을
- **참고**: MySQL의 MeCab 파서 플러그인은 **일본어 전용**이다 — 한국어 대안이 아니다.
### 2.6 인증/세션: **Spring Security 6.2.8 + Spring Session JDBC 3.2.6 + Next.js `rewrites` 동일 오리진**
- **결정**:
- **근거**: 별도 오리진(`localhost:3000` ↔ `localhost:8080`)을 유지하면 `SameSite=Strict/Lax` 쿠키가
- **CSRF**: `CookieCsrfTokenRepository.withHttpOnlyFalse()` + **모든 mutating 요청에 "현재" 토큰을 매번 읽어
- **이게 실패하는 조건**: HLS 세그먼트를 nginx가 **다른 오리진**(예: `localhost:8081`)으로 서빙하면,
- **NOT**: JWT를 `localStorage`에 저장하는 구성. XSS 1회 = 계정 탈취이고, 로그아웃/무효화가 불가능하다.
### 2.7 FE 재생: hls.js 1.6.16 — **client-only + 네이티브 폴백**
- **결정 패턴** (App Router):
- **근거**: Safari/iOS는 **MSE 없이 네이티브 HLS**를 재생한다 → `Hls.isSupported()`가 false. 폴백이 없으면
- **번들**: full 빌드 ≈ **70KB gzip**, `hls.light.*` 빌드(자막·대체 오디오·EME 제외) ≈ **50KB gzip**.
- **이게 실패하는 조건**: `next/dynamic`이나 `useEffect` 밖에서 hls.js를 import 하면
- **NOT**: `react-player` / `react-hls-player` 래퍼. 우리는 **버퍼 상태·레벨 전환·ERROR 이벤트를 직접
### 2.8 API 문서: `springdoc-openapi-starter-webmvc-ui:2.3.0`
- **결정**: **2.3.0** 고정. 2.3.0이 Spring Boot **3.2.0** 대응으로 릴리즈된 버전(2023-12-03,
- **이게 실패하는 조건**: `3.x`(Boot 4 전용)나 최신 `2.8.x`(Boot 3.5 타겟)로 올리면 Boot 3.2와의 조합이
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
## 3. Installation
### `build.gradle` (Gradle 8.x, Groovy DSL)
### Frontend
### docker-compose 발췌 (검색 파서 설정이 핵심)
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
## 6. Stack Patterns by Variant
- MySQL ngram FULLTEXT + BOOLEAN MODE, 별도 검색 인프라 없음.
- lolomo 행 조립은 요청 시 계산 + Caffeine/Spring Cache 인메모리 캐시로 충분.
- MySQL 8.0 윈도 함수(`ROW_NUMBER() OVER (PARTITION BY genre ORDER BY view_count DESC)`)를
- Modulith `@ApplicationModuleListener` → 이벤트 **externalization**(아웃박스)으로 전환하고
- `Hls.isSupported()` false 분기 → `video.src = m3u8` 네이티브 경로. Playwright에 **webkit 프로젝트 추가**.
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
## 9. Confidence & 미검증 항목 (정직 표기)
## Sources
- `/spring-projects/spring-modulith` — `@ApplicationModuleListener` 합성 정의(@Async + REQUIRES_NEW +
- https://docs.spring.io/spring-boot/docs/3.2.12/reference/html/dependency-versions.html — Boot 3.2.12 관리 버전 전체 (HIGH)
- https://docs.spring.io/spring-boot/docs/3.2.12/gradle-plugin/reference/htmlsingle/ — "requires Gradle 7.x (7.5 or later) or 8.x" (HIGH)
- https://dev.mysql.com/doc/refman/8.0/en/fulltext-search-ngram.html — ngram_token_size read-only/기본2, WITH PARSER 문법, CJK 제약 (HIGH)
- https://search.maven.org/solrsearch/select?q=g:org.springframework.modulith+AND+a:spring-modulith-bom — 1.1.12가 1.1 라인 최고 패치 (HIGH)
- https://registry.npmjs.org/hls.js/latest — 1.6.16 (HIGH)
- https://github.com/spring-projects/spring-modulith/issues/971 — Boot↔Modulith 호환 매트릭스 (HIGH)
- https://github.com/resilience4j/resilience4j/issues/2265 — 2.3.0 기동 실패(RxJava3FallbackDecorator), 2.2.0 안전 (MEDIUM)
- https://springdoc.org/ — v3=Boot4 / v2=Boot3 분기 (MEDIUM)
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
<!-- GSD:stack-end -->

<!-- GSD:conventions-start source:CONVENTIONS.md -->
## Conventions

Conventions not yet established. Will populate as patterns emerge during development.
<!-- GSD:conventions-end -->

<!-- GSD:architecture-start source:ARCHITECTURE.md -->
## Architecture

Architecture not yet mapped. Follow existing patterns found in the codebase.
<!-- GSD:architecture-end -->

<!-- GSD:skills-start source:skills/ -->
## Project Skills

No project skills found. Add skills to any of: `.claude/skills/`, `.agents/skills/`, `.cursor/skills/`, or `.github/skills/` with a `SKILL.md` index file.
<!-- GSD:skills-end -->

<!-- GSD:workflow-start source:GSD defaults -->
## GSD Workflow Enforcement

Before using Edit, Write, or other file-changing tools, start work through a GSD command so planning artifacts and execution context stay in sync.

Use these entry points:
- `/kha-run-adhoc` for small fixes, doc updates, and ad-hoc tasks
- `/kha-debug` for investigation and bug fixing
- `/kha-execute-phase` for planned phase work

Do not make direct repo edits outside a GSD workflow unless the user explicitly asks to bypass it.
<!-- GSD:workflow-end -->



<!-- GSD:profile-start -->
## Developer Profile

> Profile not yet configured. Run `/kha-user-profile` to generate your developer profile.
> This section is managed by `generate-claude-profile` -- do not edit manually.
<!-- GSD:profile-end -->
