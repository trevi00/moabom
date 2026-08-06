# Phase 1: 기반 + 가드레일 - Research

**Researched:** 2026-08-06
**Domain:** Spring Modulith 8모듈 골격 + docker-compose 전 스택 기동 + Flyway 스키마 단일 진실 + git 미디어 격리 (그린필드 스캐폴딩)
**Confidence:** HIGH (버전 핀·모듈 규약·compose 문법 = 공식 문서/레지스트리 실측) / MEDIUM (bootBuildImage↔compose 한-커맨드 배선, Next rewrites 런타임 해석)

> **본 문서의 위치**: ROADMAP은 이 phase를 "Research: skip — 공식 문서 HIGH confidence로 충분"으로 표기했다.
> 따라서 본 문서는 **새 외부 조사가 아니라** `.planning/research/{STACK,PITFALLS,ARCHITECTURE,SUMMARY}.md`를
> **Phase 1 실행 가능 단위로 합성**한 것이다. 신규 조사는 기존 리서치가 명시적으로 [열림]/[미검증]으로 남긴
> 좁은 구멍(§9 LOW 7종 중 이미지 태그, artifact 좌표, CI 액션 버전, 스캐폴딩 경로)에만 수행했다.
> **모든 버전 핀의 정본은 여전히 STACK.md다** — 본 문서는 그 핀을 레지스트리에 대조해 확인/정정한 결과를 병기한다.

---

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**스코프 게이트**
- **D-00:** 대용량 트래픽 요소(물리 MSA·DB이중화·Redis)는 **M1 후반 증설 — "진화 서사"** 로 반영한다. Phase 1~8은 그대로 유지(Modulith 모듈리스로 완성)하고, 뒤에 Phase 9~ 를 증설: 부하 주입 → 병목 실측 → Redis 캐시/세션 → DB 읽기 복제본 → 모듈 1개 물리 분리. 서킷브레이커는 기존 Phase 6 유지. **Phase 1은 이 증설을 선반영하지 않는다** (compose에 Redis/replica 없음) — Modulith 경계 검증 자체가 분리-대비 준비물이다. 로드맵 반영은 본 워크플로 종료 후 `/kha-add-phase`.

**기동 토폴로지**
- **D-01:** **프로필 이원화.** 평시 개발 = 인프라만 compose(`spring-boot-docker-compose`가 bootRun 시 자동 기동) + 앱은 bootRun. 데모/검수 = `docker compose --profile full up` 으로 앱 컨테이너(`bootBuildImage` 산출 이미지)까지 한 방 기동. **성공 기준 1("한 커맨드로 앱까지")은 full 프로필로 충족한다** — plan-phase는 SC1 검증을 full 프로필 기준으로 작성할 것.
- **D-02:** **단일 진입 오리진 규약을 P1에서 못박는다.** 브라우저는 `:3000`(Next)만 본다 — `/api/*`→앱`:8080`, `/media/*`→nginx rewrites. STACK.md §2.6의 "hls.js만 CORS로 깨지는 비대칭 버그" 실패 조건 원천 차단. 문서만이 아니라 **실동작으로 검증** (D-05의 FE 스텁이 `/api` 경유 헬스 응답을 실제로 받아야 함).
- **D-03:** **기동·리셋 UX는 docker compose 표준 커맨드 그대로.** 래퍼 스크립트/Make/Gradle 태스크 없음. README에 `up` / `down` / `down -v`(스키마 리셋) 3줄.

**레포 구조·FE 시점**
- **D-04:** **backend/ + frontend/ 모노레포.** 루트는 compose·docs(ADR)·미디어 규약·README 소유. backend/ = Gradle 프로젝트(com.moabom), frontend/ = Next.js. 후반 증설 phase의 물리 분리 시 services/ 확장을 염두에 둔 배치.
- **D-05:** **Next.js 골격을 P1에 얇게 포함.** Next 16 스캐폴딩(App Router, STACK.md 핀 준수) + rewrites 프록시 설정 + 헬스 확인 스텁 1페이지. 그 이상의 UI는 만들지 않는다 (행 UI는 P5 목업-우선 규율 대상).
- **D-06:** **full 프로필에 FE 컨테이너 포함.** 데모 모드에선 Next도 컨테이너로 떠서 "한 커맨드로 전체 서비스" 서사 완결. 평시 개발은 `npm run dev`.

**Git·CI 운영**
- **D-07:** **GitHub public repo 생성 (계정 trevi00).** 포트폴리오 목적 — 미디어 격리·대용량 차단 가드레일이 첫 커밋부터 강제되므로 처음부터 공개.
- **D-08:** **main 직접 push (solo 모드).** 현 로컬 `master`→`main` 전환. Conventional Commits. PR-only 아님 — GSD의 잦은 planning/실행 커밋과 마찰 없는 흐름. CI는 push 트리거.
- **D-09:** **P1 CI는 최소 코어 게이트:** build + `ApplicationModules.verify()` + 단위 테스트 + 미디어/대용량 파일 커밋 차단 체크 + resilience4j `dependencyInsight` 회귀 게이트(2.3.0 유입 차단). 러너는 ubuntu-latest (Testcontainers 요구 — STACK.md: Windows 러너 불가). 22-스테이지 전면 채택은 하지 않고 이후 phase마다 증설 (E2E는 P2부터).
- **D-10:** **로컬 pre-commit 훅 추가 — 미디어 차단만.** 미디어 확장자/파일 크기 차단을 로컬 훅으로 (빠름, push 전 차단 — 일단 push되면 이력 세탁 비용 큼). `verify()`는 CI에만 (로컬 훅에 빌드 태우지 않음).

**스키마·시드**
- **D-11:** **P1 Flyway는 title 최소 골격만 소유.** title(+장르 최소 관계) + `attribution`/`license` 컬럼. 나머지 도메인 테이블은 각 소유 phase가 V 마이그레이션 증분 추가 — 미확정 설계(에셋 매니페스트 형식 등)를 선행 추측하지 않는다.
- **D-12:** **시드 = 한글 골든 ≥5건 + Blender 실자산 메타 행.** 한글 골든("인터스텔라" 등)은 검색 함정(PITFALLS Pitfall 10) 검증용 메타데이터-only — 재생 에셋 없음. Blender 오픈무비 행은 CC-BY attribution 포함, 에셋 경로는 P2 배치 스크립트가 채움.
- **D-13:** **시드는 본선 V 마이그레이션에 포함.** 프로필 분기 없음 — 모든 환경이 데모 성격인 포트폴리오라 "시드 없는 환경"이 존재하지 않는다. 성공 기준 3 문구("Flyway만 돌려도 재현") 그대로.
- **D-14:** **Modulith `event_publication` 테이블은 P1에서 만들지 않는다.** "이벤트 테이블도 Flyway로 관리, `schema-initialization.enabled` 자동 생성 금지" **규율만** P1 ADR/문서에 못박고, 실제 DDL은 `@ApplicationModuleListener` 첫 사용 phase(P3 발행 상태머신 예상)가 추가.

### Claude's Discretion

- compose 파일 구성(단일 파일 + profiles vs override 파일), 헬스체크/`depends_on: service_healthy` 배선 상세
- ADR 문서 형식·위치(루트 docs/ 하위 권장), README 구성
- pre-commit 훅 구현 방식(플레인 git hook vs 도구), 차단 확장자/크기 임계값
- CI workflow yml 구조, 캐싱 전략
- Next.js 스캐폴딩 세부(TS 설정 등 — 버전 핀은 STACK.md 준수), FE Dockerfile 작성
- 8모듈 빈 골격의 패키지 내부 구조(ARCHITECTURE.md 규약 준수 하에), `allowedDependencies` 선언 방식
- title 스키마 컬럼 상세(제목 원제/한국어 이원화 등 — P3 요구 미선점 범위 내)

### Deferred Ideas (OUT OF SCOPE)

- **M1 후반 증설 phase (9~)**: 부하 주입 → 병목 실측 → Redis 캐시/세션 → DB 읽기 복제본 → 모듈 1개 물리 분리 — **본 워크플로 종료 후 `/kha-add-phase`로 로드맵 반영 필요** (2026-08-06 사용자 확정, "진화 서사" 모양). 각 증설 phase의 스코프 상세는 그때 논의.
- 서킷브레이커는 증설 아님 — 기존 Phase 6 (ARCH-02) 그대로.
</user_constraints>

---

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| **ARCH-01** | Spring Modulith 6모듈 경계 (catalog/playback/profile/recommendation/ingest/experiment) + `ApplicationModules.verify()` CI 게이트 | §Architecture Patterns P-2 (8모듈 패키지 규약 — ARCHITECTURE.md §2 정본), P-3 (`allowedDependencies` + `@NamedInterface` 선언 순서), §Pitfalls CP-6/CP-7 (starter-jpa 함정, named interface 미존재 시 verify 실패), §Code Examples C-3/C-4, §Validation V-SC2 (verify + `Type.OPEN` grep + 반사실 프로브) |
| **ARCH-05** | docker-compose 한 번으로 전 스택 기동 (mysql·nginx·prometheus·grafana) | §Architecture Patterns P-4 (profiles 이원화 토폴로지), P-5 (단일 진입 오리진 배선), §Standard Stack 이미지 태그 실측 핀, §Pitfalls CP-3 (bootBuildImage↔compose 한-커맨드 간극), CP-8 (prometheus 스크레이프 타깃 이중 모드), §Code Examples C-1/C-2, §Validation V-SC1 |

> ROADMAP은 이 외에 **비-요구사항 가드레일 산출물 4종**을 "누락 금지"로 명시했다. 본 문서는 이를 G1~G4로 추적한다:
> **G1** ADR-0001(Boot 3.2.12 EOL 근거 + 라인 최종 패치 고정 + non-blocking CVE 스캔) · **G2** resilience4j 2.2.0 핀 + 옆 주석 + `dependencyInsight` 회귀 게이트 · **G3** mysql 기동 파라미터(`--ngram_token_size=2 --collation-server=utf8mb4_0900_ai_ci`) · **G4** Windows 파일명 ASCII-safe 정책 + UTF-8 명시.
</phase_requirements>

---

## Summary

Phase 1은 코드를 거의 쓰지 않고 **되돌리기 비싼 결정 6개를 첫 커밋에 못박는** phase다. PITFALLS.md가 이 phase(그들의 "P0")에 배치한 pitfall 5종(3·5·10·14·17)의 공통 성질은 **회복 비용의 구조적 비대칭**이다 — 미디어가 git에 들어가면 `filter-repo` + force push(HIGH), Modulith 위반이 20건 쌓이면 엔티티 재배치 며칠(MEDIUM→HIGH), `ngram_token_size`를 나중에 바꾸면 FULLTEXT 인덱스 전량 재구축(LOW→MEDIUM). 반면 지금 넣는 비용은 전부 파일 몇 개다. `[CITED: .planning/research/PITFALLS.md §Recovery Strategies]`

기존 리서치는 이 phase에 필요한 것의 **약 90%를 이미 확정**해 두었다 — 버전 핀(STACK §1, §7), 모듈 패키지 규약과 `allowedDependencies` 그래프(ARCHITECTURE §2, §8.2), compose mysql 파라미터(PITFALLS Pitfall 10), Flyway 단일 진실(Pitfall 14), 라이선스 컬럼(Pitfall 17). 본 조사가 실제로 채운 것은 나머지 10%이며, 그중 **계획을 바꿔야 하는 발견이 7건**이다:

