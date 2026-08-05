# Architecture Research — 모아봄 (moabom)

**Domain:** VOD 스트리밍 서비스 (row 기반 개인화 홈 + HLS 어댑티브 재생 + 관리자 인제스트)
**Researched:** 2026-08-06
**Confidence:** HIGH (Netflix 원문 13종 실제 확보 + Spring Modulith/RFC 8216/MySQL 공식 문서 확보) / 일부 항목 미확보는 §0.2 에 명시

---

## 0. 인용 대장 (citation ledger)

> 리서치 규율 [확정]: 각 패턴의 Netflix TechBlog 원문을 **실제 확보**해 인용한다. 확보 실패는 "미확보"로 표기한다.

### 0.1 확보된 원문 (13종)

| # | 원문 | 발행일 / 저자 | 확보 | 이 문서에서 쓰이는 곳 |
|---|---|---|---|---|
| N1 | [Learning a Personalized Homepage](https://netflixtechblog.com/learning-a-personalized-homepage-aa8ec670359a) | 2015-04-09 / Chris Alvino, Justin Basilico | ✅ | lolomo 구조, row selection, dedup |
| N2 | [Netflix Recommendations: Beyond the 5 Stars (Part 1)](https://netflixtechblog.com/netflix-recommendations-beyond-the-5-stars-part-1-55838468f429) | 2012-04-06 / Xavier Amatriain, Justin Basilico | ✅ | row = 개인화 단위, Top10 row |
| N3 | [System Architectures for Personalization and Recommendation](https://netflixtechblog.com/system-architectures-for-personalization-and-recommendation-e081aa94b5d8) | 2013-03-27 / Amatriain, Basilico | ✅ | offline/nearline/online 분리, 폴백 |
| N4 | [Embracing the Differences: Inside the Netflix API Redesign](https://netflixtechblog.com/embracing-the-differences-inside-the-netflix-api-redesign-15fd8b3dc49d) | 2012-07-09 / Daniel Jacobson | ✅ | **BFF 의 실제 원형** (서버 조립) |
| N5 | [Engineering Trade-Offs and The Netflix API Re-Architecture](https://netflixtechblog.com/engineering-trade-offs-and-the-netflix-api-re-architecture-64f122b277dd) | 2016-08-23 / Katharina Probst, Justin Becker | ✅ | **playback API vs discovery API 분리** |
| N6 | [How Netflix Scales its API with GraphQL Federation (Part 1)](https://netflixtechblog.com/how-netflix-scales-its-api-with-graphql-federation-part-1-ae3557c187e2) | 2020-11-09 / Tejas Shikhare | ✅ | BFF 중복 데이터페칭 문제 |
| N7 | [Fault Tolerance in a High Volume, Distributed System](https://netflixtechblog.com/fault-tolerance-in-a-high-volume-distributed-system-91ab4faae74a) | 2012-02-29 / Ben Christensen | ✅ | Hystrix 격벽·서킷·폴백 |
| N8 | [Netflix's Viewing Data: How We Know Where You Are in House of Cards](https://netflixtechblog.com/netflixs-viewing-data-how-we-know-where-you-are-in-house-of-cards-608dd61077da) | 2015-01-27 / Fisher-Ogden, Zimmer, Kojo, Li | ✅ | 이어보기(bookmark) 쓰기 경로 |
| N9 | [Per-Title Encode Optimization](https://netflixtechblog.com/per-title-encode-optimization-7e99442b62a2) | 2015-12-14 / Aaron, Li, Manohara, De Cock, Ronca | ✅ | 비트레이트 래더 |
| N10 | [Rebuilding Netflix Video Processing Pipeline with Microservices](https://netflixtechblog.com/rebuilding-netflix-video-processing-pipeline-with-microservices-4e5e6310e359) | 2024-01-10 / Guo 외 11인 | ✅ | 인제스트 파이프라인 분해 |
| N11 | [It's All A/Bout Testing: The Netflix Experimentation Platform](https://netflixtechblog.com/its-all-a-bout-testing-the-netflix-experimentation-platform-4e1ca458c15) | 2016-04-29 / Urban, Sreenivasan, Kannan | ✅ | 실험 배정 모델 |
| N12 | [The Netflix Simian Army](https://netflixtechblog.com/the-netflix-simian-army-16e57fbab116) | 2011-07-19 / Izrailevsky, Tseitlin | ✅ | 카오스 스테이지 |
| N13 | [Introducing Atlas: Netflix's Primary Telemetry Platform](https://netflixtechblog.com/introducing-atlas-netflixs-primary-telemetry-platform-bd31f4d8ed9a) | 2014-12-12 / Harrington, Rapoport | ✅ | 차원 메트릭 |
| N14 | [Netflix Open Connect](https://openconnect.netflix.com/en/) (공식 사이트, TechBlog 아님) | 상시 갱신 | ✅ | 미디어 경로 우회 근거 |

**보조 1차 문서 (비-Netflix, 전부 확보)**: [RFC 8216 (HLS)](https://www.rfc-editor.org/rfc/rfc8216.html) · [Spring Modulith fundamentals](https://docs.spring.io/spring-modulith/reference/fundamentals.html) · [Spring Modulith events](https://docs.spring.io/spring-modulith/reference/events.html) · [MySQL 8.0 Locking Reads](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking-reads.html) · Resilience4j (Context7 `/resilience4j/resilience4j`).

### 0.2 미확보 — 정직 표기 (허위 인용 금지)

| 항목 | 상태 | 영향 |
|---|---|---|
| **회원용 검색(member-facing search) 전용 Netflix 원문** | ❌ 존재 확인 실패. 검색으로 나온 것은 *Studio Search*(사내 스튜디오용, 2022)와 "Recommending for the World"(2016) 내 부분 언급뿐 | 검색 모듈 설계는 Netflix 원형 인용 **없이** 진행. "Netflix 는 이렇게 한다" 주장 금지 |
| **My List 전용 Netflix 원문** | ❌ 없음 | My List 는 원형 인용 없이 일반 설계로 처리 |
| **Apple HLS Authoring Specification** | ❌ WebFetch 가 제목만 반환 (본문 미확보) | 세그먼트 길이/래더 권고는 RFC 8216 (확보) 로만 근거. 구현 전 1회 재확보 권장 |
| **ffmpeg 공식 HLS muxer 문서** | ❌ `ffmpeg.org` ECONNREFUSED (79.124.17.100:443) | ffmpeg 옵션 세부는 STACK.md 로 이관, 재시도 필요 |
| **Netflix 가 HLS 아닌 DASH 를 쓴다는 1차 출처** | ❌ 2차 자료만 (LOW) | §5 "우리는 HLS" 근거를 Netflix 대칭성이 아니라 hls.js/브라우저 지원으로 세울 것 |
| ChAP (Chaos Automation Platform) 원문 | ❌ 미확보 | 카오스는 N12 (Simian Army) 만 인용 |

### 0.3 인용 무결성 주의 ⚠️

본 문서의 인용문은 **WebFetch 추출기(소형 모델)를 거친 결과**다. 문장 단위 왜곡 가능성이 0 이 아니다.
→ **구현 시점에 해당 패턴을 실제로 코딩하기 직전, 그 원문 1건만 사람이 직접 열어 인용 대조**할 것 (전수 대조 불요, 착지 직전 1건 대조).
→ 특히 N9 (Per-Title) 의 "5800 kbps 1080p" 고정 래더 수치는 추출기가 표를 완전 재현하지 못했다고 스스로 밝혔다 — 수치 인용 시 반드시 재확인.

---

## 1. Standard Architecture

### 1.1 System Overview

```
┌────────────────────────────────────────────────────────────────────────────┐
│  CLIENT — Next.js + React + hls.js                                          │
│  홈(lolomo 렌더) · 상세 모달 · 플레이어 · 검색                              │
└────────┬───────────────────────────────────────────┬───────────────────────┘
         │ ① JSON API (조립 완료 응답)                │ ② 미디어 (앱 완전 우회)
         │   GET /api/home → rows[] 통째              │   GET /media/**/*.m3u8|*.ts
         ▼                                            ▼
┌──────────────────────────────────────────┐  ┌──────────────────────────────┐
│  Spring Boot 3.2 — 단일 배포 (Modulith)  │  │  nginx (정적 + 캐시)          │
│                                          │  │  Open Connect 축소 대응 (N14) │
│  ┌────────────────────────────────────┐  │  └───────────────┬──────────────┘
│  │ bff — 조립 전용 · 영속성 0          │  │                  │ read-only
│  └─┬──────┬──────┬──────┬──────┬─────┘  │                  │
│    ▼      ▼      ▼      ▼      ▼        │  ┌───────────────┴──────────────┐
│ ┌──────┐┌──────┐┌──────┐┌──────┐┌──────┐│  │  미디어 볼륨                  │
│ │catalo││playba││profil││recomm││experi││  │  D:/moabom/media/{titleId}/  │
│ │  g   ││ ck   ││  e   ││endat.││ ment ││  │    {ver}/master.m3u8 ...     │
│ └──────┘└──────┘└──────┘└──────┘└──────┘│  └───────────────▲──────────────┘
│ ┌──────┐  ┌────────────────────────────┐│                  │ write-only
│ │ingest│──┼─ ffmpeg (subprocess) ──────┼┼──────────────────┘
│ └──────┘  └────────────────────────────┘│
│ ┌──────────────────────────────────────┐│
│ │ Actuator /actuator/prometheus        ││
│ └──────────────────────────────────────┘│
└──────────────┬───────────────────────────┘
               ▼
     ┌──────────────────┐    ┌────────────┐    ┌──────────┐
     │  MySQL 8.0       │    │ Prometheus │───▶│ Grafana  │
     │  (메타데이터만,   │    └────────────┘    └──────────┘
     │   미디어 blob ✗) │
     └──────────────────┘
```

**이 그림의 단일 핵심 주장**: 화살표 ①(JSON)과 ②(미디어)는 **절대 만나지 않는다**. Spring 은 미디어 바이트를 한 번도 만지지 않는다. Netflix 가 Open Connect 로 한 일의 축소판이다 — 원문(N14): 임베디드 OCA 배치는 *"offload a substantial amount of Netflix content traffic from peering or transport circuits"*. 우리 스케일에서 "offload" 대상은 ISP 회선이 아니라 **JVM 스레드풀과 힙**이다.

### 1.2 Component Responsibilities

| 컴포넌트 | 소유(owns) | 소유하지 않음(explicitly not) | 구현 |
|---|---|---|---|
| **bff** | HTTP 계약, 응답 셰이프, 배치 하이드레이션, 응답 캐시 | 도메인 정책 판단 0 (재생 가능?·키즈 차단? 전부 도메인) | `@RestController` + 조립 서비스. `@Entity`/Repository **금지** |
| **catalog** | title/genre/asset 포인터, 발행 상태, 검색(제목·장르), Top-N 집계 | 개인화, 시청 위치 | JPA + MySQL 8.0 윈도 함수(Top-N) |
| **playback** | 시청 위치(bookmark), 재생 티켓 발급, 시청 이력 | 미디어 바이트, 트랜스코딩 | JPA upsert + 정책 검사 |
| **profile** | 가구 계정, N 프로필, 키즈 플래그, **My List** | 인증 토큰 발급 이상의 세션 관리(→ Spring Security 인프라) | JPA |
| **recommendation** | **page generation** — 행 목록 선정·행 내 정렬·중복 제거 | 타이틀 메타데이터 원본, 시청 위치 원본 (전부 spi 로 읽기만) | RowProvider 레지스트리 |
| **experiment** | 테스트 정의, 결정론적 셀 배정, 배정 로그 | 통계 분석 UI (후속) | 순수 해시 + 배정 테이블 |
| **ingest** | 업로드 원본, 트랜스코딩 잡 상태, **미디어 볼륨 쓰기 유일 주체** | 카탈로그 발행 결정 (이벤트만 발행) | 잡 테이블 + ffmpeg subprocess |
| **shared** (OPEN) | ID 값 타입, 페이징 타입만 | 그 외 전부 | `@ApplicationModule(type = OPEN)` |
| **nginx** | 미디어 읽기 서빙, 캐시 헤더 | 인증 판단 (M1 은 무인증 미디어) | docker-compose |
| **MySQL 8.0** | 전 도메인 영속 + 잡 큐 | 미디어 바이트 | 단일 스키마, 모듈별 테이블 접두 |

> **모듈 수 정정**: 브리프의 "6 모듈 + BFF 레이어"를 유지하되, BFF 를 **패키지 수준에서도 모듈로 선언**(`bff`)해 `ApplicationModules.verify()` 가 "도메인 → bff 역방향 의존"을 실제로 잡게 한다. 도메인 모듈 6종은 그대로. 총 8 (6 도메인 + bff + shared).

---

## 2. Recommended Project Structure

Spring Modulith 의 모듈 판정 규칙은 **패키지 구조 그 자체**다 — 공식 문서(fundamentals):
> *"By default, each direct sub-package of the main package is considered an application module package."*
> *"the `order` package is considered an API package. Code from other application modules is allowed to refer to types within that. `order.internal`, just as any other sub-package of the application module base package, is considered an internal one."*

즉 **레이어-우선 패키징(`controller/`, `service/`, `repository/` 최상위)을 쓰면 모듈이 1개로 붕괴한다.** 반드시 모듈-우선.

```
src/main/java/com/moabom/
├── MoabomApplication.java              # @SpringBootApplication (main package)
│
├── bff/                                # 모듈: 조립 전용
│   ├── HomeController.java             #   GET /api/home  → lolomo JSON 통째
│   ├── TitleController.java            #   GET /api/titles/{id}
│   ├── PlayController.java             #   POST /api/play/{titleId}
│   ├── LolomoAssembler.java            #   행 plan + 배치 하이드레이션
│   └── dto/                            #   응답 DTO (클라이언트 계약)
│
├── catalog/                            # 모듈: API 패키지 = 이 레벨
│   ├── CatalogApi.java                 #   ← bff 가 쓰는 공개 API
│   ├── TitleCard.java, TitleDetail.java
│   ├── spi/                            #   @NamedInterface("spi") — recommendation/playback 전용 좁은 포트
│   │   ├── package-info.java
│   │   ├── TitleQueryPort.java         #   findIdsByGenre / findTopN / findSimilar
│   │   └── PublishStatePort.java       #   isPlayable(titleId)
│   ├── search/                         #   @NamedInterface("search")  ← 승격 대비 격리
│   └── internal/                       #   Entity · Repository · 서비스 구현 (외부 참조 불가)
│
├── playback/
│   ├── PlaybackApi.java                #   startPlayback(profileId,titleId) → PlaybackTicket
│   │                                   #   heartbeat(profileId,titleId,posSec)
│   ├── spi/                            #   WatchHistoryPort (recentWatches / inProgress)
│   └── internal/                       #   BookmarkEntity, 정책 검사
│
├── profile/
│   ├── ProfileApi.java                 #   가구 계정 · 프로필 전환 · My List CRUD
│   ├── spi/                            #   ProfilePort (isKids, myListTitleIds)
│   └── internal/
│
├── recommendation/
│   ├── RecommendationApi.java          #   buildLolomoPlan(profileId) → RowPlan[]
│   ├── row/                            #   ★ OCP 확장점: 행 1종 = 파일 1개 추가
│   │   ├── RowProvider.java            #     인터페이스 (모듈 내부 SPI)
│   │   ├── ContinueWatchingRowProvider.java
│   │   ├── MyListRowProvider.java
│   │   ├── TopTenRowProvider.java
│   │   ├── GenreRowProvider.java
│   │   └── BecauseYouWatchedRowProvider.java
│   ├── internal/PageAssembler.java     #   행 선정 · 순서 · 중복 제거
│   └── internal/FallbackRows.java      #   비개인화 폴백
│
├── experiment/
│   ├── ExperimentApi.java
│   ├── spi/AssignmentPort.java         #   cellFor(profileId, testKey)
│   └── internal/                       #   결정론 해시 + 배정 로그
│
├── ingest/
│   ├── IngestApi.java                  #   관리자: 업로드 등록 → jobId
│   ├── event/TitleAssetPublished.java  #   @NamedInterface("event") — catalog 가 구독
│   └── internal/
│       ├── JobRepository.java          #   SKIP LOCKED 큐
│       ├── TranscodeWorker.java        #   @Scheduled 폴러
│       └── FfmpegRunner.java           #   ProcessBuilder 래퍼 (유일한 ffmpeg 접점)
│
└── shared/                             # @ApplicationModule(type = OPEN)
    ├── package-info.java
    └── TitleId.java, ProfileId.java

src/test/java/com/moabom/
└── ModularityTests.java                # ★ Phase 0 에 반드시 존재
```

### 2.1 Structure Rationale

- **`spi/` vs 모듈 루트 분리**: 모듈 루트(=API)는 **bff 용**, `spi/`는 **다른 도메인 모듈용**. 둘을 섞으면 recommendation 이 `CatalogApi` 전체(상세·검색 포함)에 결합돼 catalog 리팩터링이 recommendation 을 깬다. 좁은 포트를 강제하는 것이 `allowedDependencies = "catalog :: spi"` 의 존재 이유다.
- **`catalog/search/` 를 처음부터 별도 named interface 로**: 검색은 M1 에서 catalog 안에 산다(§3.6). 하지만 한국어 형태소(ES 도입)가 들어오면 별도 모듈로 승격된다. 지금 named interface 로 격리해 두면 승격이 **패키지 이동 + `@ApplicationModule` 1줄**이 된다. 미리 모듈로 쪼개면 카탈로그 데이터를 통째 투영해야 해서 순손해.
- **`ingest/event/`**: ingest 는 catalog 를 **모른다**. 이벤트 타입만 공개한다. 방향은 항상 `catalog ← (구독) ← ingest 이벤트`.
- **`shared` 를 OPEN 으로**: 공유 커널은 쓰레기통이 되는 게 기본값이다. 방어책 = **크기 상한 규칙**: `shared` 에 클래스를 추가하려면 "2개 이상 모듈이 이미 쓰고 있음"을 PR 설명에 증명. 상한이 없으면 6개월 뒤 `shared/util/` 이 생긴다.

---

## 3. Architectural Patterns

### Pattern 1 — 서버 조립 BFF (클라이언트 조립 금지)

**What:** 홈 1회 요청 = 완성된 lolomo JSON 1개. 클라이언트는 행 단위 fan-out 을 하지 않는다.

**원문 근거 (N4, 2012, Daniel Jacobson)** — 이것이 브리프가 말한 "BFF 원형"의 실제 출처다:
> *"Netflix has found substantial limitations in the traditional one-size-fits-all (OSFA) REST API approach."*
> *"Reducing the number of network requests improves performance, in some cases by several seconds."*
> *"Some client code now resides on the server"* — 어댑터가 *"pruning out unwanted fields, error handling and retries, formatting the response"* 를 담당.

**보강 (N6, 2020, Tejas Shikhare)** — BFF 를 여러 개 두면 무엇이 썩는가:
> *"many teams spent considerable effort building duplicative data-fetching code and aggregation layers to support their product needs."*
> *"Inconsistent data across different Studio applications was the top support issue in Studio Engineering in 2018."*

**⚠️ 원문 조건 vs 우리 스케일 (과낙관 경계)**
- N6 는 **Studio Edge**(사내 제작 툴 그래프) 이야기다. 회원용 스트리밍 API 가 아니다. "넷플릭스 회원 홈이 GraphQL federation 으로 서빙된다"고 쓰면 **허위 인용**이다.
- N6 가 federation 을 도입한 조건은 *"70 DGSes and hundreds of developers"*. **우리는 개발자 1명, DGS 0개**다. Federation 은 우리 문제를 하나도 풀지 않는다 — 브리프의 "단일 BFF 레이어" 결정이 맞다.
- N4 가 BFF 어댑터를 만든 조건은 "기기 수백 종 × UI 팀 다수". **우리는 클라이언트 1종(웹)**. 따라서 **클라이언트별 BFF 는 만들지 않는다. BFF 는 정확히 1개.**

**Trade-offs:** 서버가 UI 셰이프를 안다 → UI 변경이 서버 변경을 유발(결합). 수용한다. 대가로 얻는 것: 라운드트립 1회, 캐싱 지점 1개, 중복 제거를 서버에서 수행 가능.

```java
// bff/LolomoAssembler.java — 조립만, 정책 판단 0
public LolomoResponse assemble(ProfileId profileId) {
    List<RowPlan> plan = recommendation.buildLolomoPlan(profileId);   // 행 = titleId 목록만

    // ★ 배치 하이드레이션: 행 수만큼 쿼리하지 않는다 (N+1 금지)
    Set<TitleId> ids = plan.stream().flatMap(r -> r.titleIds().stream()).collect(toSet());
    Map<TitleId, TitleCard> cards = catalog.getTitleCards(ids);        // IN (...) 1회
    Map<TitleId, Integer> progress = playback.progressFor(profileId, ids); // 1회

    return LolomoResponse.of(plan, cards, progress);
}
```

### Pattern 2 — 동기 조회는 named interface, 상태 변화 통지는 이벤트

**What:** 모듈 간 통신 규칙을 **딱 두 줄**로 고정한다.

| 상황 | 수단 | 예 |
|---|---|---|
| 지금 이 응답을 만들려면 저쪽 데이터가 필요 | `xxx::spi` **직접 호출** | recommendation → `catalog::spi.findTopN()` |
| 내 상태가 변했고 남이 반응해야 함 | `@ApplicationModuleListener` **이벤트** | ingest `TitleAssetPublished` → catalog 발행 처리 |

이벤트를 조회에 쓰면(요청-응답을 이벤트로 흉내) 지옥이 열리고, 조회를 통지에 쓰면(ingest 가 catalog 를 직접 호출) 방향 의존이 뒤집힌다. 규칙을 술어로 고정한다.

**Spring Modulith 공식 문서 근거 (events)**:
> `@ApplicationModuleListener` 는 `@Async` + `@Transactional(propagation = REQUIRES_NEW)` + `@TransactionalEventListener` 를 합친 단축이다.
> *"On event publication, it finds out about the transactional event listeners that will get the event delivered and writes entries for each of them … into an event publication log as part of the original business transaction."*
> *"In case the listener fails, the log entry stays untouched so that retry mechanisms can be deployed … Automatic re-publication of the events can be enabled via the `spring.modulith.events.republish-outstanding-events-on-restart` property."*

→ **인제스트 완료 통지에 이걸 쓴다.** ffmpeg 가 30분 돌고 나서 catalog 리스너가 죽으면, 이벤트 발행 로그가 남아 재시작 시 재발행된다. 재트랜스코딩 0회. `spring-modulith-starter-jpa` 필요.

```java
// ingest/internal/TranscodeWorker.java
@Transactional
void complete(JobId jobId, HlsAsset asset) {
    jobs.markDone(jobId);
    events.publishEvent(new TitleAssetPublished(               // 같은 트랜잭션에 로그 기록
        asset.titleId(), asset.manifestPath(), asset.renditions()));
}

// catalog/internal/AssetPublicationListener.java
@Component
class AssetPublicationListener {
    @ApplicationModuleListener                                  // 비동기 + 새 트랜잭션
    void on(TitleAssetPublished e) {
        titles.publish(e.titleId(), e.manifestPath());          // 여기서 비로소 홈에 노출
    }
}
```

### Pattern 3 — 미디어 경로 우회 (Open Connect 축소)

**What:** 매니페스트/세그먼트는 nginx 가 직접 서빙. Spring 은 **경로 문자열만** 반환.

**원문 근거 (N14, Open Connect)**: OCA 는 *"the same capabilities as the OCAs that we use in our 60+ global data centers"*, 배치 효과는 *"offload a substantial amount of Netflix content traffic"*, 콘텐츠는 *"nightly content fills and updates"* 로 사전 배치.

**⚠️ 원문 조건 vs 우리 스케일**
- Netflix 는 **ISP 내부에 하드웨어를 두어 백본 회선을 아낀다**. 우리는 CDN 도 ISP 파트너십도 없다. 이식되는 것은 **"발행(publish)과 서빙(serve)의 분리"라는 구조 하나**뿐이다.
- 실제로 축소 이식할 수 있는 두 가지: (1) **사전 배치(pre-fill)** = 트랜스코딩은 재생 시점이 아니라 인제스트 시점에 끝나 있다, (2) **읽기 전용 정적 서빙**.
- 이식 불가: 지리 분산, 나이틀리 필, 피어링. 이걸 "CDN 을 구현했다"고 쓰면 과장이다. **"CDN 오리진 계층의 개념 증명"** 이 정직한 표현이다.

**미디어 레이아웃 = ingest ↔ nginx 계약 (버전 디렉터리 필수)**
```
/media/{titleId}/{assetVersion}/master.m3u8          ← EXT-X-STREAM-INF 3종
/media/{titleId}/{assetVersion}/1080p/index.m3u8, seg_00001.ts ...
/media/{titleId}/{assetVersion}/720p/...
/media/{titleId}/{assetVersion}/480p/...
```
- **원자적 발행 규칙**: 새 버전 디렉터리에 **전부 쓴 뒤** DB 포인터를 교체한다. 이미 발행된 디렉터리는 **절대 수정하지 않는다**. 발행 중 부분 매니페스트를 읽는 사고를 구조적으로 제거한다.
- RFC 8216 근거: `EXT-X-PLAYLIST-TYPE:VOD` 는 *"indicates that the Playlist file MUST NOT change."* → VOD 로 선언한 순간 사후 수정은 스펙 위반이다. 버전 디렉터리는 스펙 준수의 결과이지 취향이 아니다.
- `EXT-X-TARGETDURATION` = *"specifies the maximum Media Segment duration"*, RFC 왈 *"A typical target duration is 10 seconds."* → 세그먼트 6~10초, **키프레임 간격의 정수배**로 고정(모든 렌디션 동일 GOP)해야 렌디션 전환이 매끄럽다.
- `master.m3u8` 의 `EXT-X-STREAM-INF` 는 *"MUST include the BANDWIDTH attribute"* — 이 값이 hls.js 의 ABR 판단 입력이다. 잘못 쓰면 어댑티브가 죽는다.

### Pattern 4 — 도메인 정책은 도메인에, BFF 는 조립만

**What:** "이 프로필이 이 타이틀을 재생할 수 있는가?"는 **playback 모듈이 판단**한다. BFF 는 묻지 않는다.

**원문 근거 (N5, 2016)** — Netflix 는 **playback API 와 discovery API 를 아예 분리**했다. 그 이유가 우리 설계에 그대로 유효하다:
> *"We want each team to own and operate in production what they build."*
> *"there is no perfect solution. There is no right or wrong, only trade-offs."*

**⚠️ 원문 조건 vs 우리 스케일**: N5 의 분리 동인은 **팀 소유권과 24/7 운영 책임**(*"only the API team is responsible for the full operations of the API, including releases, 24/7 support, rollbacks"*)이다. 1인 프로젝트에 팀 경계는 없다. 그래서 우리는 **물리 분리하지 않고 모듈 경계로만** 재현한다 — 단, 재생 경로가 홈 경로와 **다른 가용성 등급**을 갖는다는 통찰은 유효하다: 추천이 죽어도 재생은 살아야 한다(Core Value). 이것이 §3.5 폴백 설계의 근거다.

```java
// playback/PlaybackApi.java — 정책이 여기 있다
public PlaybackTicket startPlayback(ProfileId p, TitleId t) {
    if (!catalogSpi.isPlayable(t))  throw new TitleNotPlayable(t);   // catalog::spi
    if (profileSpi.isKids(p) && !catalogSpi.isKidsSafe(t))
        throw new MaturityBlocked(t);                                // profile::spi
    int resume = bookmarks.positionOf(p, t).orElse(0);
    return new PlaybackTicket(catalogSpi.manifestPath(t), resume);
}
```

### Pattern 5 — 추천 = page generation (허브 방향은 recommendation → 나머지)

**What:** recommendation 이 **행 목록(RowPlan)** 을 만든다. 각 행은 `{rowType, title, titleIds[]}` — **메타데이터 없음**. 하이드레이션은 BFF.

**원문 근거 (N1, 2015)**:
> *"Currently, the Netflix homepage on most devices is structured with videos (movies and TV shows) organized into thematically coherent rows presented in a two-dimensional layout."*
> 조립 단계는 *"a row selection algorithm to assemble the full page"* 로 끝난다.
> 문제 규모: *"To algorithmically create a good personalized homepage means assembling one page per member profile and device from thousands of videos"*, *"from easily tens of thousands of potential rows"*, 그리고 기기별 행 수 제한·행 길이·**중복 제거**·성숙도 필터가 제약으로 명시된다.

**원문 근거 (N2, 2012)**:
> *"Personalization starts on our homepage, which consists of groups of videos arranged in horizontal rows."*
> *"Each row has a title that conveys the intended meaningful connection between the videos in that group. Most of our personalization is based on the way we select rows, how we determine what items to include in them, and in what order to place those items."*
> *"75% of what people watch is from some sort of recommendation."*
> Top10: *"this is our best guess at the ten titles you are most likely to enjoy."*
> (정직 표기: N2 는 "Continue Watching" 이라는 행 이름을 **명시하지 않는다**. 이어보기 행은 우리 설계이지 N2 인용이 아니다.)

**⚠️ 원문 조건 vs 우리 스케일**
| 원문 전제 | 우리 실제 | 귀결 |
|---|---|---|
| 후보 행 "수만 개" (N1) | 장르 ~15개 + 고정 행 4종 = **후보 행 20개 내외** | 행 *선택* 알고리즘이 사실상 불필요. **행 정렬(ordering)** 만 개인화하면 충분 |
| 회원 5,700만 (N1) × 기기별 페이지 | 프로필 수십 | 사전계산(offline) 불필요. **요청 시 계산(online)** 이 정답 |
| ML 랭커, 다단계 후보 생성 | 시청 이력·장르 카운트 | 규칙 기반 점수로 시작. CF 는 브리프대로 후속 |
| 중복 제거·성숙도 필터 (N1) | **동일하게 필요** | 유일하게 100% 이식되는 제약 — PageAssembler 에서 구현 |

**설계 결정 — 의존 방향 (검토 후 채택)**
- **채택**: `recommendation → {catalog::spi, playback::spi, profile::spi, experiment::spi}`. RowProvider 는 recommendation **내부** 확장점.
- **기각한 대안**: RowProvider 인터페이스를 recommendation 이 공개하고 각 모듈이 구현(플러그인 역전). 더 순수해 보이지만 **"때문에 시청함(BecauseYouWatched)" 행이 catalog(유사도) + playback(이력) 양쪽을 필요로 해서 순환(catalog ↔ playback)을 만든다.** 역전은 단일-소스 행에만 성립한다. 순환 위험 > 순수성 이득.
- OCP 는 유지된다: **새 행 1종 = `row/` 에 파일 1개 추가 + `@Component`**. 기존 파일 수정 0. (L0 설계 원칙 준수)

```java
public interface RowProvider {
    RowType type();
    int basePriority();                        // 정렬 기본값 (experiment 가 덮어씀)
    Optional<RowPlan> build(RowContext ctx);   // titleIds 만 채운다
}

// internal/PageAssembler.java
List<RowPlan> assemble(ProfileId p) {
    RowContext ctx = RowContext.of(p, experimentSpi.cellFor(p, "lolomo_order_v1"));
    return providers.stream()                            // Spring 이 전부 주입
        .map(pr -> pr.build(ctx)).flatMap(Optional::stream)
        .sorted(orderingFor(ctx.cell()))
        .map(dedupe())                                   // ★ N1 의 중복 제거 제약
        .limit(MAX_ROWS).toList();
}
```

### Pattern 6 — 폴백: 서킷브레이커의 정직한 범위

**What:** BFF → recommendation 호출에 Resilience4j `@CircuitBreaker(fallbackMethod=...)`. 폴백 = **비개인화 행**(Top10 + 장르행, catalog 만으로 생성).

**원문 근거 (N7, 2012, Ben Christensen)**:
> *"Netflix API receives more than 1 billion incoming calls per day which in turn fans out to several billion outgoing calls"*
> *"30 dependencies each with 99.99% uptime would result in 2+ hours downtime/month"*
> 격리: *"separate threads on per-dependency thread pools"* — *"If a dependency becomes latent … it can saturate all of the threads in its own thread pool, but Tomcat request threads will timeout or be rejected immediately"*
> 서킷: *"when a DependencyCommand has passed a certain threshold of error (such as 50% error rate in a 10 second period) and will then reject all requests until health checks succeed"*
> 폴백 유형: *"Cache," "Eventual Consistency," "Stubbed Data," "Empty Response ('Fail Silent')"*

**원문 근거 (N3, 2013)** — 폴백은 추천 시스템의 1급 설계 요소다:
> *"A purely online computation may fail to meet its SLA in some circumstances, so it is always important to think of a fast fallback mechanism such as reverting to a precomputed result."*

**⚠️ 원문 조건 vs 우리 스케일 — 이 항목이 가장 정직해야 한다**
- Hystrix 의 스레드풀 격벽이 존재한 이유는 **네트워크 의존(원격 서비스)** 이 지연될 때 Tomcat 스레드를 잠식하기 때문이다. **우리의 recommendation 호출은 같은 JVM 안의 메서드 호출이다.** 격벽이 막을 "원격 지연"이 없다.
- 따라서 정직한 진술: **모듈리스에서 인-프로세스 호출에 건 서킷브레이커는 (a) 예외 발생 시 폴백, (b) 느린 DB 쿼리 누적 시 차단 — 이 둘에는 실효가 있고, 스레드 격리는 얻지 못한다.** "Hystrix 를 이식했다"가 아니라 **"실패 격리의 *가시적* 데모 + 실제 예외 폴백"** 이 맞는 표현이다.
- 실효를 높이려면: `@TimeLimiter` + `CompletableFuture` 로 **시간 제한을 실제로 강제**(느린 쿼리 대비). 이건 진짜 방어다.
- 30개 의존 99.99% 산술(N7)은 우리에게 **적용되지 않는다** — 우리 의존은 MySQL 1개다. 인용은 하되 "우리 수치"로 옮기지 말 것.

```yaml
resilience4j.circuitbreaker:
  instances:
    lolomo:
      slidingWindowType: COUNT_BASED
      slidingWindowSize: 20
      minimumNumberOfCalls: 10
      failureRateThreshold: 50          # N7 의 "50% error rate" 를 그대로
      waitDurationInOpenState: 10s
      permittedNumberOfCallsInHalfOpenState: 3
      registerHealthIndicator: true     # → Actuator → Prometheus 로 상태 노출
```
(설정 키·유효범위는 Resilience4j 공식 소스 `CommonCircuitBreakerConfigurationProperties` 확인 — `failureRateThreshold` 는 1~100 검증됨.)

**데모 시나리오(포트폴리오 가치의 핵심)**: `/actuator` 로 서킷 상태 노출 → 추천 쿼리에 인위적 예외/지연 주입 → Grafana 에서 `resilience4j_circuitbreaker_state` 가 OPEN 으로 전이 → 홈은 **비개인화 행으로 계속 응답**. 이것이 N12 의 축소판이다: *"we have to design a cloud architecture where individual components can fail without affecting the availability of the entire system."*

### Pattern 7 — 인제스트: DB 백드 잡 큐 + ffmpeg subprocess

**What:** 업로드 → `ingest_job` 행(PENDING) → `@Scheduled` 워커가 `SELECT ... FOR UPDATE SKIP LOCKED` 로 1건 집어 ffmpeg 실행 → HLS 산출 → 이벤트 발행.

**MySQL 8.0 공식 문서 근거**:
> *"A locking read that uses `SKIP LOCKED` never waits to acquire a row lock. The query executes immediately, removing locked rows from the result set."*
> *"Queries that skip locked rows return an inconsistent view of the data. `SKIP LOCKED` is therefore not suitable for general transactional work. However, it may be used to avoid lock contention when multiple sessions access the same queue-like table."*

→ 브리프의 MySQL 8.0 선택이 **윈도 함수(Top-N)** 외에 **큐 안전성**이라는 두 번째 정당화를 얻는다.

**원문 근거 (N10, 2024)** — 무엇을 왜 쪼개는가:
> Reloaded 에서 *"the video quality calculation was implemented inside the video encoder module"* 였고, 그 결합 때문에 재인코딩 없이 품질만 재계산하는 게 *"extremely difficult"* 했다.
> 신 구조는 VIS(검사) / CAS(복잡도) / LGS(래더 생성) / VES(인코딩) / VVS(검증) / VQS(품질) 6개로 분리.
> *"distributed chunk-based encoding"* 이 *"greatly reduced the processing latency and increased system resiliency"*.

**⚠️ 원문 조건 vs 우리 스케일**
| N10 전제 | 우리 실제 | 결정 |
|---|---|---|
| 6개 마이크로서비스 | 1개 워커 | **분리하지 않는다.** 단, N10 의 *교훈* 하나는 이식: **"검사/인코딩/검증"을 잡의 명시적 단계(step)로 기록**해 재실행 단위를 만든다. 단계 없는 통짜 잡은 실패 시 처음부터다 |
| 청크 분할 병렬 인코딩 | Sintel(15분) 단일 파일 | **청크 분할 안 함.** 청크는 join·경계 아티팩트 비용을 부르고, 병렬 이득은 코어가 남을 때만 유효. 우리는 **렌디션 3종을 병렬**(1080/720/480)로 하는 것이 자연스러운 병렬 축 |
| VQS(VMAF) 별도 서비스 | — | M1 제외. 단, ffmpeg 로 VMAF 를 뽑아 잡 메타에 기록하는 것은 **저비용 고임팩트 포트폴리오 항목** (후속 후보) |
| Per-title 래더 (N9) | 고정 3단 래더 | **고정 래더로 시작.** N9 는 *"some titles reach very high PSNR (45 dB or more) at bitrates of 2500 kbps or less. On the other extreme, some titles require bitrates of 8000 kbps or more"* 를 근거로 per-title 을 도입했다 — 그 가치는 **카탈로그가 다양할 때** 나온다. 저작권-프리 영상 5~10편에는 per-title 이득이 측정되지 않는다. (⚠️ N9 의 고정 래더 수치는 §0.3 대로 재확인 필요) |

**동시성 상한 필수**: ffmpeg 는 CPU 를 전부 먹는다. 워커 동시 실행 1~2개로 제한하지 않으면 **트랜스코딩 중 홈 응답이 죽는다**(단일 머신). 이는 포트폴리오 데모 중 가장 흔한 사고다.

```java
@Scheduled(fixedDelay = 5000)
void poll() {
    if (running.get() >= MAX_CONCURRENT) return;              // ★ 상한
    jobs.claimOne().ifPresent(this::run);                     // FOR UPDATE SKIP LOCKED
}
```

### Pattern 8 — 실험 배정은 결정론 (읽을 때마다 뽑지 않는다)

**원문 근거 (N11, 2016)**:
> *"Each member belongs exclusively to one cell within a given experiment, with one of the cells always designated the 'default cell'."*
> 배정 방식 2종: *"Batch allocations give analysts the ultimate flexibility, allowing them to populate tests using custom queries"* / *"Real-Time allocations provide analysts with the ability to configure rules which are evaluated as the user interacts with Netflix"*
> 층화 표본: *"the populations in each cell should be as homogeneous as possible"*
> 인프라: Cassandra + EVCache(3시간 TTL), 배정 이벤트 → Kafka → Hive/Ignite, Spark Streaming → ElasticSearch → ABlaze 준실시간
> 성능: *"150K to 450K requests per second"*, 캐시 웜 시 *"< 1ms"*, 실시간 평가 *"around 50ms"*

**⚠️ 원문 조건 vs 우리 스케일**
- Cassandra/EVCache/Kafka/Spark/ElasticSearch **전부 불필요**. 45만 rps 가 아니라 수십 rps 다.
- **이식되는 불변식은 딱 하나**: *한 프로필은 한 실험에서 정확히 하나의 셀에 속하고, 그 배정은 변하지 않는다.* 이게 깨지면(요청마다 랜덤) 지표가 무의미해지고 UI 가 깜빡인다.
- 축소 구현: `cell = HASH(profileId + ":" + testKey) % cellCount` — **순수 함수, 저장 없이도 재현 가능**. 여기에 **최초 노출 시 1행 배정 로그**(profile_id, test_key, cell, assigned_at)를 남긴다. 로그는 배정 *결정*이 아니라 배정 *관측*이다.
- 층화 표본(N11)은 **채택하지 않는다** — 프로필 수십 개에서 층화는 통계적으로 무의미하고 복잡도만 늘린다. 정직하게 "미채택"으로 문서화.
- **순서 불변식**: 배정 로그 기록 → 효과 적용. 반대로 하면 효과만 있고 관측이 없는 구간이 생긴다.

---

## 4. Data Flow

### 4.1 Flow A — 홈(lolomo) 조립 · 읽기 경로 (가장 빈번)

```
[사용자: 홈 진입]
   │
   ▼ GET /api/home?profileId=P            (1 request)
bff.HomeController
   │
   ├─▶ experiment::spi.cellFor(P,"lolomo_order_v1") ──▶ [experiment_allocation]  (배정 로그)
   │
   ├─▶ recommendation.buildLolomoPlan(P)   ◀── @CircuitBreaker(name="lolomo")
   │      │
   │      ├─▶ playback::spi.inProgress(P)        ──▶ [playback_bookmark]   → 이어보기 행
   │      ├─▶ profile::spi.myListTitleIds(P)     ──▶ [profile_my_list]     → 마이리스트 행
   │      ├─▶ catalog::spi.findTopN(10)          ──▶ [title, view_stat]    → Top10 행 (윈도 함수)
   │      ├─▶ catalog::spi.findIdsByGenre(g)     ──▶ [title_genre]         → 장르 행 ×N
   │      ├─▶ playback::spi.recentWatches(P) + catalog::spi.findSimilar()  → "때문에 시청" 행
   │      │
   │      └─▶ PageAssembler: 정렬(cell 별) → 중복 제거 → MAX_ROWS 절단
   │              ⇩ 실패 시
   │          FallbackRows: Top10 + 인기 장르행 (catalog 만)      ◀── N3 "fast fallback"
   │
   ├─▶ catalog.getTitleCards(unique ids)   ──▶ [title] WHERE id IN (...)   ★ 1 쿼리
   ├─▶ playback.progressFor(P, ids)        ──▶ [playback_bookmark]         ★ 1 쿼리
   │
   ▼
LolomoResponse { rows: [ {type, title, items:[{titleId,name,boxart,progressPct}] } ] }
```

**쿼리 예산(설계 목표)**: 행 10개 × 타이틀 20개여도 **총 쿼리 ≈ 8~10회**(행 생성 6 + 하이드레이션 2). 카드당 쿼리는 금지. 이것이 유일하게 지켜야 할 성능 계약이다.

### 4.2 Flow B — 재생 · 이어보기 (Core Value 경로)

```
[사용자: 타이틀 클릭 → 재생]
   │
   ▼ POST /api/play/{titleId}
bff.PlayController ──▶ playback.startPlayback(P, T)
                          ├─ catalog::spi.isPlayable(T)          (발행됐나?)
                          ├─ profile::spi.isKids(P) + 성숙도      (차단?)
                          └─ bookmark.positionOf(P,T) → resumeSec
   ◀── PlaybackTicket { manifestUrl: "/media/{T}/{ver}/master.m3u8", resumeSec: 412 }
   │
   ▼ hls.js: loadSource(manifestUrl); currentTime = resumeSec
   │
   ├──────────────▶ nginx GET /media/.../master.m3u8       ★ Spring 미경유
   ├──────────────▶ nginx GET /media/.../720p/index.m3u8
   └──────────────▶ nginx GET /media/.../720p/seg_00042.ts  (대역폭에 따라 렌디션 전환)
   │
   ▼ 재생 중 10~30초마다
POST /api/playback/heartbeat {titleId, posSec}
   └─▶ playback: UPSERT playback_bookmark (profile_id, title_id) → position_sec, updated_at
```

**원문 근거 (N8, 2015)**:
> *"For each movie or TV episode that a member views, Netflix records how much was watched and where the viewer left off."*
> 규모: *"billions of viewing-related events per day"*, *"over two billion hours every month"*
> 구조: *"Data is partitioned into N stateful nodes by a simple mod N of the member's account id"*, *"Memcached is layered on top of Cassandra as a guaranteed low latency read path for materialized views."*
> 일관성: *"an inconsistent write may happen but will get corrected soon after due to a short cache entry TTL"* (최종 일관성 수용)

**⚠️ 원문 조건 vs 우리 스케일**
| N8 전제 | 우리 실제 | 결정 |
|---|---|---|
| 일 수십억 이벤트 | 일 수백 이벤트 | **stateful 샤딩·Memcached·Cassandra 전부 불필요.** MySQL 단일 테이블 UPSERT |
| 계정 id mod N 파티셔닝 | — | 미채택 |
| 최종 일관성 수용 | **동일 채택** | 하트비트는 last-write-wins. 재생 위치가 3초 틀려도 사용자는 모른다. 트랜잭션 강화 금지(과설계) |
| 이벤트 로그 + 배치 집계 | M1 은 **북마크 테이블이 곧 이력** | `playback_bookmark(profile_id,title_id,position_sec,completed,updated_at)` PK(profile_id,title_id). 별도 이벤트 로그는 후속. 정직 표기: **정밀 분석은 불가**(세션·중도이탈 재구성 불가) — 그 대가로 테이블 1개 |

**하트비트 주기 = 유일한 튜닝 노브**: 5초면 쓰기 6배, 30초면 최대 30초 손실. **권장 15초 + `visibilitychange`/`pause` 시 즉시 1회**. 이 "이탈 시 flush" 가 없으면 사용자가 창을 닫을 때마다 위치를 잃는다(체감상 가장 큰 결함).

### 4.3 Flow C — 인제스트 (관리자, 쓰기 경로)

```
[관리자: 원본 업로드]
   ▼ POST /admin/ingest  (multipart 또는 로컬 경로 등록)
ingest.IngestApi ──▶ INSERT ingest_job(status=PENDING, source_path)  ──▶ jobId 즉시 반환 (202)
                                                                          ★ 요청 스레드에서 ffmpeg 금지
   ┌──────────────────────────────────────────────────────────────┐
   │ @Scheduled(5s) TranscodeWorker  (동시 실행 ≤ 2)               │
   │   SELECT ... FOR UPDATE SKIP LOCKED  → PENDING 1건 → RUNNING  │
   │     step 1 INSPECT   ffprobe: 해상도/길이/코덱 → 잡 메타 기록 │  ← N10 의 VIS 축소
   │     step 2 ENCODE    ffmpeg → 1080p/720p/480p (렌디션 병렬)   │  ← N10 의 VES
   │     step 3 PACKAGE   HLS 세그먼트 + variant/master.m3u8       │
   │     step 4 VALIDATE  매니페스트 파싱 + 세그먼트 개수 검증     │  ← N10 의 VVS 축소
   │   → /media/{titleId}/{ver}/…  (새 버전 디렉터리에만 쓰기)     │
   │   → status=DONE + publishEvent(TitleAssetPublished)           │
   └───────────────────────────────┬──────────────────────────────┘
                                   │ Spring Modulith Event Publication Registry
                                   │ (원 트랜잭션에 발행 로그 기록 → 실패해도 재발행)
                                   ▼
              catalog @ApplicationModuleListener
                 → title.manifest_path = …/{ver}/master.m3u8
                 → title.status = PUBLISHED       ★ 이 순간부터 홈/검색에 노출
```

**중요 경계**: ingest 는 catalog 테이블을 **직접 쓰지 않는다**. "이 타이틀이 이제 볼 수 있다"를 결정하는 권한은 catalog 에 있다. ingest 는 "에셋이 준비됐다"는 사실만 통지한다. 이 구분이 없으면 두 모듈이 같은 테이블을 쓰는 은닉 결합이 생기고 Modulith 검증은 그걸 못 잡는다(패키지 의존만 보므로).

> ⚠️ **Modulith verify 의 한계 — 정직 표기**: `ApplicationModules.verify()` 는 **정적 패키지 의존만** 검증한다. *같은 DB 테이블을 두 모듈이 만지는 결합*, *리플렉션/이벤트 런타임 결합*은 잡지 못한다. 테이블 소유권은 **명명 규칙(`catalog_*`, `playback_*`, …) + 리뷰**로 지켜야 하며, 이는 자동 게이트 밖이다.

---

## 5. 축소 이식 정직 대조표 (원문 전제 vs 우리 스케일) — 총괄

| # | 브리프의 축소 이식 | 원문 | 원문이 전제한 조건 | 우리 조건 | 정직한 이식 판정 |
|---|---|---|---|---|---|
| 1 | Spring Modulith 6 모듈 | N5, N6 | 팀 다수·24/7 소유권 분리, 70 DGS·개발자 수백 | 개발자 1, 팀 0 | **경계만 이식, 물리 분리 미이식.** 정당 — 원문의 분리 동인이 조직이므로 |
| 2 | 단일 BFF, 서버 조립 | N4 | 기기 수백 종 × UI 팀 다수 → 기기별 어댑터 | 클라이언트 1종 | **조립 이식 ✅ / 클라이언트별 BFF 미이식.** BFF 는 정확히 1개 |
| 3 | Resilience4j 서킷 | N7 | **원격** 의존 30개, 일 10억 콜, 스레드풀 격벽 | 인-프로세스 호출, 의존 1개(MySQL) | **폴백 ✅ / 스레드 격리 ❌.** "Hystrix 이식"이라 쓰면 과장. "예외·지연 폴백 + 가시적 데모"가 정확 |
| 4 | nginx 정적 HLS | N14 | ISP 내장 어플라이언스, 60+ DC, 나이틀리 필 | 로컬 볼륨 1개 | **발행/서빙 분리 ✅ / CDN ❌.** "CDN 오리진 개념 증명"이 정직한 표현 |
| 5 | ffmpeg 3-렌디션 래더 | N9, N10 | 카탈로그 수만 편, per-title 최적화, 청크 병렬, 6 마이크로서비스 | 저작권-프리 5~10편, 단일 머신 | **고정 래더 + 단일 워커.** per-title·청크·VMAF 는 후속(측정 가능해질 때) |
| 6 | 실험 플래그 + 배정 로그 | N11 | 45만 rps, Cassandra/EVCache/Kafka/Spark/ES, 층화 표본 | 수십 rps | **결정론 배정 + 배정 로그 ✅ / 층화·스트리밍 파이프라인 ❌** |
| 7 | 카오스 = 컨테이너 kill | N12 | 프로덕션 인스턴스 무작위 종료, 엔지니어 대기 | CI 스테이지 | **의도 이식 ✅.** 원문 왈 *"By running Chaos Monkey in the middle of a business day, in a carefully monitored environment with engineers standing by…"* — 우리는 "관측되는 환경에서 의도적 실패"라는 조건을 CI 로 재현 |
| 8 | Actuator+Prometheus+Grafana | N13 | 초당 수십억 데이터포인트, 12억 시계열 | 시계열 수백 | **차원 모델 ✅ / 규모 ❌.** 원문 핵심(*"a metric's identity is an arbitrary unique set of key-value pairs"*)은 Micrometer 태그로 그대로 이식 |
| 9 | 개인화 행 | N1, N2, N3 | 후보 행 수만 개, 회원 5,700만, offline 사전계산 | 후보 행 ~20, 프로필 수십 | **online 계산만 ✅ / offline·nearline ❌.** 중복 제거·성숙도 필터는 100% 이식 |
| 10 | 검색 | — | — | — | **원형 인용 없음(§0.2).** Netflix 근거를 붙이지 말 것 |
| 11 | HLS 프로토콜 | (해당 원문 미확보) | Netflix 는 DASH 계열 사용(2차 자료, LOW) | HLS 채택 | **정직 표기: 프로토콜은 Netflix 를 따르지 않는다.** HLS 선택 근거는 hls.js·Safari 네이티브·ffmpeg 패키징 단순성이지 "Netflix 가 그래서"가 아니다 |

---

## 6. Scaling Considerations

| 규모 | 조정 |
|---|---|
| **~100 프로필 (실 대상)** | 현 설계 그대로. 단일 JVM + nginx + MySQL. 캐시 없음. 사전계산 없음 |
| **1k~10k** | ① lolomo 응답 캐시(프로필별 TTL 60s) ② catalog 읽기 캐시(Caffeine) ③ 하트비트 배칭 ④ nginx `expires` + `sendfile` 튜닝 |
| **100k+** | ① 미디어 → 실 CDN(오리진은 그대로) ② recommendation offline 사전계산(N3 의 offline/nearline 도입) ③ **playback 모듈만 물리 분리**(N5 의 discovery/playback 분리 근거) ④ 북마크 → 전용 스토어 |

### 6.1 무엇이 먼저 깨지는가 (순서대로)

1. **디스크 I/O·대역폭 (미디어)** — 가장 먼저. 1080p 세그먼트 동시 5스트림이면 로컬 디스크가 먼저 운다. *조치*: nginx `sendfile on` + OS 페이지 캐시, 렌디션 상한 720p 데모.
2. **lolomo N+1** — 하이드레이션을 배치로 안 하면 프로필 10개에서도 즉사. *조치*: 설계 단계에서 봉쇄(§4.1 쿼리 예산). **이건 스케일 문제가 아니라 설계 결함**이다.
3. **ffmpeg 가 CPU 를 독점** — 트랜스코딩 중 홈 응답 지연. *조치*: 동시 실행 상한 + `nice`.
4. **하트비트 쓰기** — 마지막. 15초 주기 × 동시 시청 100명 = 6.7 write/s. MySQL 이 하품한다.

**트립와이어(물리 분리 발동 조건)** — 브리프의 "트래픽이 요구할 때"를 술어로:
- (a) 배포 충돌: 추천 실험 배포가 재생 가용성에 영향을 준 사건 2회 이상, **또는**
- (b) 재생 경로 p99 가 홈 경로 부하와 상관관계 확인, **또는**
- (c) 팀이 2개 이상.
셋 중 하나도 참이 아니면 **분리하지 않는다.** (N5: *"there is no perfect solution. There is no right or wrong, only trade-offs."*)

---

## 7. Anti-Patterns

### AP1 — Spring 이 미디어 바이트를 서빙
**하는 짓**: `StreamingResponseBody` / `Resource` 로 `.ts` 세그먼트 반환 (인증 붙이려고).
**왜 틀렸나**: 세그먼트 요청은 재생 1건당 수백 회다. 서블릿 스레드가 즉시 고갈되고, 트랜스코딩·홈 응답까지 같이 죽는다. Open Connect(N14) 의 존재 이유 자체가 이 분리다.
**대신**: nginx 직접 서빙. 인증이 필요해지면 **X-Accel-Redirect**(Spring 이 인가만 하고 파일 전송은 nginx 에 위임) — M1 은 무인증으로 시작하고 이 경로를 후속 후보로 남긴다.

### AP2 — 클라이언트가 행을 조립
**하는 짓**: `/api/rows/top10`, `/api/rows/genre/28` … 를 프론트가 10번 호출.
**왜 틀렸나**: N4 — *"Reducing the number of network requests improves performance, in some cases by several seconds."* 게다가 **중복 제거를 클라이언트가 할 수 없다**(행 간 중복은 서버만 안다 — N1 의 dedup 제약).
**대신**: `/api/home` 1회.

### AP3 — 레이어-우선 패키징
**하는 짓**: `com.moabom.controller/`, `com.moabom.service/`, `com.moabom.repository/`.
**왜 틀렸나**: Modulith 가 이걸 **3개 모듈(controller/service/repository)** 로 인식한다. 도메인 경계 검증이 통째로 무의미해진다.
**대신**: 모듈-우선(§2). 레이어는 모듈 *안*에서.

### AP4 — `shared` 공유 커널 비대화
**하는 짓**: 두 모듈이 뭘 나눠 쓸 때마다 `shared` 로 올림 → `shared/util/`, `shared/dto/` 탄생.
**왜 틀렸나**: `shared` 는 OPEN 이라 검증을 안 받는다. 결국 모든 모듈이 `shared` 를 통해 결합하고, 경계는 서류상으로만 남는다.
**대신**: ID 값 타입·페이징만. 그 외에는 **복제를 허용**한다(두 모듈이 각자 자기 뷰를 갖는 게 정답인 경우가 대부분).

### AP5 — 실험 셀을 읽을 때마다 랜덤 결정
**왜 틀렸나**: N11 — *"Each member belongs exclusively to one cell within a given experiment."* 요청마다 뽑으면 같은 사용자가 새로고침할 때마다 UI 가 바뀌고 지표는 잡음이 된다.
**대신**: 결정론 해시 + 최초 노출 시 배정 로그 1행.

### AP6 — ffmpeg 를 요청 스레드에서 실행
**왜 틀렸나**: 15분 영상 3렌디션 = 수 분~수십 분. HTTP 타임아웃, 커넥션 점유, 재시도 시 중복 실행.
**대신**: 202 Accepted + 잡 ID + 폴링/상태 API.

### AP7 — 미디어를 MySQL BLOB 으로
**왜 틀렸나**: 세그먼트 수천 개 × 수 MB. 백업·복제·버퍼풀이 전부 붕괴한다.
**대신**: 파일시스템 + DB 에는 경로만.

### AP8 — 발행된 미디어 디렉터리를 덮어쓰기(재트랜스코딩 시)
**왜 틀렸나**: RFC 8216 — VOD 플레이리스트는 *"MUST NOT change"*. 재생 중인 클라이언트가 사라진 세그먼트를 요청한다.
**대신**: 새 `{assetVersion}` 디렉터리 → DB 포인터 교체 → (유예 후) 구 버전 삭제.

### AP9 — Netflix 규모 인프라를 그대로 복사
**하는 짓**: Kafka·Cassandra·ES·Spark 를 "Netflix 가 쓰니까" 도입.
**왜 틀렸나**: N11 의 그 스택은 *"150K to 450K requests per second"* 를 위한 것이다. 우리 부하에서 그것들은 **운영 부채이지 아키텍처가 아니다**. 포트폴리오 평가자는 "왜 안 썼는지 설명할 수 있는가"를 본다.
**대신**: 축소 판정을 §5 표로 문서화 — 이 표 자체가 포트폴리오 산출물이다.

---

## 8. Integration Points

### 8.1 External

| 대상 | 통합 방식 | 함정 |
|---|---|---|
| **nginx** | docker-compose 볼륨 공유(rw=app, ro=nginx) | Windows 볼륨 마운트 경로/권한. `D:/moabom/media` 를 컨테이너에 바인드. CORS 헤더 필요(FE 가 다른 포트) — 브라우저 E2E 로만 잡히는 결함 |
| **ffmpeg** | `ProcessBuilder` subprocess | 표준출력/에러 **반드시 소비**(안 하면 파이프 버퍼 차서 프로세스 정지). 타임아웃 필수. Windows 경로 인코딩 |
| **MySQL 8.0** | JPA + `FOR UPDATE SKIP LOCKED` 는 네이티브 쿼리 | 잡 클레임 쿼리는 JPQL 로 안 됨 → `@Query(nativeQuery=true)` |
| **Prometheus** | Actuator `/actuator/prometheus` | 기본 미노출 → `management.endpoints.web.exposure.include` 설정 필요 |
| **hls.js** | FE. Safari 는 네이티브 HLS | `master.m3u8` 의 `BANDWIDTH` 가 ABR 입력 (RFC 8216: MUST) |

### 8.2 Internal Boundaries (전체 의존 그래프)

| 경계 | 통신 | 근거·주의 |
|---|---|---|
| bff → catalog / playback / profile / recommendation / experiment (`::api`) | 직접 호출 | bff 는 sink. **역방향 절대 금지** |
| recommendation → catalog `::spi` | 직접 | 장르·Top-N·유사 타이틀 ID 조회 |
| recommendation → playback `::spi` | 직접 | 이어보기·최근 시청 |
| recommendation → profile `::spi` | 직접 | My List·키즈 플래그 |
| recommendation → experiment `::spi` | 직접 | 행 정렬 변형 |
| playback → catalog `::spi` | 직접 | 발행 여부·매니페스트 경로 |
| playback → profile `::spi` | 직접 | 키즈 차단 판정 |
| catalog ← ingest `event` | **이벤트** (`@ApplicationModuleListener`) | 유일한 비동기 경계. 발행 로그로 내구성 |
| 그 외 전부 | **금지** | `catalog`, `profile`, `experiment`, `ingest` 는 다른 도메인 모듈에 의존하지 않는다 |

**순환 검사**: `bff → {5}` , `recommendation → {catalog, playback, profile, experiment}` , `playback → {catalog, profile}` , `ingest → ∅` , `catalog/profile/experiment → ∅`. **DAG 확인 ✅** (recommendation 이 playback 위, playback 이 catalog 위, catalog 는 최하층).

```java
// catalog/package-info.java — 최하층: 아무에게도 의존하지 않음
@ApplicationModule(allowedDependencies = {})
package com.moabom.catalog;

// playback/package-info.java
@ApplicationModule(allowedDependencies = { "catalog :: spi", "profile :: spi" })
package com.moabom.playback;

// recommendation/package-info.java
@ApplicationModule(allowedDependencies = {
    "catalog :: spi", "playback :: spi", "profile :: spi", "experiment :: spi" })
package com.moabom.recommendation;
```
(문법 근거 — Modulith 공식: *"A module can opt into declaring its allowed dependencies by using the `@ApplicationModule` annotation on the package"*, `::` 는 named interface 지정, `:: *` 는 전체 named interface 허용.)

---

## 9. Suggested Build Order (로드맵 입력)

의존성이 강제하는 순서 + **위험 우선(riskiest-first)** 원칙.

| 순서 | 단계 | 선행 이유 | 완료 판정 |
|---|---|---|---|
| **P0** | 뼈대 + `ModularityTests` + docker-compose(mysql·nginx) + Actuator/Prometheus | **`ApplicationModules.verify()` 는 모듈보다 먼저 존재해야 한다.** 나중에 붙이면 이미 쌓인 위반을 예외처리하며 시작하게 되고, 그 순간 경계 검증은 장식이 된다 | 빈 모듈 8개 + verify 통과 + `/actuator/prometheus` 200 |
| **P0.5** | **미디어 스파이크 (Java 0줄)** — 손으로 ffmpeg 돌려 Sintel → 3렌디션 HLS, nginx 서빙, 정적 HTML + hls.js 재생 | **최고 위험을 맨 앞에.** 렌디션 전환·GOP 정렬·CORS·Windows 경로는 전부 여기서 터진다. 그리고 **인제스트 *모듈*은 P7 이지만 미디어 *산출물*은 P2 부터 필요하다** — 이 둘을 분리하지 않으면 재생 개발이 인제스트 완성까지 인질이 된다 | 브라우저에서 이어보기 없이 재생 + 네트워크 탭에서 렌디션 전환 관측. **ffmpeg 명령을 스크립트로 커밋**(P7 이 이걸 Java 로 감쌈) |
| **P1** | catalog (title·genre·asset 포인터·시드) | 모든 것이 titleId 위에 선다. 최하층 모듈 | 시드 타이틀 조회 API + Top-N 윈도 함수 쿼리 |
| **P2** | profile (가구 계정·N 프로필·키즈 플래그) | 모든 개인화가 profileId 로 키잉된다. playback 정책의 선행 | 프로필 전환 후 컨텍스트 유지 |
| **P3** | playback (재생 티켓 + 북마크 + 하트비트) | catalog(발행 여부) + profile(키즈) 필요. P0.5 의 실 에셋 사용 | **★ Core Value 폐곡선**: 목록 → 재생 → 이탈 → 재진입 시 이어보기 |
| **P4** | **BFF v1 — 비개인화 lolomo** (Top10 + 장르행) + FE 홈 | **추천보다 먼저.** 이유: 이것이 곧 **P5 서킷브레이커의 폴백 구현체**다. 순서를 뒤집으면 폴백이 사후 스텁이 되어 데모가 가짜가 된다 | `/api/home` 1회 호출로 행 렌더 + 쿼리 예산 §4.1 준수 실측 |
| **P5** | recommendation (RowProvider + 개인화 정렬 + 중복 제거) + **Resilience4j 서킷 + 폴백** | playback·profile·catalog spi 전부 필요. 폴백 대상(P4) 존재 | 추천 강제 실패 시 홈이 P4 응답으로 계속 동작 + Grafana 에서 서킷 OPEN 관측 |
| **P6** | 검색 + My List | 둘 다 작고, My List 는 **RowProvider 1개 추가**로 끝난다 → OCP 주장의 실증 | 마이리스트 담기 → 홈에 행 등장(기존 파일 수정 0) |
| **P7** | ingest 모듈 (P0.5 스크립트를 잡 큐 + 이벤트로 승격) | 여기서야 catalog 발행 이벤트·SKIP LOCKED·Event Publication Registry 가 의미를 갖는다. 앞에 두면 재생 개발이 막힌다 | 업로드 → 수 분 뒤 홈에 신규 타이틀 자동 등장 |
| **P8** | experiment (결정론 배정 + 로그 + 행 정렬 변형) | **변형시킬 대상(P5 의 행 정렬)이 존재해야 한다.** 실험을 먼저 만들면 실험할 게 없다 | 같은 프로필 = 항상 같은 셀 (재현 테스트) + 배정 로그 적재 |
| **P9** | 관측 대시보드 + 카오스 스테이지 | 앞의 전부가 계측 대상 | 컨테이너 kill 후 회복 + Grafana 패널(트랜스코딩 시간·행 조립 지연·서킷 상태) |

### 9.1 순서가 강제되는 지점 (로드맵이 바꾸면 안 되는 것)

1. **P0 의 verify 테스트 → 모든 모듈**. 사후 도입은 무의미.
2. **P0.5 의 미디어 산출물 → P3 재생**. 그러나 **P7 인제스트 모듈 ↛ P3**. (산출물 의존 ≠ 모듈 의존)
3. **P4 폴백 → P5 서킷브레이커**. 역순이면 폴백이 가짜.
4. **P5 행 정렬 → P8 실험**. 역순이면 실험 대상 부재.
5. **P1 catalog → P2/P3/P5 전부**. 최하층.

### 9.2 로드맵용 리서치 플래그

| 단계 | 추가 리서치 필요? | 이유 |
|---|---|---|
| P0.5 | **필요 (HIGH)** | ffmpeg HLS 옵션 공식 문서 **미확보**(§0.2). 렌디션 GOP 정렬·`var_stream_map`·세그먼트 길이는 실측 스파이크 + 공식 문서 재확보 필요 |
| P6 (검색) | **필요 (MEDIUM)** | 한국어 형태소 [열림]. Netflix 원형 인용 없음(§0.2). MySQL FULLTEXT ngram vs ES 판단 필요 |
| P8 (실험) | 필요 (LOW) | 최소 통계 처리(전환율 비교) 범위 결정 |
| P1~P5 | 불필요 | 표준 Spring/JPA 패턴. 본 문서로 충분 |
| P7 | 부분 | 잡 상태 머신·재시도 정책은 표준. ffmpeg 실패 분류만 실측 |

---

## 10. Open Questions / Gaps

1. **인증·세션**: 가구 계정 로그인 방식(세션 vs JWT)이 어느 모듈 소유인지 미결. 제안: `profile` 이 아니라 **인프라 계층(Spring Security)** 이 소유하고, profile 은 도메인만. 로드맵 P2 에서 결정.
2. **키즈 성숙도 데이터 출처**: `catalog` 가 등급 필드를 갖는가, 별도 테이블인가. M1 은 boolean `kids_safe` 로 축소 제안(브리프상 등급 엔진은 후속).
3. **BFF 응답 캐시 위치**: 모듈 내부 vs nginx. 규모상 M1 은 캐시 없음 권장 — 성급한 최적화.
4. **미디어 인증**: M1 무인증. X-Accel-Redirect 승격 시점 미정.
5. **`shared` 상한 규칙 집행**: 리뷰 의존(자동 게이트 없음) — 정직 표기.
6. **Modulith 버전 핀**: Spring Boot 3.2 ↔ Modulith 1.1.x 계열 (1.2 도 3.2 호환 진술 확인, 출처=spring.io 블로그 2024-05-24, MEDIUM). STACK.md 에서 정확히 핀할 것.

---

## Sources

**Netflix TechBlog 원문 (전부 WebFetch 실확보, §0.1 표 참조)** — N1~N13 + Open Connect(N14).
**1차 규격·공식 문서**: [RFC 8216 HLS](https://www.rfc-editor.org/rfc/rfc8216.html) (HIGH) · [Spring Modulith fundamentals](https://docs.spring.io/spring-modulith/reference/fundamentals.html) (HIGH) · [Spring Modulith events](https://docs.spring.io/spring-modulith/reference/events.html) (HIGH) · [MySQL 8.0 Locking Reads](https://dev.mysql.com/doc/refman/8.0/en/innodb-locking-reads.html) (HIGH) · Resilience4j Context7 `/resilience4j/resilience4j` (HIGH).
**미확보(§0.2)**: Apple HLS Authoring Spec · ffmpeg 공식 HLS muxer 문서 · Netflix 회원 검색 원문 · Netflix My List 원문 · Netflix DASH 1차 출처 · ChAP 원문.

**신뢰도 총평**: 모듈 경계·데이터 흐름·빌드 순서 = **HIGH**(공식 문서 + 원문 근거). 미디어 파이프라인 세부(ffmpeg 옵션·래더 수치) = **MEDIUM**(원문 일부 미확보, P0.5 실측 필요). 검색 설계 = **LOW**(원형 없음, 별도 리서치).
**알려진 결함/잔여 리스크**: 인용 추출기 왜곡 가능성(§0.3, 미대조 13건) · Modulith verify 가 DB 테이블 결합을 못 잡음(§4.3) · `shared` 비대화 방지책이 리뷰 의존 · P0.5 스파이크 전까지 미디어 경로 리스크 미해소.

---
*Architecture research for: VOD 스트리밍 (Netflix TechBlog 패턴 축소 이식)*
*Researched: 2026-08-06*