1. **PITFALLS Pitfall 3의 `.gitignore` 권고를 문자 그대로 쓰면 D-05가 깨진다** — `*.ts`는 MPEG-TS 세그먼트이자 **TypeScript**다. `frontend/`가 같은 저장소에 사는 D-04/D-05 하에서 전역 `*.ts` 무시는 Next.js 소스를 통째로 배제한다. 경로-스코프 패턴 + 크기 게이트로 대체해야 한다.
2. **start.spring.io가 Boot 3.x를 더 이상 제공하지 않는다** (실측: 선택 가능 버전 4.0.7 이상만). 스캐폴딩은 Initializr가 아니라 **STACK.md §3의 build.gradle을 손으로 놓고 `gradle wrapper --gradle-version 8.14.5`** 로 간다.
3. **`spring-modulith-starter-jpa`는 `event_publication` JPA 엔티티를 끌고 온다** — D-14("P1에서 테이블 만들지 않음")와 정면 충돌 가능. P1은 `spring-modulith-starter-core`만 쓰고 starter-jpa는 P3로 미룬다.
4. **`spring-boot-starter-security`를 P1에 넣으면 기본 HTTP Basic이 켜져 `/api/health`가 401** — D-02의 "실동작 검증"이 통과하지 못한다. 보안 스타터는 소비자(AUTH-01/02)가 생기는 Phase 3으로 미룬다.
5. **`docker compose --profile full up` 한 커맨드로는 앱 이미지가 만들어지지 않는다** — compose는 Gradle을 실행하지 못한다. D-01(bootBuildImage) + D-03(래퍼 금지) + SC1(한 커맨드)이 문자 그대로 동시에 성립하지 않으므로 planner가 명시적으로 선택해야 한다(§CP-3에 3안 제시, 권장안 있음).
6. **`core.autocrlf=true`(이 머신 실측) + Windows → ubuntu-latest CI** 조합은 `gradlew`의 개행/실행권한으로 CI를 죽이는 고전 실패다. `.gitattributes` + `git update-index --chmod=+x gradlew`가 첫 커밋에 있어야 한다.
7. **Dependabot(G1의 "CVE 스캔")은 켜는 순간 G2의 핀과 싸운다** — Boot 4.x / resilience4j 2.3.0 bump PR을 연다. `ignore:` 규칙 없이는 가드레일이 소음 발생기가 된다.

**Primary recommendation:** 이 phase를 **"골격 → 가드레일 → 실동작 증명"** 3단으로 나누고, 각 단계의 마지막에 **반사실 프로브**(위반을 일부러 만들어 게이트가 실제로 붉어지는지 확인 후 되돌림)를 둔다. 하네스 선례(`reference_gate_value_by_outcome_not_labels`, `feedback_prose_gate_counterfactual_verification`)가 반복해서 말하는 것 — 게이트는 "존재한다"가 아니라 "위반을 실제로 막았다"로만 가치가 증명된다. SC2("경계를 위반한 커밋은 CI에서 실패한다")와 SC4("미디어가 히스토리에 들어가지 못한다")는 **문장 자체가 반사실을 요구**한다.

---

## Standard Stack

### 정본 선언

**모든 버전 핀의 단일 진실은 `.planning/research/STACK.md`다.** 아래 표는 그 핀을 2026-08-06 레지스트리에 대조한 **검증 결과**이며, 불일치가 발견된 항목만 "정정" 열에 표기했다. STACK.md와 충돌하는 값을 여기서 새로 만들지 않았다.

### Core — Phase 1이 실제로 쓰는 것

| Technology | STACK.md 핀 | 레지스트리 실측 (2026-08-06) | P1 사용 | 근거 |
|---|---|---|---|---|
| JDK | Temurin 17 LTS | 로컬 `C:/Program Files/Java/jdk-17` 존재 | ✅ toolchain 17 | `[VERIFIED: 로컬 파일시스템]` |
| Spring Boot | **3.2.12** | 3.2.x 최고 패치 = **3.2.12** 확인 | ✅ | `[VERIFIED: search.maven.org spring-boot-starter-parent g:a:v]` |
| Spring Modulith | **1.1.12** | 1.1.x 최고 패치 = **1.1.12** 확인 (1.1.13 없음; 1.2.13/1.3.7/1.4.1 라인은 Boot 3.3+) | ✅ `starter-core` + `starter-test` | `[VERIFIED: search.maven.org spring-modulith-bom]` |
| Gradle (wrapper) | 8.14+ (8.x 라인) | 8.x 라인 최종 = **8.14.5** (현행 최신은 9.6.1 = 미지원) | ✅ **8.14.5로 핀** | `[VERIFIED: services.gradle.org/versions/all]` |
| Flyway | 9.22.3 (BOM) + `flyway-mysql` | `flyway-mysql:9.22.3` POM HTTP 200 | ✅ 둘 다 필수 | `[VERIFIED: repo1.maven.org]` |
| MySQL Connector/J | `com.mysql:mysql-connector-j` 8.3.0 (BOM) | BOM 관리 — 직접 핀 금지 | ✅ `runtimeOnly` | `[CITED: STACK.md §1]` |
| resilience4j-spring-boot3 | **2.2.0** (2.3.0 금지) | Central에 2.2.0·2.3.0 모두 존재 → **bump가 물리적으로 가능** = 게이트 필요성 실증 | ✅ **G2 게이트 대상으로만** | `[VERIFIED: search.maven.org]` |
| Micrometer Prometheus | BOM 1.12.13 | BOM 관리 | ✅ `runtimeOnly` | `[CITED: STACK.md §1]` |
| spring-boot-docker-compose | BOM, `developmentOnly` | Boot 3.2 구성명은 `developmentOnly` | ✅ D-01 dev 경로 | `[CITED: STACK.md §1]` |

### Docker 이미지 — STACK.md §9 LOW 5번("이미지 태그 미검증") 해소

| 서비스 | 권장 태그 | Docker Hub 실측 | 비고 |
|---|---|---|---|
| mysql | `mysql:8.0` | HTTP 200, last_updated **2026-05-05** | EOL(2026-04-30) 직후가 마지막 갱신 — **이 태그는 이제 동결됐다**. pull은 계속 가능. `8.0.44`도 존재(2026-01-17) |
| nginx | `nginx:stable-alpine` | HTTP 200, 2026-07-18 | STACK 권고 그대로 유효 (`stable` = 1.30.4) |
| prometheus | `prom/prometheus:v3.13.2` | HTTP 200, 2026-07-30 | STACK 미핀 항목 — **v3.13.2로 핀 권장** (`latest`/`v3` 롤링 금지) |
| grafana | `grafana/grafana:13.1.2` | HTTP 200, 2026-08-04 | STACK 미핀 항목 — **13.1.2로 핀 권장** |
| Node (FE 컨테이너) | `node:24-alpine` | HTTP 200, 2026-08-03 | STACK Node 24 LTS 핀과 정합 |
| Paketo builder | `paketobuildpacks/builder-jammy-base:latest` | HTTP 200, **2026-08-04** (활발히 갱신 중) | Boot 3.2.12 `bootBuildImage` 기본값. 아래 CP-3 참조 |

`[VERIFIED: hub.docker.com/v2/repositories/*/tags/* — 8개 태그 전부 HTTP 200 + last_updated 실측]`

### Frontend — 레지스트리 실측 정정

| Technology | STACK.md 핀 | npm 실측 (2026-08-06) | P1 권장 |
|---|---|---|---|
| Next.js | 16.2.x | `latest` = **16.3.0**, 16.2 라인 최종 = **16.2.12** | **16.2.12** (STACK의 16.2.x 핀 존중) |
| React / React DOM | 19.2.x | `latest` = **19.2.8** | 19.2.8 (next@16.2.0 peer: `^19.0.0` ✅) |
| Node engines | Node 24 권장 | next@16.2.0 `engines.node: >=20.9.0` | 로컬 22.17.1도 **충족** (§Environment) |
| hls.js | 1.6.16 | `latest` = **1.6.17** (1패치 드리프트) | **P1 불필요** — D-05는 헬스 스텁뿐. P2 착수 시 재확인 |

`[VERIFIED: registry.npmjs.org — next/react/hls.js dist-tags 및 next@16.2.0 engines·peerDependencies]`

### GitHub Actions — D-09 CI 배선용 (STACK.md 미커버 영역)

| Action | 최신 릴리스 | 발행일 |
|---|---|---|
| `actions/checkout` | **v7.0.1** | 2026-07-20 |
| `actions/setup-java` | **v5.7.0** | 2026-07-31 |
| `actions/setup-node` | **v7.0.0** | 2026-07-14 |
| `gradle/actions/setup-gradle` | **v6.3.0** | 2026-08-02 |

`[VERIFIED: gh api repos/{owner}/{repo}/releases/latest — 인증된 GitHub API 직접 조회]`

> 메이저 핀 권장(`@v7` / `@v5` / `@v6`). SHA 핀은 이 프로젝트 위협모델(공개 포트폴리오, 시크릿 없음)에 과하다.

### P1에서 **의도적으로 제외**하는 의존성 (STACK.md §3 build.gradle 대비 델타)

STACK.md §3의 `build.gradle` 발췌는 **프로젝트 전체(M1 종료 시점) 기준**이다. P1이 그것을 통째로 복사하면 두 개의 구체적 사고가 난다:

| 제외 대상 | 왜 P1에서 빼는가 | 언제 넣는가 |
|---|---|---|
| `spring-modulith-starter-jpa` | `JpaEventPublication` **@Entity**를 클래스패스에 올린다. `ddl-auto=validate`(Flyway 단일 진실의 표준 짝) 하에서 `event_publication` 테이블 부재 → **기동 실패**. D-14가 "P1에서 테이블 만들지 않음"이므로 스타터도 같이 미뤄야 정합한다 | **P3** (`@ApplicationModuleListener` 첫 사용 + 해당 V 마이그레이션과 동시) |
| `spring-boot-starter-security` | 컨텍스트에 올라오는 순간 기본 `SecurityFilterChain`이 **전 경로 HTTP Basic 401**. D-02의 "FE 스텁이 `/api` 경유 헬스를 실제로 렌더"가 통과 못 한다. permitAll 임시 체인을 P1에 넣으면 P3에서 지워야 할 죽은 설정이 된다 | **P3** (AUTH-01/02) |
| `spring-session-jdbc` | 소비자 없음. `SPRING_SESSION` 테이블 DDL을 P1 Flyway에 선점하게 만든다(D-11 위반) | **P3** |
| `springdoc-openapi-starter-webmvc-ui` | 문서화할 API가 헬스 스텁 1개뿐 | **P3~P5** (첫 실 API) |
| `flyway`/`jpa` 자체 | ❌ 제외 아님 — SC3가 요구 | P1 필수 |
| `resilience4j-spring-boot3:2.2.0` | ❌ 제외 아님 — **G2 회귀 게이트의 피검사체가 필요**하다. 미사용이어도 클래스패스에 있어야 `dependencyInsight`가 의미를 갖는다 | P1 필수 (`spring-boot-starter-aop` 동반) |

**Installation (P1 실제 의존성):**

```groovy
// backend/build.gradle
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
  implementation 'org.springframework.boot:spring-boot-starter-actuator'
  implementation 'org.springframework.boot:spring-boot-starter-aop'

  implementation 'org.springframework.modulith:spring-modulith-starter-core'   // starter-jpa 는 P3
  runtimeOnly    'org.springframework.modulith:spring-modulith-actuator'

  // 복원력 — Boot BOM 미관리. 2.3.0 은 Boot3+Java17 에서 기동 실패(issue #2265) → 절대 bump 금지.
  // 회귀 게이트: .github/workflows/ci.yml 의 "resilience4j pin" 스텝 참조.
  implementation 'io.github.resilience4j:resilience4j-spring-boot3:2.2.0'

  runtimeOnly 'io.micrometer:micrometer-registry-prometheus'
  runtimeOnly 'com.mysql:mysql-connector-j'
  implementation 'org.flywaydb:flyway-core'
  implementation 'org.flywaydb:flyway-mysql'      // MySQL 8 은 이 모듈 없으면 Unsupported Database

  compileOnly 'org.projectlombok:lombok'
  annotationProcessor 'org.projectlombok:lombok'
  developmentOnly 'org.springframework.boot:spring-boot-docker-compose'

  testImplementation 'org.springframework.boot:spring-boot-starter-test'
  testImplementation 'org.springframework.modulith:spring-modulith-starter-test'
  testImplementation 'org.springframework.boot:spring-boot-testcontainers'
  testImplementation 'org.testcontainers:mysql'
}

dependencyManagement {
  imports { mavenBom "org.springframework.modulith:spring-modulith-bom:${springModulithVersion}" }
}

// G2 집행: 게이트는 CI 가시성, strictly 는 실제 강제
configurations.all {
  resolutionStrategy {
    force 'io.github.resilience4j:resilience4j-spring-boot3:2.2.0'
  }
}

tasks.withType(JavaCompile).configureEach { options.encoding = 'UTF-8' }   // G4
tasks.withType(Test).configureEach {
  useJUnitPlatform()
  systemProperty 'file.encoding', 'UTF-8'
  systemProperty 'sun.jnu.encoding', 'UTF-8'
}
jacoco { toolVersion = '0.8.12' }
```

`[CITED: .planning/research/STACK.md §3 build.gradle — 위는 그 발췌에서 P1 미사용 항목을 제거하고 G2/G4 집행을 추가한 것]`

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|---|---|---|
| 손으로 놓은 `build.gradle` | start.spring.io 생성 | **불가.** Initializr가 Boot 3.x를 더 이상 제공하지 않는다 (선택지 = 4.0.7 이상만) `[VERIFIED: start.spring.io/metadata/client]` |
| `gradle wrapper --gradle-version 8.14.5` | 시스템 gradle(9.4.1) 직접 사용 | Boot 3.2 플러그인이 Gradle 9.x를 지원 목록에 두지 않음. **wrapper만 사용**하고 bare `gradle` 호출 금지 |
| 단일 `compose.yaml` + `profiles` | `compose.override.yaml` 분리 | profiles가 D-01 이원화에 1:1 대응하고 D-03("표준 커맨드 그대로")과 마찰이 없다. override 파일은 `-f` 인자를 강요해 D-03 위반 |
| 플레인 git hook + `core.hooksPath` | pre-commit(python) / husky(node) | 훅 하나를 위해 런타임 의존성을 추가할 이유 없음. `core.hooksPath`는 훅을 **버전관리 가능**하게 만드는 표준 수단 |
| Dependabot (G1 CVE 스캔) | Trivy/OWASP dependency-check job | Dependabot은 CI 상태에 영향 없음 = **태생적으로 non-blocking**. 단 `ignore:` 필수(CP-9) |

---

## Architecture Patterns

### P-1. 저장소 레이아웃 (D-04 구체화)

```
D:/moabom/                          # repo root — compose·docs·규약 소유
├── compose.yaml                    # 단일 파일 + profiles (D-01/D-03)
├── .env.example                    # MEDIA_ROOT 등 (실 .env 는 gitignore)
├── .gitignore                      # ★ 첫 커밋 (SC4)
├── .gitattributes                  # ★ 첫 커밋 (CP-5 — eol=lf)
├── .editorconfig                   # charset=utf-8 (G4)
├── README.md                       # up / down / down -v 3줄 + 상표 고지(Pitfall 17)
├── .githooks/pre-commit            # D-10 미디어 차단
├── docs/adr/
│   ├── 0001-runtime-versions.md    # G1
│   └── 0002-media-and-encoding.md  # G4 (+ D-14 규율 기록)
├── infra/
│   ├── nginx/default.conf
│   └── prometheus/prometheus.yml
├── .github/
│   ├── workflows/ci.yml            # D-09
│   └── dependabot.yml              # G1 (CP-9 ignore 규칙 포함)
├── backend/                        # Gradle 프로젝트
│   ├── gradlew · gradlew.bat · gradle/wrapper/  (8.14.5)
│   ├── build.gradle · settings.gradle · gradle.properties
│   └── src/main/java/com/moabom/…  (P-2)
│       resources/db/migration/V1__…sql · V2__…sql
└── frontend/                       # Next.js 16.2.12
    ├── next.config.ts              # rewrites (D-02)
    ├── Dockerfile                  # D-06
    └── app/page.tsx                # 헬스 스텁 (D-05)

D:/moabom-media/                    # ★ 저장소 밖 — 현재 미존재, P1이 생성
```

**미디어 루트가 저장소 밖이라는 사실이 SC4의 실제 방어선이다** — `.gitignore`는 2차 방어. `[CITED: PITFALLS.md Pitfall 3 "물리적으로 git이 볼 수 없으면 사고가 불가능하다"]`

### P-2. Modulith 8모듈 빈 골격 (ARCHITECTURE.md §2 정본)

Modulith의 모듈 판정 규칙은 **패키지 구조 그 자체**다 — 공식 문서: *"By default, each direct sub-package of the main package is considered an application module package."* 따라서 **레이어-우선 패키징(`controller/`·`service/`·`repository/` 최상위)을 쓰면 모듈이 1개로 붕괴한다.** `[CITED: docs.spring.io/spring-modulith fundamentals, ARCHITECTURE.md §2 인용]`

```
com.moabom/
├── MoabomApplication.java
├── bff/          HealthController.java (D-05 소비 대상), package-info.java
├── catalog/      package-info.java, spi/package-info.java + spi/TitleQueryPort.java
├── playback/     package-info.java, spi/package-info.java + spi/WatchHistoryPort.java
├── profile/      package-info.java, spi/package-info.java + spi/ProfilePort.java
├── recommendation/ package-info.java
├── experiment/   package-info.java, spi/package-info.java + spi/AssignmentPort.java
├── ingest/       package-info.java, event/package-info.java
└── shared/       package-info.java (@ApplicationModule(type = OPEN)), TitleId.java, ProfileId.java
```

**"빈 골격"의 정확한 의미**: 구현 클래스는 0개지만 **`package-info.java`와 SPI 포트 인터페이스는 존재해야 한다**. 이유는 P-3.

### P-3. `allowedDependencies` ↔ `@NamedInterface` 선언 순서 (SC2의 실패 지점)

의존 그래프는 ARCHITECTURE.md §8.2가 DAG로 확정했다: `bff → {5개 도메인}`, `recommendation → {catalog, playback, profile, experiment}::spi`, `playback → {catalog, profile}::spi`, `catalog/profile/experiment/ingest → ∅`.

**함정**: `allowedDependencies = "catalog :: spi"` 는 **catalog 쪽에 `@NamedInterface("spi")`가 이미 선언돼 있어야** 해석된다. 그리고 `@NamedInterface`의 비직관적 규칙 — *"어떤 패키지에 `@NamedInterface("spi")`를 선언하면 그 패키지는 unnamed named interface에서 제거되고 오직 `"order :: spi"` 형태로만 접근 가능해진다"* — 즉 **노출을 늘리려고 붙였다가 기존 접근이 깨지는 방향으로 작동**한다. `[CITED: PITFALLS.md Pitfall 5 §3 (Modulith 공식 문서 기반, [H])]`

따라서 P1의 선언 순서는:
1. 먼저 각 `spi/package-info.java`에 `@NamedInterface("spi")` + **포트 인터페이스 1개**(마커 겸 P3~P6의 실 계약 자리)
2. 그다음 소비자 모듈의 `@ApplicationModule(allowedDependencies = {...})`
3. `verify()` 실행

`allowedDependencies`를 **선언하지 않은 모듈은 제약이 없다** — 다른 모듈의 루트 패키지 + 모든 named interface에 접근 가능. **경계는 명시적으로 선언해야만 존재한다.** 8모듈 전부에 선언을 채워라. `[CITED: PITFALLS.md Pitfall 5 §4 [H]]`

### P-4. compose 토폴로지 — profiles 이원화 (D-01/D-06)

Compose profiles 규칙 (공식 문서 실측):
- *"Services without a `profiles` attribute are always enabled."* → mysql·nginx·prometheus·grafana는 프로필 없음 = 항상 기동
- *"When a service with assigned profiles is explicitly targeted on the command line its profiles are started automatically"*, 그리고 대상 서비스의 `depends_on` 의존성도 함께 기동
- ⚠️ 역방향 금지: **프로필 없는 서비스가 프로필 있는 서비스를 `depends_on` 하면 compose 검증 오류** — 우리 배치(app/web만 `profiles: [full]`, 인프라는 무프로필)는 이 규칙을 위반하지 않는다

`[VERIFIED: docs.docker.com/compose/how-tos/profiles/]`

| 모드 | 커맨드 | 뜨는 것 | 앱 |
|---|---|---|---|
| 평시 개발 (D-01) | `./gradlew bootRun` (spring-boot-docker-compose가 `docker compose up` 자동 실행) | mysql·nginx·prometheus·grafana | JVM 로컬 |
| 데모/검수 (SC1) | `docker compose --profile full up` | 위 4종 + **app + web** | 컨테이너 |
| 리셋 (D-03) | `docker compose down -v` | — | — |

**`spring-boot-docker-compose` 배선 (D-04 모노레포 필수 설정):**
```yaml
# backend/src/main/resources/application.yaml
spring:
  docker:
    compose:
      file: "../compose.yaml"          # backend/ 가 작업 디렉터리 → 루트 compose 를 가리킨다
      lifecycle-management: start-only # bootRun 종료 시 인프라를 내리지 않음(개발 루프 속도)
      # profiles.active 미지정 = 무프로필 서비스만 = 인프라만 (D-01 정확히 일치)
```
`spring.docker.compose.file`은 *"point to a compose file located outside the application root"* 용도로 공식 지원되며 상대경로 예시(`../my-compose.yml`)가 문서에 있다. `spring.docker.compose.profiles.active`도 존재하지만 **P1에서는 지정하지 않는 것이 정답** — 지정하면 dev에서도 app 컨테이너가 떠 bootRun과 8080 포트를 다툰다. `[CITED: docs.spring.io spring-boot dev-services.adoc via Context7]`

> ⚠️ Context7이 반환한 `testAndDevelopmentOnly` 구성은 **Boot 3.4+ 문법**(main 브랜치 문서)이다. **Boot 3.2에서는 `developmentOnly`만 존재**한다. 이런 버전 드리프트가 이 phase에서 가장 흔한 오염원이다.

### P-5. 단일 진입 오리진 배선 (D-02)

브라우저는 `:3000`만 본다. `next.config.ts`의 `rewrites`가 `/api/*`와 `/media/*`를 서버 사이드로 프록시한다 → 브라우저 입장에서 **동일 오리진**이 되어 CORS·SameSite 문제군이 구조적으로 사라진다. `[CITED: STACK.md §2.6]`

**dev와 full에서 목적지가 다르다** — 하드코딩하면 한쪽이 깨진다:

| 모드 | `BACKEND_ORIGIN` | `MEDIA_ORIGIN` |
|---|---|---|
| dev (`npm run dev`) | `http://localhost:8080` | `http://localhost:8081` (compose가 nginx:80 → 8081 publish) |
| full (컨테이너) | `http://app:8080` | `http://nginx:80` |

→ 환경변수로 외부화하고 `.env.example`·compose `environment:`에 둘 다 명시. `next.config.ts`의 `rewrites()`는 **서버 기동 시** 평가되므로 `next start` 런타임 env가 반영된다 `[ASSUMED — Next 16에서 실측 필요, §Assumptions A3]`.

**D-02의 "실동작 검증"은 이 phase의 가장 값싼 고신호 테스트다**: `curl -f http://localhost:3000/api/health` 가 200 + JSON을 돌려주면 (a) Next가 떴고 (b) rewrites가 배선됐고 (c) 앱이 떴고 (d) 네트워크가 이어졌음이 한 번에 증명된다. FE 스텁 페이지는 이 응답을 렌더한다.

### P-6. Flyway 단일 진실 (D-11/D-12/D-13, SC3)

**`docker-entrypoint-initdb.d`를 쓰지 않는다.** MySQL 이미지는 `/var/lib/mysql`에 데이터가 있으면 init을 통째로 건너뛴다 — 스키마를 고치고 `down && up` 해도 아무 일도 안 일어난다. 스키마·시드의 단일 진실은 **Flyway**, `initdb.d`는 (필요하면) DB/계정 생성까지만. `[CITED: PITFALLS.md Pitfall 14 [H]]`

마이그레이션 배치 (D-11/D-13):
- `V1__init_catalog.sql` — `title` (+장르 최소 관계), `attribution`/`license` **NOT NULL**
- `V2__seed_golden.sql` — 한글 골든 ≥5건(메타-only) + Blender 실자산 메타 행(CC-BY). **본선 V 마이그레이션** — 프로필 분기 없음

`attribution`/`license`를 **처음부터** 넣는 이유: 나중에 추가하면 모든 시드를 다시 만든다. `[CITED: PITFALLS.md Pitfall 17]`

### Anti-Patterns to Avoid

- **레이어-우선 패키징** (`com.moabom.controller/…`): 8모듈이 1모듈로 붕괴 → `verify()`가 장식이 된다
- **`@ApplicationModule(type = OPEN)`으로 위반 회피**: 사이클 탐지에서 아예 제외되고 경계가 소멸. Modulith 공식 문서 스스로 *"완전 모듈화된 앱에서 open module 사용은 부실한 모듈화의 징후"*라고 명시. `shared` 외 사용 금지 — 팀 규약상 **하네스 mutation 토큰급 결정**으로 취급 `[CITED: PITFALLS.md Pitfall 5]`
- **`verify()`를 "모듈이 좀 생긴 뒤에" 켜기**: 위반 1건에서 고치는 비용과 40건에서 고치는 비용은 다른 종이다
- **`.gitignore`에 전역 `*.ts`**: TypeScript 소스를 배제한다 (CP-1)
- **compose에 `mysql` `command:` 누락**: `ngram_token_size`는 read-only 변수라 런타임 `SET GLOBAL` 불가. 나중에 바꾸면 FULLTEXT 인덱스 전량 재구축 `[CITED: PITFALLS.md Pitfall 10 [H]]`
- **`latest` 태그 이미지**: 재현 불가능한 기동. 위 실측 태그로 핀
- **`bootRun`을 bare `gradle`로**: 시스템 gradle 9.4.1은 Boot 3.2 미지원. `./gradlew`만

---

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---|---|---|---|
| 모듈 경계 검증 | ArchUnit 규칙 직접 작성 | `spring-modulith-starter-test` + `ApplicationModules.of(App.class).verify()` | ArchUnit은 Modulith가 전이 제공. 사이클·named interface·allowedDependencies 해석을 직접 짜면 규칙이 아니라 버그를 만든다 `[CITED: STACK.md §2.9]` |
| 스키마 초기화·시드 | `initdb.d` SQL / `ddl-auto=update` | Flyway `V__` 마이그레이션 | initdb는 1회성(Pitfall 14), `ddl-auto=update`는 단일 진실을 두 개로 만든다 |
| MySQL 8 방언 처리 | Flyway 설정 튜닝 | `flyway-core` + **`flyway-mysql`** | 없으면 `Unsupported Database: MySQL 8.0` `[CITED: STACK.md §5]` |
| DataSource URL 조립 (dev) | `application.yaml`에 수기 JDBC URL | `spring-boot-docker-compose` ConnectionDetails 자동 주입 | compose 포트가 바뀌어도 앱 설정이 따라간다 |
| 통합 테스트용 DB | 로컬 MySQL 설치 전제 | Testcontainers + `@ServiceConnection` | CI(ubuntu-latest)에서 그대로 재현. Windows 러너는 불가 `[CITED: STACK.md §2.9]` |
| 앱 컨테이너 이미지 | 손으로 쓴 Dockerfile(BE) | `./gradlew bootBuildImage` (Paketo) | 레이어 분할·JVM 튜닝·헬스 기본값을 공짜로. 단 CP-3의 배선 문제 있음 |
| 대용량 blob 탐지 | `find`/`du` 스크립트 | `git rev-list --objects --all \| git cat-file --batch-check` | 워킹트리가 아니라 **히스토리 전체**를 본다 — SC4가 요구하는 것이 정확히 이것 |
| git 훅 배포 | 훅을 각자 복사 | `.githooks/` + `git config core.hooksPath .githooks` | 훅이 버전관리 대상이 된다 (단 `core.hooksPath` 설정 자체는 클론마다 1회 필요 — README 명시) |

**Key insight:** 이 phase에서 손으로 짜야 하는 것은 **가드레일의 판정 로직**(미디어 확장자 목록, 크기 임계값)뿐이고, 그것마저 git 내장 명령의 조합으로 끝난다. 나머지는 전부 이미 존재하는 도구의 **배선**이다 — 그래서 이 phase의 실패는 "못 만들어서"가 아니라 "잘못 배선해서" 발생한다.

---

## Common Pitfalls

### CP-1. `.gitignore`의 `*.ts`가 TypeScript를 삼킨다 ★ 신규 발견

**What goes wrong:** PITFALLS.md Pitfall 3은 `.gitignore`에 `media/`, **`*.ts`**, `*.m4s`, `*.mp4` … 를 권고한다. 그 문서가 쓰일 당시엔 FE 시점이 미정이었다. **D-04/D-05로 `frontend/`가 같은 저장소에 들어온 지금, 전역 `*.ts`는 Next.js 소스 전체를 무시한다** — `next.config.ts`, 모든 `.ts` 모듈. `git status`가 조용해서 몇 시간 뒤에나 발견된다.

**Why it happens:** `.ts` = MPEG-TS 세그먼트 = TypeScript. 확장자 충돌이 문헌에 잘 안 나온다.

**How to avoid:** 확장자 블랙리스트에서 `.ts`를 빼고 **경로 스코프 + 크기 게이트**로 대체:
```gitignore
# 미디어 격리 (PITFALLS Pitfall 3) — 단, 전역 *.ts 금지: TypeScript 와 충돌한다
media/
transcode-tmp/
uploads/
*.mp4
*.mkv
*.mov
*.avi
*.webm
*.m4s
# MPEG-TS 세그먼트는 경로로만 배제 (frontend/**/*.ts 는 TypeScript 소스다)
media/**/*.ts
**/hls/**/*.ts
```
pre-commit 훅·CI 체크의 확장자 정규식에도 동일 원칙 적용. 크기 임계값(5MB)이 `.ts` 없이도 세그먼트를 잡는 2차 방어다.

**Warning signs:** `git status`에 `frontend/` 변경이 안 보인다 / `git check-ignore -v frontend/next.config.ts` 가 매칭된다.

### CP-2. start.spring.io가 Boot 3.x를 더 이상 주지 않는다 ★ 신규 발견

**What goes wrong:** "Initializr로 뼈대 뽑자"가 막힌다. 2026-08-06 실측 시 선택 가능 `bootVersion`은 **4.0.7 / 4.0.8-SNAPSHOT / 4.1.0 / 4.1.1-SNAPSHOT** 뿐이고 기본값은 4.1.0. **3.x가 목록에 없다.** `[VERIFIED: start.spring.io/metadata/client]`

**How to avoid:** 4.x로 생성 후 다운그레이드하지 말 것(Java 25 baseline·구조 상이). **STACK.md §3의 `build.gradle`을 손으로 놓고** 래퍼만 생성:
```bash
cd backend && gradle wrapper --gradle-version 8.14.5
```
(시스템 gradle 9.4.1이 8.14.5 래퍼를 생성하는 것은 정상 — 래퍼 생성은 프로퍼티 파일 작성일 뿐이다.)

### CP-3. `docker compose --profile full up` 하나로는 앱 이미지가 생기지 않는다 ★ 결정 필요

**What goes wrong:** D-01은 앱 컨테이너를 **`bootBuildImage` 산출 이미지**로 정했다. 그런데 compose는 Gradle을 실행하지 못한다. `image: moabom-app:local`만 선언하면 이미지가 로컬에 없을 때 compose는 레지스트리 pull을 시도하고 실패한다. 즉 **D-01(bootBuildImage) + D-03(래퍼 스크립트 금지) + SC1(한 커맨드)이 문자 그대로 동시에 성립하지 않는다.**

**Why it happens:** `bootBuildImage`는 Docker 데몬에 직접 이미지를 만든다 — *"The `bootBuildImage` task requires access to a Docker daemon."* compose의 `build:`는 Dockerfile만 안다. `[CITED: docs.spring.io/spring-boot/docs/3.2.12/gradle-plugin/reference/htmlsingle/]`

**planner가 골라야 할 3안:**

| 안 | 배선 | SC1 문구와의 관계 | 대가 |
|---|---|---|---|
| **A (권장)** | `app: {image: moabom-app:local, pull_policy: never}` + README에 **선행 1회** `cd backend && ./gradlew bootBuildImage --imageName=moabom-app:local` | SC1 = "**이미지 빌드 후** `docker compose --profile full up` 한 커맨드" — 검증 문구를 정직하게 2단계로 | 데모 전 빌드 1회. `pull_policy: never`가 "이미지 없음"을 즉시 명확히 실패시킨다(pull 시도 후 인증 오류로 헷갈리지 않음) |
| B | `app: {build: {context: ./backend, dockerfile: Dockerfile}}` (멀티스테이지 Gradle) | `docker compose --profile full up --build` 진짜 한 커맨드 | **D-01의 bootBuildImage 결정을 뒤집는다** — 사용자 확인 필요. Dockerfile 유지보수 추가 |
| C | A + `bootBuildImage`를 CI가 수행해 GHCR에 push, compose는 `ghcr.io/trevi00/moabom-app:latest` | 진짜 한 커맨드(pull) | P1 CI 스코프(D-09 최소 코어) 확대. 첫 실행에 네트워크 필요 |

**권장 = A.** 근거: D-01·D-03을 둘 다 지키면서 SC1 검증 문구만 정직해진다(`./gradlew bootBuildImage`는 "기동·리셋 UX 래퍼"가 아니라 **빌드 스텝**이므로 D-03 위반이 아니다). B는 locked decision 변경이라 사용자 재확인 없이는 채택 불가.

**추가 리스크(MEDIUM):** Boot 3.2.12의 기본 빌더는 `paketobuildpacks/builder-jammy-base:latest`이며, 2025~2026년에 `:latest` 태그로 빌드가 실패한다는 커뮤니티 보고가 있다(spring-boot issue #45335 등). 다만 해당 이미지는 **2026-08-04에도 갱신되고 있어**(Docker Hub 실측) "죽은 태그"는 아니다. 완화: `bootBuildImage { builder = 'paketobuildpacks/builder-jammy-base:<특정버전>' }` 로 핀할 수 있게 **첫 성공 빌드의 빌더 다이제스트를 ADR-0001에 기록**할 것. `[VERIFIED: hub.docker.com 태그 존재/갱신일]` `[CITED: WebSearch — spring-boot#45335 (MEDIUM, 단일 이슈 기반)]`

### CP-4. `spring-modulith-starter-jpa` ↔ D-14 충돌 ★ 신규 발견

**What goes wrong:** starter-jpa는 이벤트 발행 레지스트리의 JPA 구현(`JpaEventPublication` @Entity)을 클래스패스에 올린다. Flyway를 단일 진실로 쓰면 보통 `spring.jpa.hibernate.ddl-auto=validate`를 켜는데, D-14에 따라 `event_publication` 테이블이 **없으므로 스키마 검증 실패로 기동이 죽는다**.

**How to avoid:** P1은 `spring-modulith-starter-core`만. starter-jpa는 `@ApplicationModuleListener` 첫 사용 phase(P3 예상)에서 **해당 V 마이그레이션과 같은 커밋**에 추가. 이 규율을 ADR-0002(또는 0001)에 기록한다 — D-14가 요구한 "규율만 문서화"의 실체가 바로 이것이다.

추가로 `spring.modulith.events.jdbc.schema-initialization.enabled`는 **명시적으로 `false`**(기본값이지만 명시)로 두고 옆에 근거 주석. `[ASSUMED: ddl-auto=validate 하에서의 정확한 실패 모드 — 실측으로 확인할 것, §Assumptions A1]`

### CP-5. `core.autocrlf=true` + Windows → ubuntu-latest CI 사망 ★ 신규 발견

**What goes wrong:** 이 머신은 `core.autocrlf=true`(local·global 둘 다 실측). Windows에서 만든 `gradlew`(sh 스크립트)가 CRLF로 커밋되면 ubuntu 러너에서 `/usr/bin/env: 'sh\r': No such file or directory`로 죽는다. 더 흔한 것은 **실행 권한**: Windows git은 exec 비트를 설정하지 않아 CI의 `./gradlew build`가 `Permission denied`로 실패한다.

**How to avoid (첫 커밋에):**
```gitattributes
* text=auto eol=lf
*.bat  text eol=crlf
gradlew.bat text eol=crlf
gradlew text eol=lf
*.sh   text eol=lf
*.mp4 binary
*.m4s binary
```
```bash
git update-index --chmod=+x gradlew
git update-index --chmod=+x .githooks/pre-commit
```
`[VERIFIED: git config --get core.autocrlf → true (local & global)]`

### CP-6. `allowedDependencies`가 존재하지 않는 named interface를 가리킨다

**What goes wrong:** `playback`의 `allowedDependencies = "catalog :: spi"`인데 `catalog/spi/package-info.java`에 `@NamedInterface("spi")`가 없으면 `verify()`가 참조 해석 단계에서 실패한다. 빈 골격이라 SPI 패키지를 안 만들었을 때 정확히 발생한다.

**How to avoid:** P-3의 선언 순서 준수 — **SPI 패키지와 포트 인터페이스를 먼저** 만든다. 포트는 구현 없는 `interface` 1개면 충분하고, 그것이 곧 P3~P6의 실 계약 자리다.

### CP-7. `spring-boot-starter-security`가 D-02 검증을 401로 막는다 ★ 신규 발견

**What goes wrong:** 스타터를 넣는 순간 기본 체인이 전 경로에 HTTP Basic을 건다. `curl http://localhost:3000/api/health` → 401. "rewrites가 깨졌나?"로 몇 시간 태우기 좋다.

**How to avoid:** P1 의존성에서 제외(§Standard Stack 델타 표). 만약 어떤 이유로 포함해야 한다면 `permitAll` 체인을 **명시적으로 작성하고 P3에서 삭제할 것**을 TODO로 남긴다.

### CP-8. Prometheus 스크레이프 타깃이 dev/full에서 다르다 ★ 신규 발견

**What goes wrong:** dev에서는 앱이 **호스트 JVM**(bootRun)이고 full에서는 **컨테이너**(`app:8080`)다. `prometheus.yml`에 `app:8080`만 쓰면 dev에서 영구 DOWN, `localhost:8080`만 쓰면 컨테이너 안에서 자기 자신을 가리켜 둘 다 DOWN.

**How to avoid:** Docker Desktop의 host-gateway를 단일 타깃으로:
```yaml
# infra/prometheus/prometheus.yml
scrape_configs:
  - job_name: moabom-app
    metrics_path: /actuator/prometheus
    static_configs: [{ targets: ['host.docker.internal:8080'] }]
```
```yaml
# compose.yaml
prometheus:
  extra_hosts: ["host.docker.internal:host-gateway"]   # Linux 호환 (Docker Desktop 에선 불필요하나 무해)
app:
  profiles: [full]
  ports: ["8080:8080"]        # full 에서도 호스트에 노출 → 같은 타깃이 양쪽에서 유효
```
그리고 Actuator 노출을 명시:
```yaml
management:
  endpoints.web.exposure.include: health,info,prometheus
  endpoint.health.probes.enabled: true
```
Prometheus 엔드포인트는 **기본 미노출**이다. `[CITED: ARCHITECTURE.md §8.1]` `[ASSUMED: host-gateway 단일 타깃 전략의 양쪽 동작 — 실측 필요, §Assumptions A2]`

### CP-9. Dependabot(G1)이 G2의 핀과 싸운다 ★ 신규 발견

**What goes wrong:** G1은 "non-blocking CVE 스캔"을 요구한다. Dependabot을 켜면 즉시 `spring-boot 3.2.12 → 4.1.x`, `resilience4j 2.2.0 → 2.3.0` bump PR을 연다. 2.3.0은 **기동 실패**가 확정된 버전이다(STACK §2.1). 가드레일이 가드레일을 공격하는 상태가 되고, 알림 피로로 결국 전체를 끈다 — 그러면 CVE 가시성이 0이 된다.

**How to avoid:**
```yaml
# .github/dependabot.yml
version: 2
updates:
  - package-ecosystem: gradle
    directory: /backend
    schedule: { interval: weekly }
    open-pull-requests-limit: 5
    ignore:
      # ADR-0001: Boot 3.2 라인 고정(EOL 인지·수용). 라인 이동은 Modulith 동반 이동을 요구한다.
      - dependency-name: "org.springframework.boot*"
        update-types: ["version-update:semver-major", "version-update:semver-minor"]
      - dependency-name: "org.springframework.modulith*"
        update-types: ["version-update:semver-major", "version-update:semver-minor"]
      # STACK §2.1: 2.3.0 은 Boot3+Java17 에서 기동 실패(issue #2265). 전 업데이트 차단.
      - dependency-name: "io.github.resilience4j:*"
  - package-ecosystem: npm
    directory: /frontend
    schedule: { interval: weekly }
  - package-ecosystem: github-actions
    directory: /
    schedule: { interval: monthly }
```
**이 파일 자체가 G1의 "EOL 근거"를 코드로 표현한 것**이므로 ADR-0001에서 링크할 것.

### CP-10. Testcontainers가 compose와 다른 MySQL 파라미터로 뜬다

**What goes wrong:** SC3 검증 테스트가 기본 파라미터 MySQL 컨테이너를 띄우면 `ngram_token_size`·collation이 compose와 달라진다. P1에서는 조용하다가 **P7 검색 phase에서 "테스트는 통과하는데 실 DB에서 안 나온다"** 로 터진다.

**How to avoid:** 테스트 컨테이너에 compose와 **동일한 `command` 파라미터**를 주고, 그 사실을 테스트가 스스로 단언하게 한다:
```java
@Container @ServiceConnection
static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
    .withCommand("--ngram_token_size=2",
                 "--character-set-server=utf8mb4",
                 "--collation-server=utf8mb4_0900_ai_ci");
```
그리고 `SHOW VARIABLES LIKE 'ngram_token_size'` = 2 를 단언 → **G3가 "compose 파일에 문자열이 있다"가 아니라 "파라미터가 실제로 먹었다"로 검증된다.**

### CP-11. nginx + Windows 바인드 마운트의 `sendfile` 손상

**What goes wrong:** `D:/moabom-media`를 컨테이너에 바인드 마운트해 nginx로 서빙하면, 가상화 파일시스템에서 `sendfile`이 오작동해 **잘린/낡은 파일**을 내보내는 사례가 보고돼 있다. "가끔 세그먼트가 깨진다"로 나타나 트랜스코딩 버그로 오진하기 딱 좋다. `[CITED: PITFALLS.md Pitfall 4 Windows/Docker 추가 함정 [M]]`

**How to avoid:** P1의 nginx conf에 `sendfile off;`. P2에서 실 세그먼트가 생기기 전에 넣어두면 비용 0. 캐시 정책 분리(m3u8 max-age=10 / 세그먼트 immutable)도 지금 같이 넣어두는 것이 싸다(검증은 P2 소관).

---

## Code Examples

### C-1. `compose.yaml` — profiles 이원화 (D-01/D-03/D-06, ARCH-05)

```yaml
name: moabom

services:
  mysql:
    image: mysql:8.0
    # G3 — ngram_token_size 는 read-only 변수: 기동 인자로만 설정 가능 (PITFALLS Pitfall 10)
    command:
      - --ngram_token_size=2
      - --character-set-server=utf8mb4
      - --collation-server=utf8mb4_0900_ai_ci
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD:-moabom-local}
      MYSQL_DATABASE: moabom
    ports: ["3306:3306"]
    volumes: ["mysql-data:/var/lib/mysql"]
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "127.0.0.1", "--silent"]
      interval: 5s
      timeout: 5s
      retries: 30
      start_period: 30s

  nginx:
    image: nginx:stable-alpine
    ports: ["8081:80"]
    volumes:
      - ./infra/nginx/default.conf:/etc/nginx/conf.d/default.conf:ro
      - ${MEDIA_ROOT:-D:/moabom-media}:/srv/media:ro     # 저장소 밖 미디어 루트 (SC4)

  prometheus:
    image: prom/prometheus:v3.13.2
    ports: ["9090:9090"]
    volumes: ["./infra/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro"]
    extra_hosts: ["host.docker.internal:host-gateway"]   # CP-8

  grafana:
    image: grafana/grafana:13.1.2
    ports: ["3001:3000"]
    environment:
      GF_SECURITY_ADMIN_PASSWORD: ${GRAFANA_PASSWORD:-admin}
    depends_on: [prometheus]

  # --- profile: full (데모/검수) ---
  app:
    profiles: ["full"]
    image: moabom-app:local        # 선행: cd backend && ./gradlew bootBuildImage --imageName=moabom-app:local
    pull_policy: never             # CP-3 — 이미지 부재를 즉시·명확히 실패시킨다
    depends_on:
      mysql: { condition: service_healthy }
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/moabom?characterEncoding=UTF-8&serverTimezone=Asia/Seoul
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_ROOT_PASSWORD:-moabom-local}
      JAVA_TOOL_OPTIONS: "-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"   # G4
    ports: ["8080:8080"]

  web:
    profiles: ["full"]
    build: { context: ./frontend }
    depends_on: [app]
    environment:
      BACKEND_ORIGIN: http://app:8080     # D-02
      MEDIA_ORIGIN:   http://nginx:80
    ports: ["3000:3000"]

volumes:
  mysql-data:
```

> `--profile full` 미지정 시 `app`/`web`은 뜨지 않는다 — *"Services without a `profiles` attribute are always enabled."* `[VERIFIED: docs.docker.com/compose/how-tos/profiles/]`

### C-2. `next.config.ts` — 단일 진입 오리진 (D-02/D-05)

```ts
import type { NextConfig } from 'next';

const BACKEND = process.env.BACKEND_ORIGIN ?? 'http://localhost:8080';
const MEDIA   = process.env.MEDIA_ORIGIN   ?? 'http://localhost:8081';

const nextConfig: NextConfig = {
  output: 'standalone',                 // FE 컨테이너 이미지 축소 (D-06)
  async rewrites() {
    return [
      { source: '/api/:path*',   destination: `${BACKEND}/api/:path*` },
      { source: '/media/:path*', destination: `${MEDIA}/media/:path*` },
    ];
  },
};
export default nextConfig;
```

### C-3. Modulith 모듈 선언 (ARCH-01)

```java
// com/moabom/catalog/package-info.java  — 최하층: 아무에게도 의존하지 않는다
@org.springframework.modulith.ApplicationModule(allowedDependencies = {})
package com.moabom.catalog;

// com/moabom/catalog/spi/package-info.java  — 다른 도메인 모듈용 좁은 포트
@org.springframework.modulith.NamedInterface("spi")
package com.moabom.catalog.spi;

// com/moabom/playback/package-info.java
@org.springframework.modulith.ApplicationModule(
    allowedDependencies = { "catalog :: spi", "profile :: spi" })
package com.moabom.playback;

// com/moabom/recommendation/package-info.java
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
    "catalog :: spi", "playback :: spi", "profile :: spi", "experiment :: spi" })
package com.moabom.recommendation;

// com/moabom/bff/package-info.java  — sink. 역방향 의존은 verify() 가 잡는다
@org.springframework.modulith.ApplicationModule(allowedDependencies = {
    "catalog", "playback", "profile", "recommendation", "experiment" })
package com.moabom.bff;

// com/moabom/shared/package-info.java  — 유일한 OPEN. 크기 상한은 리뷰 규율
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN)
package com.moabom.shared;
```
`[CITED: ARCHITECTURE.md §8.2 (의존 그래프 DAG 확정) + Context7 /spring-projects/spring-modulith fundamentals (`::` named interface 문법, `@NamedInterface` package-info 선언)]`

### C-4. `ModularityTests` — SC2의 게이트 (Docker 불필요, 초 단위)

```java
package com.moabom;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTests {

    static final ApplicationModules modules = ApplicationModules.of(MoabomApplication.class);

    @Test
    void verifiesModularStructure() {
        modules.verify();          // ★ 첫 커밋부터 존재해야 한다 (PITFALLS Pitfall 5)
    }

    @Test
    void writesDocumentation() {
        new Documenter(modules).writeDocumentation();   // build/spring-modulith-docs (PlantUML)
    }
}
```
> `ApplicationModules.of(...).verify()`는 **Spring 컨텍스트를 띄우지 않는 정적 분석**이다 → DB·Docker 없이 수 초에 끝난다. 그래서 커밋 단위 quick gate로 적합하다. `[CITED: Context7 /spring-projects/spring-modulith — plain JUnit 예제]`

### C-5. `.githooks/pre-commit` — D-10 미디어 차단

```sh
#!/bin/sh
# D-10: 미디어 확장자 + 대용량 파일만 차단. verify()/빌드는 CI 소관 (로컬 훅에 태우지 않는다).
# CP-1: *.ts 는 전역 차단하지 않는다 — TypeScript 와 충돌.
set -e
MAX_BYTES=5242880   # 5MB
MEDIA_RE='\.(mp4|mkv|mov|avi|webm|m4s|flv|wmv|mpg|mpeg)$'
PATH_TS_RE='(^|/)(media|transcode-tmp|uploads)/.*\.ts$'

fail=0
staged=$(git diff --cached --name-only --diff-filter=ACMR)
[ -z "$staged" ] && exit 0

for f in $staged; do
  case_hit=0
  echo "$f" | grep -Eiq "$MEDIA_RE" && case_hit=1
  echo "$f" | grep -Eq  "$PATH_TS_RE" && case_hit=1
  if [ "$case_hit" = 1 ]; then
    echo "BLOCKED (media extension): $f" >&2; fail=1; continue
  fi
  if [ -f "$f" ]; then
    size=$(git cat-file -s ":$f" 2>/dev/null || echo 0)
    if [ "$size" -gt "$MAX_BYTES" ]; then
      echo "BLOCKED (>5MB: ${size}B): $f" >&2; fail=1
    fi
  fi
done

if [ "$fail" = 1 ]; then
  echo "" >&2
  echo "미디어/대용량 파일은 저장소에 들어갈 수 없습니다. 미디어 루트는 D:/moabom-media/ 입니다." >&2
  echo "우회가 정말 필요하면: git commit --no-verify (이력 세탁 비용을 감수한다는 뜻)" >&2
  exit 1
fi
```
설치(클론마다 1회, README 명시): `git config core.hooksPath .githooks`

### C-6. SC4 검증 — 히스토리 전체 blob 스캔

```bash
# 5MB 초과 blob 이 히스토리 어디에도 없는가 (워킹트리가 아니라 전 이력)
git rev-list --objects --all \
  | git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' \
  | awk '$1=="blob" && $3 > 5242880 {print $3, $4}'
# → 출력이 비어 있어야 PASS

# 미디어 확장자가 추가된 적이 있는가
git log --all --diff-filter=A --name-only --pretty=format: \
  | sort -u | grep -Ei '\.(mp4|mkv|mov|avi|webm|m4s|flv|wmv)$'
# → 출력 없음 = PASS

# .gitignore 가 실제로 매칭하는가 (라벨이 아니라 결과로 판정)
git check-ignore -v media/probe.mp4 && echo "ignore OK"
git check-ignore -v frontend/next.config.ts && echo "CP-1 회귀! TypeScript 가 무시되고 있다" || echo "ts OK"
```

### C-7. CI 워크플로 골격 (D-09)

```yaml
name: ci
on:
  push: { branches: [main] }
  workflow_dispatch:

jobs:
  core:
    runs-on: ubuntu-latest          # Testcontainers 요구 (STACK §2.9: Windows 러너 불가)
    steps:
      - uses: actions/checkout@v7
        with: { fetch-depth: 0 }     # SC4 히스토리 스캔에 전체 이력 필요

      - name: media guard (SC4)
        run: |
          big=$(git rev-list --objects --all \
            | git cat-file --batch-check='%(objecttype) %(objectname) %(objectsize) %(rest)' \
            | awk '$1=="blob" && $3 > 5242880 {print $3, $4}')
          if [ -n "$big" ]; then echo "$big"; echo "::error::5MB 초과 blob 이 히스토리에 있습니다"; exit 1; fi
          hits=$(git log --all --diff-filter=A --name-only --pretty=format: | sort -u \
            | grep -Ei '\.(mp4|mkv|mov|avi|webm|m4s|flv|wmv)$' || true)
          if [ -n "$hits" ]; then echo "$hits"; echo "::error::미디어 파일이 커밋된 적이 있습니다"; exit 1; fi

      - name: modulith OPEN guard (SC2)
        run: |
          # shared 외의 Type.OPEN 은 경계 소멸 (PITFALLS Pitfall 5)
          hits=$(grep -rln 'Type\.OPEN' backend/src/main/java --include='package-info.java' \
                 | grep -v '/shared/' || true)
          if [ -n "$hits" ]; then echo "$hits"; echo "::error::shared 외 모듈에 Type.OPEN"; exit 1; fi

      - uses: actions/setup-java@v5
        with: { distribution: temurin, java-version: '17' }
      - uses: gradle/actions/setup-gradle@v6

      - name: build + test (verify() 포함)
        working-directory: backend
        run: ./gradlew build --no-daemon

      - name: resilience4j pin gate (G2)
        working-directory: backend
        run: |
          out=$(./gradlew -q dependencyInsight --configuration runtimeClasspath \
                --dependency io.github.resilience4j:resilience4j-spring-boot3)
          echo "$out"
          echo "$out" | grep -q '2\.2\.0' || { echo "::error::resilience4j 2.2.0 핀 이탈"; exit 1; }
          echo "$out" | grep -q '2\.3\.'  && { echo "::error::2.3.x 유입 — Boot3+Java17 기동 실패(issue #2265)"; exit 1; }
          exit 0

      - name: compose config validity (ARCH-05 저비용 프록시)
        run: docker compose --profile full config -q
```

---

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|---|---|---|---|
| start.spring.io로 Boot 3.x 뼈대 생성 | **Initializr는 4.0.7+ 만 제공** → 3.2.12는 손으로 스캐폴딩 | Boot 3.x 전 라인 EOL 이후 (실측 2026-08-06) | CP-2 — 스캐폴딩 태스크를 "Initializr 실행"으로 계획하면 막힌다 |
| `actions/checkout@v4`, `setup-java@v4` | **checkout@v7, setup-java@v5, setup-node@v7, gradle/actions@v6** | 2026-07~08 릴리스 | 오래된 예제 복붙 시 deprecated 경고/노드 런타임 경고 |
| `gradle/gradle-build-action` | **`gradle/actions/setup-gradle`** (통합 리포) | gradle/actions 통합 이후 | 구 액션명은 유지보수 종료 방향 |
| `mysql:mysql-connector-java` | **`com.mysql:mysql-connector-j`** | Boot 3 | `[CITED: STACK.md §5]` |
| `flyway-core` 단독 | **`flyway-core` + `flyway-mysql`** | Flyway 9.x MySQL 분리 | 없으면 `Unsupported Database: MySQL 8.0` |
| Next.js Pages Router | **App Router (Next 16)** | Pages Router 유지보수 모드 | `[CITED: STACK.md §5]` |
| `developmentOnly` (Boot 3.2) | `testAndDevelopmentOnly` (Boot 3.4+) | Boot 3.4 | ⚠️ **최신 문서를 그대로 쓰면 3.2에서 구성 이름 오류** — Context7이 반환한 main 브랜치 스니펫이 이 함정을 포함하고 있었다 |

**Deprecated/outdated (이 프로젝트에서 쓰지 않음):**
- `paketobuildpacks/builder:base`(bionic) — jammy로 대체됨. Boot 3.2 기본값은 `builder-jammy-base:latest`
- Gradle 9.x — Boot 3.2 플러그인 지원 목록 밖 (`[CITED: STACK.md §5, 실패 여부는 미검증 = 정직 표기 유지]`)
- `docker-compose`(v1, 하이픈) — v2 `docker compose`

---

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|---|---|---|---|---|
| JDK 17 | Gradle toolchain, Boot 3.2 baseline | ✓ | `C:/Program Files/Java/jdk-17` | — |
| JAVA_HOME | Gradle 데몬 JVM | ✓ | jdk-21 (Gradle 8.14는 Java 17–24 지원) | — |
| Gradle (시스템) | wrapper 생성 1회 | ✓ | 9.4.1 → `gradle wrapper --gradle-version 8.14.5` | — |
| Docker Engine | compose, Testcontainers, bootBuildImage | ✓ | 28.1.1 (linux 컨테이너) | — |
| Docker Compose v2 | ARCH-05 | ✓ | v2.35.1-desktop.1 | — |
| Node.js | Next 16 빌드/dev | ✓ | **22.17.1** (STACK 핀은 24) | next@16 `engines: >=20.9.0` **충족** → 비차단. FE 컨테이너는 `node:24-alpine`로 고정 |
| npm | FE 의존성 | ✓ | 10.9.2 | — |
| git | 전부 | ✓ | 2.45.1.windows.1 | — |
| GitHub CLI | D-07 repo 생성, D-08 push | ✓ | 2.86.0, **trevi00 로그인됨**, scopes: `repo`·`workflow`·`gist`·`read:org`·`user` | — |
| D: 여유 공간 | 미디어 루트 | ✓ | **470GB free / 932GB** | — |
| ffmpeg | 트랜스코딩·HLS 패키징 | ✗ | PATH에 없음 | **P1의 어떤 성공 기준·가드레일도 ffmpeg를 요구하지 않는다** — ROADMAP이 트랜스코딩 요구사항 전부(PLAY-01/02, ARCH-03, INGST-01)를 Phase 2에 배정했고, STATE.md는 ffmpeg/HLS 수치 확보를 **Phase 2 착수 조건**으로 등록했다. 따라서 P1 계획에 설치 태스크를 넣지 않되, **Phase 2 착수 전 설치가 선행 조건**임을 인수인계에 남긴다 |

**추가 실측:**
- `D:/moabom-media/` **미존재** → P1이 생성해야 한다 (`.env`/`.env.example`의 `MEDIA_ROOT`와 함께)
- git remote **없음**, 브랜치 `master`, 47 objects / size-pack 0 → **미디어 오염 이력 0, 깨끗한 출발점** (SC4의 baseline이 이미 green)
- `core.autocrlf=true` (local·global) → CP-5 대응 필수
- GitHub Actions `workflow` 스코프 보유 → `.github/workflows/*` push 가능 ✓

**Missing dependencies with no fallback:** 없음 — P1을 막는 환경 결손 0.
**Missing dependencies with fallback:** Node 22.17.1(핀은 24) — Next 16 최소 요구를 충족하므로 로컬 개발 비차단, 컨테이너는 24로 고정해 산출물 일관성 유지.

---

## Validation Architecture

### Test Framework

| Property | Value |
|---|---|
| Framework | JUnit Jupiter 5.10.5 (Boot 3.2.12 BOM) + Spring Modulith 1.1.12 test starter + Testcontainers 1.19.8 |
| Config file | `backend/build.gradle` (`tasks.withType(Test) { useJUnitPlatform() }`) — **현재 미존재 → Wave 0** |
| Quick run command | `cd backend && ./gradlew test --tests '*ModularityTests*'` (Docker 불필요, 초 단위) |
| Full suite command | `cd backend && ./gradlew build` |
| 비-JUnit 게이트 | git/compose/gradle CLI 기반 셸 체크 (SC4·G2·G3) — CI 스텝으로 코드화 |

### Phase Requirements → Test Map

| ID | Behavior | Test Type | Automated Command | File Exists? |
|---|---|---|---|---|
| **V-SC1a** | `docker compose --profile full config` 가 유효 (인터폴레이션·프로필·의존 그래프) | smoke | `docker compose --profile full config -q` | ❌ Wave 0 (compose.yaml) |
| **V-SC1b** | full 프로필 기동 후 앱 헬스 200 | manual-only | `docker compose --profile full up -d --wait && curl -f http://localhost:3000/api/health` | ❌ Wave 3 |
| **V-SC1c** | 4개 인프라 컨테이너가 전부 running/healthy | manual-only | `docker compose --profile full ps --format '{{.Service}} {{.Status}}'` | ❌ Wave 3 |
| **V-SC2a** | `ApplicationModules.verify()` green | unit | `./gradlew test --tests '*ModularityTests*'` | ❌ Wave 1 |
| **V-SC2b** | `shared` 외 `Type.OPEN` 0건 | smoke | `grep -rln 'Type\.OPEN' backend/src/main/java --include=package-info.java \| grep -v /shared/` → 빈 출력 | ❌ Wave 1 (CI 스텝) |
| **V-SC2c** | 8모듈 전부 `@ApplicationModule` 선언 보유 | smoke | `ls backend/src/main/java/com/moabom/*/package-info.java \| wc -l` = 8 + grep 확인 | ❌ Wave 1 |
| **V-SC2d** ★ | **경계 위반 커밋이 실제로 CI에서 붉어진다** (반사실) | manual-once | 임시 위반(`catalog`가 `playback.internal` import) 추가 → `./gradlew test` **FAIL 확인** → revert. 결과를 phase 검증 로그에 기록 | ❌ Wave 4 |
| **V-SC3a** | 빈 DB에 Flyway만 돌려 스키마 재현 | integration | `./gradlew test --tests '*SchemaMigrationTest*'` (Testcontainers `mysql:8.0`) | ❌ Wave 2 |
| **V-SC3b** | `title.attribution` / `title.license` 컬럼 존재 + NOT NULL | integration | 위 테스트 내 `information_schema.columns` 단언 | ❌ Wave 2 |
| **V-SC3c** | 한글 골든 시드 ≥5건이 utf8mb4로 라운드트립 | integration | `SELECT count(*) FROM title WHERE title_ko REGEXP '[가-힣]'` ≥ 5 + `'인터스텔라'` 정확 일치 | ❌ Wave 2 |
| **V-SC3d** | Blender CC-BY 행의 attribution 비어있지 않음 | integration | `SELECT count(*) FROM title WHERE license LIKE 'CC-BY%' AND attribution <> ''` ≥ 1 | ❌ Wave 2 |
| **V-SC4a** | 히스토리 전체에 5MB 초과 blob 0건 | smoke | C-6의 `rev-list \| cat-file` 파이프 → 빈 출력 | ❌ Wave 0 (CI 스텝) |
| **V-SC4b** | 미디어 확장자가 추가된 이력 0건 | smoke | `git log --all --diff-filter=A --name-only` + grep → 빈 출력 | ❌ Wave 0 |
| **V-SC4c** | `.gitignore`가 미디어를 실제로 매칭 **하고** TypeScript는 매칭하지 않음 (CP-1 회귀) | smoke | `git check-ignore -v media/probe.mp4` PASS **AND** `git check-ignore frontend/next.config.ts` 비매칭 | ❌ Wave 0 |
| **V-SC4d** ★ | **pre-commit 훅이 실제로 차단한다** (반사실) | manual-once | 6MB 더미 + `probe.mp4` 스테이징 → `git commit` **거부 확인** → unstage. 로그 기록 | ❌ Wave 4 |
| **V-SC4e** | 미디어 루트가 저장소 밖에 존재 | smoke | `test -d D:/moabom-media` && `git -C D:/moabom check-ignore` 무관함 확인 | ❌ Wave 0 |
| **V-G1** | ADR-0001 존재 + 필수 3요소(EOL 근거·3.2.12 핀·non-blocking CVE) 언급 | smoke | `test -f docs/adr/0001-runtime-versions.md` + `grep -c` 키워드 3종 | ❌ Wave 4 |
| **V-G2a** | resilience4j 해석 버전 = 2.2.0, 2.3.x 부재 | smoke | `./gradlew -q dependencyInsight --configuration runtimeClasspath --dependency io.github.resilience4j:resilience4j-spring-boot3` grep | ❌ Wave 1 |
| **V-G2b** | build.gradle 버전 옆에 근거 주석 존재 | smoke | `grep -A2 'resilience4j-spring-boot3' backend/build.gradle \| grep -q '2.3'` (금지 사유 주석) | ❌ Wave 1 |
| **V-G3a** | compose에 mysql 기동 파라미터 3종 존재 | smoke | `docker compose config \| grep -E 'ngram_token_size=2\|utf8mb4_0900_ai_ci'` | ❌ Wave 0 |
| **V-G3b** ★ | **파라미터가 실제로 먹었다** (문자열 존재가 아니라 결과) | integration | 테스트 컨테이너에서 `SHOW VARIABLES LIKE 'ngram_token_size'` = 2, `@@collation_server` = `utf8mb4_0900_ai_ci` | ❌ Wave 2 |
| **V-G4a** | 컴파일/테스트 인코딩 UTF-8 명시 | smoke | `grep -q "options.encoding = 'UTF-8'" backend/build.gradle` + `.editorconfig charset=utf-8` | ❌ Wave 1 |
| **V-G4b** | ASCII-safe 파일명 정책 문서 존재 + D-14 규율 기록 | smoke | `test -f docs/adr/0002-media-and-encoding.md` + `grep -q 'schema-initialization'` | ❌ Wave 4 |
| **V-D02** | 단일 진입 오리진이 실동작 (문서 아님) | manual-only | full 기동 후 `curl -f http://localhost:3000/api/health` 200 + FE 스텁 페이지 렌더 확인 | ❌ Wave 3 |
| **V-CP5** | CI가 ubuntu에서 `./gradlew` 실행 성공 (eol/exec bit) | smoke | CI `core` job green 자체가 증거 | ❌ Wave 0 |

**manual-only 정당화:**
- **V-SC1b/V-SC1c/V-D02**: full 프로필 기동은 `bootBuildImage`(Paketo, 수 분) + FE 이미지 빌드를 요구한다. D-09가 P1 CI를 "최소 코어 게이트"로 못박았으므로 CI에 넣지 않는다. 대신 **V-SC1a(`compose config -q`)를 자동 프록시**로 두어 YAML·인터폴레이션·프로필·의존 그래프 오류는 매 push에서 잡는다. 실기동 검증은 phase 게이트에서 사람이 1회 수행하고 결과를 기록.
- **V-SC2d/V-SC4d (반사실)**: 자동화하면 "일부러 깨진 커밋"이 이력에 남는다. 1회 수동 프로브 + 로그 기록이 맞다. **이 두 항목을 빼면 SC2·SC4의 문장("CI에서 실패한다", "들어가지 못한다")이 증명되지 않는다** — 게이트의 존재는 게이트의 작동이 아니다.

### Sampling Rate

- **Per task commit:** `cd backend && ./gradlew test --tests '*ModularityTests*'` (Docker 불필요, 초 단위) + pre-commit 훅 자동 발화
- **Per wave merge:** `cd backend && ./gradlew build` + `docker compose --profile full config -q` + C-6 미디어 스캔 3종
- **Phase gate:** 위 전체 + `docker compose --profile full up -d --wait` 실기동 + `curl -f localhost:3000/api/health` + 반사실 프로브 2종(V-SC2d, V-SC4d) 수행 및 로그 기록 → 이후 `/kha-verify-uat`

### Wave 0 Gaps

현재 저장소에 **테스트 인프라가 전무하다**(planning 커밋 5개뿐). 아래는 전부 신규:

- [ ] `backend/settings.gradle` · `build.gradle` · `gradle.properties` — 프레임워크 정의 자체
- [ ] `backend/gradlew` + `gradle/wrapper/gradle-wrapper.properties` (**8.14.5**) — `gradle wrapper --gradle-version 8.14.5`
- [ ] `.gitattributes` (eol=lf) + `git update-index --chmod=+x gradlew` — **CI가 돌기 위한 전제** (CP-5)
- [ ] `.gitignore` (CP-1 준수 패턴) — 첫 커밋 이전
- [ ] `.githooks/pre-commit` + README의 `core.hooksPath` 안내
- [ ] `compose.yaml` + `.env.example` + `infra/{nginx,prometheus}/…`
- [ ] `backend/src/test/java/com/moabom/ModularityTests.java` — V-SC2a
- [ ] `backend/src/test/java/com/moabom/SchemaMigrationTest.java` (+ `@ServiceConnection` MySQLContainer, CP-10의 command 미러링) — V-SC3a~d, V-G3b
- [ ] `.github/workflows/ci.yml` — V-SC4a/b/c, V-SC2b/c, V-G2a, V-SC1a
- [ ] `.github/dependabot.yml` — V-G1 (CP-9 ignore 규칙)
- [ ] `D:/moabom-media/` 디렉터리 생성 — V-SC4e

---

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control (P1) |
|---|---|---|
| V1 Architecture | yes | Modulith 경계 + `verify()` = 신뢰 경계의 기계적 집행. ADR로 결정 근거 기록 |
| V2 Authentication | **no** | AUTH-01/02 = Phase 3. P1에 security 스타터를 넣지 않는 것이 오히려 안전(CP-7 — 죽은 permitAll 설정이 남지 않음) |
| V3 Session Management | no | Phase 3 |
| V4 Access Control | 부분 | Actuator 노출 화이트리스트만 (아래) |
| V5 Input Validation | 부분 | P1에 사용자 입력 표면 없음(헬스 스텁만). Flyway 시드는 정적 SQL |
| V6 Cryptography | no | 암호 자산 없음 |
| **V14 Configuration** | **yes** | 공개 저장소(D-07) + 시크릿 유출 방지가 이 phase의 실제 보안 표면 |

### Known Threat Patterns for {Spring Boot 3.2 + docker-compose + 공개 GitHub 저장소}

| Pattern | STRIDE | Standard Mitigation |
|---|---|---|
| Actuator 전체 노출(`include: "*"`)로 `/env`·`/heapdump`·`/configprops` 유출 | Information Disclosure | `management.endpoints.web.exposure.include: health,info,prometheus` **화이트리스트 명시**. `"*"` 금지 |
| `.env`(DB 비밀번호·경로) 커밋 | Information Disclosure | `.gitignore`에 `.env`, 커밋되는 것은 `.env.example`만. compose는 `${VAR:-default}` 형태로 기본값 노출 |
| 공개 repo에 토큰·키 유입 | Information Disclosure | GitHub **Secret scanning + Push protection** 활성화(public repo 무료). D-07 직후 1회 설정 |
| MySQL root 비밀번호 하드코딩 | Information Disclosure | 로컬 전용 값임을 README/ADR에 **명시**하고 실 시크릿과 구분. 인터넷 미노출이 전제(ADR-0001) |
| EOL 런타임(Boot 3.2, MySQL 8.0)의 미패치 CVE | Elevation of Privilege | G1의 **non-blocking** CVE 가시성(Dependabot) + ADR에 "인터넷 미노출 / 승격 경로" 명시. 붉은 CI 방치가 신호를 죽이므로 blocking 금지 `[CITED: STACK.md §0 완화 3종]` |
| 미디어 볼륨 쓰기 권한이 nginx에 부여됨 | Tampering | compose 마운트를 `:ro` 로 (C-1) — nginx는 읽기 전용, 쓰기 주체는 ingest(P8)뿐 `[CITED: ARCHITECTURE.md §8.1]` |
| Dependabot 자동 머지로 핀 이탈 | Tampering (공급망) | auto-merge 미설정 + CP-9 ignore 규칙 + G2 CI 게이트의 3중 방어 |

> **정직 표기:** P1에는 인증·권한 표면이 없으므로 ASVS V2~V4의 실질 커버리지는 0에 가깝다. 이는 결손이 아니라 **스코프**다 — 해당 통제는 Phase 3(AUTH)·Phase 8(AUTH-03)의 소관이며, P1이 제공하는 것은 그 통제가 들어설 **모듈 경계와 설정 규율**뿐이다.

---

## Assumptions Log

> `[ASSUMED]` 태그가 붙은 주장 전부. planner/discuss-phase는 이들을 **실행 중 실측으로 닫히는 항목**으로 취급하고, 계획에 확인 스텝을 배치할 것.

| # | Claim | Section | Risk if Wrong |
|---|---|---|---|
| **A1** | `spring-modulith-starter-jpa` + `ddl-auto=validate` + `event_publication` 부재 → **기동 실패**. (스타터가 JPA 엔티티를 노출한다는 것은 문서상 사실이나, 정확한 실패 모드는 미실측) | CP-4 | 낮음. 틀렸더라도 권장 조치(P1은 starter-core만)는 D-14와 독립적으로 옳다. 반대로 맞는데 무시하면 Wave 1이 기동 불가로 막힌다 |
| **A2** | `host.docker.internal:host-gateway` 단일 스크레이프 타깃이 dev(호스트 JVM)와 full(컨테이너, 8080 publish) 양쪽에서 동작 | CP-8 | 중간. 틀리면 Grafana 대시보드가 한쪽 모드에서 빈다. 대안 = 두 개의 scrape job(하나는 상시 DOWN 감수) |
| **A3** | Next 16의 `next.config.ts` `rewrites()`가 **서버 기동 시** 평가되어 `next start` 런타임 env(`BACKEND_ORIGIN`)를 반영 | P-5 | 중간. 틀리면 FE 이미지가 빌드 시점 오리진에 고정 → dev/full 중 한쪽이 깨진다. 대안 = 이미지를 full 전용으로 빌드하고 dev는 `npm run dev`만 사용(D-06이 이미 그 모양) |
| **A4** | 빈 SPI 패키지(`@NamedInterface` + 포트 인터페이스 1개)로 `allowedDependencies = "x :: spi"` 참조가 정상 해석된다 | P-3, CP-6 | 낮음-중간. 틀리면 verify()가 초기에 붉어진다 — **오히려 조기 발견**이라 비용이 작다. 완화가 이미 권장안에 포함(포트 인터페이스 1개 배치) |
| **A5** | Paketo `builder-jammy-base:latest`가 현 시점 정상 동작 (이미지는 2026-08-04 갱신 확인, 그러나 실 빌드는 미수행) | CP-3 | 중간. 틀리면 SC1의 full 프로필이 막힌다 → 대안 = 빌더 버전 핀 또는 CP-3의 B안(Dockerfile) |
| **A6** | `mysqladmin ping --silent` 헬스체크가 `mysql:8.0` 이미지에서 인증 없이 성공 (초기화 중에는 실패, 완료 후 성공) | C-1 | 낮음. 흔한 관용구지만 이미지 버전에 따라 종료코드가 다를 수 있다. 대안 = `mysqladmin ping -u root -p$MYSQL_ROOT_PASSWORD` |
| **A7** | `resolutionStrategy.force`가 `dependencyInsight` 출력에서 2.2.0으로 보이며 G2 게이트와 충돌하지 않음 | Standard Stack | 낮음. `force`가 적용되면 dependencyInsight가 "forced" 표기와 함께 2.2.0을 보여준다 — grep 패턴이 그대로 통과 |

---

## Open Questions

1. **CP-3 — SC1의 "한 커맨드" 문구를 어떻게 착지시킬 것인가**
   - 알고 있는 것: compose는 Gradle을 실행할 수 없다. D-01은 bootBuildImage, D-03은 래퍼 금지.
   - 불명확한 것: 사용자가 "선행 빌드 1회 + compose up"을 SC1 충족으로 받아들이는가, 아니면 Dockerfile 전환(D-01 변경)을 원하는가.
   - 권장: **A안**(`pull_policy: never` + README 선행 빌드 1줄)으로 계획하고, plan 문서에 "SC1 = 이미지 빌드 후 한 커맨드"로 **검증 문구를 명시적으로 재서술**. B안 채택은 D-01 재확인 필요.

2. **G1의 "non-blocking CVE 스캔" 구현체**
   - 알고 있는 것: Dependabot은 CI 상태에 영향 없어 태생적 non-blocking. Trivy는 `continue-on-error`가 필요.
   - 불명확한 것: 사용자가 "스캔 리포트가 CI 로그에 보이는 것"을 원하는지, "PR로 알림 오는 것"을 원하는지.
   - 권장: **Dependabot 우선**(비용 0, CP-9 ignore 필수) + 필요 시 Trivy job을 `continue-on-error: true`로 추가. ADR-0001에 선택 근거 기록.

3. **`title` 스키마 컬럼 상세 (CONTEXT: Claude 재량, D-11 범위 내)**
   - 알고 있는 것: `attribution`/`license` NOT NULL은 확정. 검색은 P7이지만 한글 골든 시드는 P1.
   - 불명확한 것: 원제/한국어 제목 이원화(`title_ko`/`title_en`)를 P1에 넣을지. STACK §2.5의 FULLTEXT 대상 컬럼이 `(title_ko, title_en, synopsis)`이므로 **P7에서 인덱스를 붙일 컬럼이 P1에 존재하면 재작업이 없다**.
   - 권장: `title_ko`·`title_en`·`synopsis`까지는 P1에 두되 **FULLTEXT 인덱스는 P7 소관**(ngram vs LIKE 실측 전 인덱스 선점 금지 — SRCH-02 규율). D-11의 "미확정 설계 선행 추측 금지"와 충돌하지 않는 최소선.

4. **ADR 2장 분할 vs 1장 통합**
   - ROADMAP은 가드레일 4종을 나열했을 뿐 문서 개수를 정하지 않았다. 본 문서는 0001(런타임 버전/EOL/CVE)과 0002(미디어·인코딩·D-14 규율)로 나눴다.
   - 권장: 분할 유지 — 0001은 "왜 EOL 스택인가"라는 리뷰어의 첫 질문에 답하는 문서(포트폴리오 가치)이고, 0002는 운영 규약이라 독자와 수명이 다르다.

5. **`master` → `main` 전환 시점과 remote 생성 순서 (D-07/D-08)**
   - 알고 있는 것: 로컬 5커밋, remote 없음, gh CLI 인증됨(trevi00).
   - 권장 순서: (1) 가드레일 파일(.gitignore/.gitattributes/훅)을 **먼저 로컬 커밋** → (2) `git branch -m master main` → (3) `gh repo create trevi00/moabom --public --source=. --remote=origin` → (4) push. **가드레일이 첫 push 이전에 존재**해야 D-07의 근거("첫 커밋부터 강제")가 성립한다. 다만 기존 5개 planning 커밋은 이미 존재하므로 "첫 커밋"은 사후적으로 만족 불가 — 정직하게 "**첫 push 이전**"으로 표현할 것.

---

## Sources

### Primary (HIGH confidence)

**프로젝트 내부 리서치 (본 문서의 1차 입력)**
- `.planning/research/STACK.md` — 전 버전 핀의 정본, §2.1(resilience4j 2.2.0/2.3.0 금지), §2.6(동일 오리진), §3(build.gradle·compose 발췌), §5(What NOT to Use), §7(호환 매트릭스), §9(미검증 7종)
- `.planning/research/ARCHITECTURE.md` — §2(패키지 구조 정본, 레이어-우선 금지), §8.2(의존 그래프 DAG + `allowedDependencies` 코드), §9(빌드 순서 강제 지점)
- `.planning/research/PITFALLS.md` — Pitfall 3(미디어 git), 5(Modulith 경계 침식·`Type.OPEN`·named interface 비직관 규칙), 10(ngram 기동 파라미터·한글 골든 시드), 14(initdb 1회성 → Flyway 단일 진실), 17(라이선스 컬럼), Pitfall-to-Phase Mapping, "Looks Done But Isn't" 체크리스트
- `.planning/research/SUMMARY.md` — §Phase 0 Delivers(첫 커밋 규율 목록)
- `.planning/phases/01-foundation-guardrails/01-CONTEXT.md` — D-00~D-14 locked decisions

**공식 문서 (본 세션 실측)**
- Context7 `/spring-projects/spring-modulith` — `spring-modulith-starter-test` 좌표, `ApplicationModules.of(App.class).verify()` plain-JUnit 패턴, `@ApplicationModule(allowedDependencies)` / `@NamedInterface` package-info 선언, `::` 문법, `:: *` 와일드카드
- Context7 `/spring-projects/spring-boot` — `spring.docker.compose.file`(상대경로 지원)·`profiles.active`·`lifecycle-management`·테스트 시 기본 비활성
- https://docs.spring.io/spring-boot/docs/3.2.12/gradle-plugin/reference/htmlsingle/ — `bootBuildImage` 기본 빌더 `paketobuildpacks/builder-jammy-base:latest`, Docker 데몬 요구, `imageName` 기본값
- https://docs.docker.com/compose/how-tos/profiles/ — 무프로필 서비스 상시 활성, 프로필 서비스 타깃 시 `depends_on` 자동 기동

**레지스트리·API 실측 (2026-08-06)**
- `search.maven.org` — spring-modulith-bom(1.1.12가 1.1 최고), resilience4j-spring-boot3(2.2.0·2.3.0 병존), spring-boot-starter-parent(3.2.12가 3.2 최고)
- `repo1.maven.org` — `flyway-mysql:9.22.3` POM HTTP 200
- `services.gradle.org/versions/all`·`/current` — 8.14.5(8.x 최종), 9.6.1(현행)
- `registry.npmjs.org` — next(latest 16.3.0, 16.2 최종 16.2.12, `engines.node >=20.9.0`), react 19.2.8, hls.js 1.6.17
- `hub.docker.com/v2` — mysql:8.0(2026-05-05) · nginx:stable-alpine · node:24-alpine · prom/prometheus:v3.13.2 · grafana/grafana:13.1.2 · paketobuildpacks/builder-jammy-base:latest(2026-08-04) 전부 HTTP 200
- `gh api repos/*/releases/latest` — checkout v7.0.1 · setup-java v5.7.0 · setup-node v7.0.0 · gradle/actions v6.3.0
- `start.spring.io/metadata/client` — 선택 가능 bootVersion = 4.0.7 / 4.0.8-SNAPSHOT / 4.1.0 / 4.1.1-SNAPSHOT (**3.x 부재**)
- 로컬 환경 실측 — JDK 17/21/24 설치, JAVA_HOME=21, gradle 9.4.1, Docker 28.1.1 + compose v2.35.1, node 22.17.1, gh 2.86.0(trevi00 인증), git 2.45.1 `core.autocrlf=true`, D: 470GB free, `D:/moabom-media` 미존재, remote 없음

### Secondary (MEDIUM confidence)

- https://docs.gradle.org/current/userguide/compatibility.html — Gradle 8.x는 Java 17–24로 실행 가능, Java 21은 8.5+, Java 24는 8.14+
- WebSearch(교차) — Boot 3.4에서 `bootBuildImage` 기본 빌더가 `builder-jammy-java-tiny`로 변경(3.2는 여전히 jammy-base), `builder-jammy-base:latest` 관련 빌드 실패 보고(spring-boot#45335 — **단일 이슈 기반, 이미지는 현재도 갱신 중이라 상충**)

### Tertiary (LOW confidence — 검증 필요로 표기)

- Assumptions Log A1~A7 — 전부 본 세션에서 실행 검증하지 않았다. 실행 중 실측 대상.

---

## Metadata

**Confidence breakdown:**

| Area | Level | Reason |
|---|---|---|
| 버전 핀 (Maven/npm/Docker/Actions) | **HIGH** | 전 항목을 레지스트리 API로 직접 조회. STACK.md 핀과 대조해 드리프트 4건(Gradle 8.14.5, Next 16.2.12, hls.js 1.6.17, 이미지 태그 5종) 확인 |
| Modulith 모듈 규약·verify 배선 | **HIGH** | ARCHITECTURE.md(공식 문서 인용) + Context7 원문 교차. 단 "빈 SPI 패키지" 동작만 ASSUMED(A4) |
| compose profiles 토폴로지 | **HIGH** | Docker 공식 문서 원문 규칙 확보. dev/full 이원화가 D-01과 1:1 대응 |
| Flyway/스키마/시드 | **HIGH** | PITFALLS Pitfall 14·17이 공식 문서 기반 [H]. 컬럼 상세만 열림(OQ-3) |
| 미디어 격리 게이트 | **HIGH** | git 명령 조합은 결정론적. CP-1(`.ts` 충돌)이 이 조사의 최대 수정 사항 |
| bootBuildImage ↔ compose 배선 | **MEDIUM** | 문서상 사실은 확보(compose는 Gradle 미실행, Docker 데몬 요구). 실 빌드 미수행(A5) + 결정 필요(OQ-1) |
| Next rewrites 런타임 해석 | **MEDIUM** | 패턴은 STACK.md 확정. Next 16에서의 env 평가 시점만 ASSUMED(A3) |
| Prometheus 스크레이프 이중 모드 | **MEDIUM** | 관용 패턴이나 본 조합 미실측(A2) |

**Research date:** 2026-08-06
**Valid until:** 2026-09-05 (30일 — 코어는 EOL 라인이라 오히려 안정적. 단 **GitHub Actions 메이저**와 **npm/Docker 태그**는 빠르게 움직이므로 phase 착수가 2주를 넘기면 §Standard Stack 표만 재확인할 것)

---
*Phase: 01-foundation-guardrails*
*Research synthesized from project-level research (STACK/PITFALLS/ARCHITECTURE/SUMMARY) + 2026-08-06 registry/environment 실측*
