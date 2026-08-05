# Project Research Summary

**Project:** 모아봄 (moabom) — Netflix-class VOD 스트리밍 서비스 포트폴리오
**Domain:** SVOD 스트리밍 서비스 (Spring Modulith BE + Next.js/hls.js FE, HLS 어댑티브 재생, lolomo row 홈, 관리자 인제스트, MVP 7 슬라이스)
**Researched:** 2026-08-06
**Confidence:** MEDIUM-HIGH

## Executive Summary

모아봄은 "실사용자 제품"이 아니라 **채용 평가자를 소비자로 둔 엔지니어링 포트폴리오**다. 4개 리서치 트랙(STACK/FEATURES/ARCHITECTURE/PITFALLS)이 독립적으로 도달한 공통 결론은 하나다 — **Netflix 원문(TechBlog 13종 + RFC 8216 + arXiv 검색 논문)의 아키텍처 패턴을 "우리 스케일에서 정직하게" 축소 이식하고, 그 축소 판단 자체를 산출물로 문서화하라.** ARCHITECTURE.md의 "축소 이식 정직 대조표"가 이 원칙의 실행판이다: Spring Modulith 6모듈은 팀 소유권 분리(N5/N6)의 축소이지 물리 분리가 아니고, Resilience4j 서킷브레이커는 인-프로세스 호출에서 스레드 격리 효과 없이 "예외/지연 폴백의 가시적 데모"로 정직하게 재정의되며, nginx 정적 서빙은 Open Connect(N14)의 "CDN 오리진 개념 증명"이지 실제 CDN이 아니다. 이 정직성 규율이 4개 문서 전체를 관통한다.

권장 접근은 **위험 역순 배치 금지**로 요약된다. Core Value(브라우즈→재생→이어보기)를 마지막에 몰아넣는 흔한 실수(PITFALLS Pitfall 1)를 구조로 막기 위해, 모든 트랙이 독립적으로 "손으로 ffmpeg 한 번 돌린 산출물 → nginx 정적 서빙 → hls.js 재생 → 새로고침 후 재개"라는 **Java 코드 0줄짜리 수직 슬라이스를 최우선**으로 지목한다(ARCHITECTURE의 P0.5, PITFALLS의 P1, FEATURES의 "인제스트 2단 분해 (a) 배치 스크립트"). 그 다음은 도메인 골격(catalog→profile→playback) → 비개인화 BFF(홈 조립, 이게 나중에 서킷브레이커 폴백이 됨) → 개인화 추천+복원력 데모 → 검색/마이리스트 → 인제스트 제품화 → 실험/관측 순으로, "폴백을 먼저 만들고 그 위에 개인화를 얹는" 순서가 4개 문서에서 반복된다.

핵심 리스크는 세 갈래다. (1) **미디어 파이프라인 함정**(키프레임 미정렬로 인한 ABR 끊김, ffmpeg stderr 미배수 데드락, Windows 경로/한글 인코딩, git에 미디어 유입)은 전부 1차 출처 기반 HIGH confidence로 문서화됐고 P0~P2에 예방책이 배치돼 있다. (2) **버전 핀 리스크**(Boot 3.2 EOL, resilience4j 2.3.0 기동 실패, springdoc 3.x 비호환)는 STACK.md가 정확한 버전과 "실패하는 조건"을 1:1로 못박아 완화됐다. (3) **한국어 검색·시리즈 모델**은 4개 문서 전부가 "결정하지 않음"으로 명시한 열린 결정이며, 특히 Apple HLS Authoring Spec 원문은 **STACK/FEATURES/ARCHITECTURE/PITFALLS 네 문서 전부가 독립적으로 확보 실패**(WebFetch가 본문을 못 가져옴)를 보고했다는 점이 눈에 띈다 — 로드맵 P0~P2 사이 어딘가에서 사람이 직접 열어 재확인하는 태스크가 필요하다.

## Key Findings

### Recommended Stack

핵심 스택(Java 17 / Boot 3.2 / Modulith / JPA / MySQL 8.0 / Next.js+hls.js)은 브리프에서 이미 잠긴 결정이며, STACK.md는 그 위에서 정확한 버전 핀과 열려 있던 보조 라이브러리를 확정했다. Boot 3.2는 2024-12-31 OSS EOL이지만(HIGH confidence), 로컬/도커 전용 포트폴리오라 실 운영 리스크는 낮다고 판단 — 대신 `3.2.12`(라인 최종 패치) 고정 + ADR 1장 + non-blocking CVE 스캔의 3종 완화책을 P0에 배치할 것을 권고한다.

**Core technologies:**
- **Spring Boot 3.2.12 + Spring Modulith 1.1.12**: 공식 호환 매트릭스가 명시하는 유일한 안전 조합(3.3.x는 Modulith 1.2.x 필요) — HIGH
- **Resilience4j 2.2.0 (2.3.0 금지)**: 2.3.0은 Boot 3+Java 17에서 RxJava 미포함 시 `ClassNotFoundException`으로 기동 자체가 실패(GitHub 이슈 #2265 확인) — MEDIUM, 그러나 회귀 방지 규율(버전 옆 주석 + dependencyInsight 게이트) 필수
- **자체 ProcessBuilder ffmpeg 어댑터** (Jaffree/bramp 미도입): 우리 워크로드는 "파일→파일 배치"뿐이라 라이브러리가 주는 기능(FTP 서버 구동 등)이 오히려 부작용
- **`IngestJob` 상태머신 + Modulith 이벤트** (Spring Batch 아님): chunk-oriented ETL 모델이 우리 워크로드(외부 프로세스 1개 장시간 실행)에 안 맞음 — 단, 크래시 시 좀비 잡 방지용 reconciler가 필수 동반 스코프
- **MySQL 8.0 FULLTEXT `WITH PARSER ngram`** (Elasticsearch 미도입): `ngram_token_size`가 read-only라 docker-compose `command:`에 기동 시 명시 필요 — MEDIUM, 1글자/초성 검색 한계는 알려진 트립와이어
- **hls.js 1.6.16 client-only + Safari 네이티브 폴백**: `react-player` 류 래퍼 금지(ABR 이벤트를 가려 이어보기/복원력 데모를 못 만듦)
- **세션 쿠키(Spring Session JDBC) + Next.js `rewrites` 동일 오리진**: JWT localStorage 금지. Safari HLS 네이티브 경로가 커스텀 헤더를 못 붙이므로 쿠키 기반이 구조적으로 강제됨

### Expected Features

FEATURES.md는 "사용자 기준 table stakes"와 "평가자 기준 differentiator"를 분리해 스코프 폭발을 막는다.

**Must have (table stakes):**
- 프로필 분리(가구 1계정 N프로필, 데이터 전 도메인이 `profile_id` FK) — Netflix Help 공식 확인(HIGH)
- lolomo row 홈(후보→필터→랭킹→선택→중복제거 5단계 조립) — Netflix TechBlog 원문 확인(HIGH)
- HLS 어댑티브 재생 + 이어보기 + **완료 임계값**(없으면 다 본 영화가 이어보기 행에 영구 잔류)
- 즉시 검색(디바운스) + 짧은 쿼리 대응 + 무결과 대체 착지(카탈로그 외 검색이 13% — arXiv 확인)
- 마이리스트, Top10 랭킹 행, 관리자 콘텐츠 인제스트(다른 모든 기능의 전제)

**Should have (differentiators, 평가자 신호):**
- Resilience4j 서킷브레이커 폴백 **실측 데모**(Grafana에서 OPEN 전이 관측)
- 미디어 검사→사다리 결정→검증 3단 분리(Netflix VIS/CAS/LGS/VES/VVS/VQS와 개념적 동형)
- RowProvider 플러그인 구조(OCP — 새 행 = 파일 1개 + 등록 1줄)
- 한국어 초성/부분 일치 검색(국내 평가자 즉시 체감 차별점)
- 수동 화질 선택(비용 대비 데모 효과 최고)

**Defer (v2+):**
- DRM/다운로드/4K/라이브 스트리밍(저작권-프리 콘텐츠라 보호 대상 자체가 없음 — "왜 DRM을 안 넣었나"가 좋은 신호)
- 협업 필터링 추천(카탈로그 규모에서 co-play 행렬이 사실상 비어 있음)
- 시리즈/에피소드 모델(오픈무비는 대체로 단편 — 있어야만 "다음 화 자동재생" 등 3개 기능군이 성립)

### Architecture Approach

BFF가 서버에서 완성된 lolomo JSON을 조립해 1회 응답으로 반환하고(Netflix N4 원형), 미디어 바이트는 Spring이 절대 만지지 않고 nginx가 직접 서빙한다(Open Connect N14 축소). 6개 도메인 모듈(catalog/playback/profile/recommendation/experiment/ingest) + bff + shared(OPEN)의 8모듈 구조이며, `ApplicationModules.verify()`가 P0 첫 커밋부터 존재해야 사이클 침식(`Type.OPEN` 도피)을 막을 수 있다는 것이 아키텍처 리서치의 핵심 경고다.

**Major components:**
1. **catalog** — 최하층(아무에게도 의존 안 함), title/genre/asset 포인터·발행 상태·Top-N 집계
2. **playback** — 시청 위치·재생 티켓·정책 판단("재생 가능한가?")을 소유, catalog/profile SPI만 소비
3. **recommendation** — page generation(행 목록 선정/정렬/중복제거)만, 메타데이터 원본은 안 가짐 — RowProvider 레지스트리로 OCP 확장
4. **ingest** — 미디어 볼륨 쓰기의 유일한 주체, catalog 발행 결정권은 없고 이벤트(`TitleAssetPublished`)만 발행
5. **bff** — 조립 전용, 영속성 0, 도메인 정책 판단 0

### Critical Pitfalls

1. **재생이 마지막 페이즈로 밀림(리스크 역순)** — Java 0줄 수직 슬라이스(손 트랜스코딩→nginx→hls.js→새로고침 재개)를 최우선 phase로 못박아 구조로 방지
2. **렌디션 간 키프레임 미정렬** — `-sc_threshold 0 -g -keyint_min` 사다리 전체 공통 고정 없이는 ABR 전환에서 끊김. `EXTINF` diff 스크립트를 P2 자동 검증에 코드화(사람 눈 검사 금지 — Apple `mediastreamvalidator`는 macOS 전용이라 이 프로젝트에서 못 씀)
3. **미디어 산출물이 git 히스토리에 유입** — 미디어 루트를 저장소 밖(`D:/moabom-media/`)에 두어 물리적으로 사고를 차단. `.gitignore`는 첫 커밋 이전에
4. **Spring Modulith 경계 침식** — JPA 양방향 연관이 verify() 사이클 위반을 만들고, 늦게 CI에 붙이면 `Type.OPEN`으로 도피하며 경계가 소멸. verify()는 모듈이 빈 껍데기일 때부터 켠다
5. **ffmpeg stderr 미배수 데드락** — 짧은 테스트 클립에서는 재현 안 되다가 장편에서 확실히 걸림. `redirectErrorStream` + 별도 스레드 드레인 + `waitFor(timeout)` 필수

## Implications for Roadmap

4개 문서 모두 **동일한 스파인**에 수렴한다: (0) 기반 결정/가드레일 → (1) Java 0줄 미디어 수직 슬라이스로 최고 위험 선제 해소 → (2) 도메인 골격 → (3) 비개인화 BFF(추후 폴백이 됨) → (4) 개인화+복원력 데모 → (5) 검색/마이리스트 → (6) 인제스트 제품화 → (7) 실험/관측 하드닝. 아래 제안은 이 스파인을 반영하되, **정확한 phase 개수/경계는 로드맵 작성자가 확정**해야 한다 — 특히 인제스트 제품화 시점에 관해 문서 간 실제 이견이 있다(§Phase Ordering Rationale 참조).

### Phase 0: 기반 + 가드레일
**Rationale:** 3개 문서(STACK/ARCHITECTURE/PITFALLS)가 독립적으로 "verify() 테스트, git 미디어 격리, docker-compose 단일 진실(Flyway), 한글 시드, EOL ADR, 라이선스 스키마는 첫 커밋에 존재해야 한다"고 명시. 사후 도입 비용이 구조적으로 비대칭(위반 1건 vs 40건)이다.
**Delivers:** Spring Modulith 8모듈 빈 골격 + `ApplicationModules.verify()` green, docker-compose(mysql 8.0 `--ngram_token_size=2 --collation-server=utf8mb4_0900_ai_ci`, nginx, prometheus/grafana), 저장소 밖 미디어 루트 + `.gitignore`, Flyway를 스키마 단일 진실로, ADR-0001(EOL 스택 근거), 한글 골든 시드("인터스텔라" 등), `title.attribution/license` 컬럼
**Addresses:** (기능 아님, 전 기능의 전제 조건)
**Avoids:** Pitfall 3(미디어 git 유입), Pitfall 5(Modulith 경계 침식), Pitfall 10 설정 절반(ngram 기동 파라미터), Pitfall 14(docker init 1회성), Pitfall 17(라이선스)

### Phase 1: 미디어 재생 수직 슬라이스 (Java 0줄)
**Rationale:** FEATURES/ARCHITECTURE/PITFALLS 세 문서가 독립적으로 "가장 위험한 것을 가장 먼저"를 지목 — 손으로 ffmpeg 돌린 산출물 하나만 있으면 재생 계약(키프레임 정렬·CORS·Windows 경로·hls.js 라이프사이클) 전체를 관리자 UI·DB·검색 없이 검증할 수 있다. ARCHITECTURE는 이를 "P0.5", PITFALLS는 "P1"이라 부르지만 **내용은 동일한 phase**다.
**Delivers:** Big Buck Bunny/Sintel 1편 수동 트랜스코딩(3렌디션 GOP 정렬 사다리) → nginx 정적 서빙 → 최소 Next.js 페이지 + hls.js 재생 → 15초 재생 후 새로고침 → 그 지점부터 재개. `EXTINF` 정렬 검증 스크립트를 산출물로 커밋.
**Addresses:** HLS 어댑티브 재생, 이어보기 기초(FEATURES table stakes)
**Avoids:** Pitfall 1(리스크 역순), Pitfall 2(키프레임 미정렬), Pitfall 9(Windows 경로/한글 인코딩), Pitfall 12(hls.js SSR/StrictMode)

### Phase 2: catalog + profile 도메인 골격 + 배치 인제스트
**Rationale:** ARCHITECTURE — catalog은 최하층(무의존)이라 먼저, profile은 이후 거의 모든 도메인의 FK 축이라 그 다음. FEATURES — `profile_id`를 계정 단위로 잘못 설계하면 전 테이블 마이그레이션이 되므로 스키마 확정이 선행돼야 함. 인제스트는 "배치 스크립트 버전"(자산 확보용)만 이 phase에 포함 — 제품화(잡 큐 UI)는 후속.
**Delivers:** title/genre/asset 메타 + Top-N 윈도 함수, 프로필 CRUD(키즈 플래그 포함) + 격리, 배치 인제스트 스크립트(FfmpegCommand 어댑터 재사용, 시드 SQL 생성)
**Uses:** MySQL 8.0 윈도 함수, `com.mysql:mysql-connector-j`, Flyway+flyway-mysql
**Implements:** catalog 모듈(`allowedDependencies={}`), profile 모듈

### Phase 3: playback 도메인 (Core Value 폐곡선)
**Rationale:** catalog(발행 여부)·profile(키즈 차단) SPI가 존재해야 정책 판단이 가능. 이 phase에서 P1의 손-스파이크가 실 서버 저장 이어보기로 승격되며 Core Value 루프(목록→재생→이탈→재진입)가 완결된다.
**Delivers:** 재생 티켓 발급 + 북마크 UPSERT + 하트비트(15초 스로틀+`sendBeacon` 이탈 저장) + 완료 임계값(95%) + 인증 방식 확정(세션 쿠키+동일 오리진 rewrites)
**Addresses:** 재생 오류 복구, Continue Watching 진입/제거 규칙
**Avoids:** Pitfall 13(이어보기 엣지 케이스 7종), Pitfall 11(CORS/Range/인증 — 여기서 방식을 정하면 나중 비용 0)

### Phase 4: BFF v1 — 비개인화 lolomo 홈
**Rationale:** ARCHITECTURE와 FEATURES가 동일하게 강조 — 이 phase의 산출물이 Phase 5 서킷브레이커의 **폴백 구현체 그 자체**가 된다. 순서를 뒤집으면 폴백이 사후 스텁이 되어 데모 가치가 사라진다.
**Delivers:** `/api/home` 1회 응답(Top10 + 장르행 + 히어로), 중복제거·빈 행 억제·최소 길이 규칙, 배치 하이드레이션(쿼리 예산 테스트로 봉쇄)
**Avoids:** Pitfall 6(N+1 — 행 개수와 무관한 쿼리 상한을 테스트로 고정), AP2(클라이언트 행 조립)

### Phase 5: recommendation v1 + Resilience4j 복원력
**Rationale:** playback/profile/catalog SPI + Phase 4의 폴백 대상이 모두 갖춰진 뒤에만 성립. 콜드스타트 폴백과 서킷브레이커 폴백이 같은 코드 경로를 공유하도록 설계(비용 절반).
**Delivers:** RowProvider 레지스트리(이어보기/마이리스트/Top10/장르/"때문에 시청"), 이력 기반 개인화 정렬, `resilience4j-spring-boot3:2.2.0` 서킷브레이커 + Grafana 상태 전이 데모
**Uses:** resilience4j 2.2.0(STACK §2.1, 2.3.0 금지)

### Phase 6: 검색 + 마이리스트
**Rationale:** 둘 다 작다. 마이리스트는 RowProvider 계약의 가장 단순한 구현체라 OCP 실증(기존 파일 수정 0)에 유용. 검색은 카탈로그 데이터가 있어야 의미가 생기므로 이 시점.
**Delivers:** MySQL ngram FULLTEXT(+ 비교용 LIKE 구현·측정 결과 문서화 — PITFALLS Pitfall 10의 "무비판적 채택 금지" 규율), My List CRUD/행
**Research flag:** 한국어 검색 구현 경로는 4개 문서 전부가 [열림]으로 남긴 결정 — 반드시 측정 후 채택

### Phase 7: 인제스트 제품화 (잡 상태머신 + 이벤트 발행)
**Rationale:** ARCHITECTURE는 이 phase를 의도적으로 여기(재생/BFF/추천 완성 후)에 배치 — "인제스트 *모듈*"과 "미디어 *산출물*"을 분리해, 산출물은 Phase 2에서 이미 확보했으므로 재생 개발이 인제스트 완성에 인질 잡히지 않는다는 논리다. **단, 이 배치는 PITFALLS/FEATURES와 정면으로 다르다 — 아래 Phase Ordering Rationale 참조.**
**Delivers:** 관리자 인증, 업로드 등록, ffprobe 검사→ffmpeg 트랜스코드(병렬 렌디션)→패키징→검증 4단계 잡, `leased_until` 기반 reconciler(고아 잡 회수), 재시도 상한(3회)+DLQ, Modulith `@ApplicationModuleListener` 이벤트 발행(내구성 = Event Publication Registry)
**Avoids:** Pitfall 7(고아 잡/재시도 폭풍/디스크 소진), Pitfall 8(stderr 데드락), Pitfall 15(ffmpeg 보안/SSRF)

### Phase 8: 실험 플래그 + 관측/카오스 하드닝
**Rationale:** 실험은 변형시킬 대상(Phase 5의 행 정렬)이 있어야 의미가 생기므로 여기. 관측/카오스는 계측 대상 전체가 존재해야 값어치가 있다.
**Delivers:** 결정론 셀 배정(해시 기반, 저장 없이 재현 가능) + 배정 로그, QoE 계측(기동시간·리버퍼·전환) → Prometheus/Grafana, 컨테이너 kill 카오스 스테이지, Playwright 크로스브라우저(Chrome+Safari) E2E — PITFALLS의 "Looks Done But Isn't" 체크리스트 전체를 회귀 스위트로 코드화

### Phase Ordering Rationale

- **강한 합의**: 재생 수직 슬라이스 최우선(Phase 1), catalog/profile이 recommendation/BFF보다 먼저, 비개인화 BFF가 서킷브레이커 폴백보다 먼저, 실험이 정렬 대상(추천)보다 먼저 — 4개 문서 전원 일치.
- **⚠️ 이견 — 인제스트 제품화 시점**: ARCHITECTURE는 잡 큐/이벤트 발행 등 "인제스트 모듈"을 **의도적으로 P7(후반)**에 배치하며 "산출물 의존 ≠ 모듈 의존"을 근거로 든다. 반면 PITFALLS 자체의 phase 이름 붙이기는 "P2 인제스트/트랜스코딩"으로 **훨씬 이른 시점**을 가리키고, Pitfall 7(고아 잡/재시도/디스크 소진)의 예방책을 "첫 구현에 포함하라, 나중에 넣는 것은 상태 머신 재작성"이라며 이르게 배치할 것을 권고한다. FEATURES의 MVP-7 목록도 "인제스트 제품화"를 Launch-With 8개 항목의 **맨 마지막**에 두면서도 구체적 phase 번호는 매기지 않았다. → **세 문서 모두 "잡 상태머신의 lease/reconciler/재시도 상한은 v1 필수 스코프"라는 데는 동의하지만, "언제 짓는가"는 동의하지 않는다.** 로드맵 작성자가 결정할 축: (a) Phase 2의 배치 스크립트로 Phase 6까지 버틸 실 자산이 충분한가, (b) 콘텐츠 다양성/볼륨 요구가 제품화 인제스트를 앞당기는가.
- **약한 이견 — 검색 배치**: FEATURES는 8개 Launch-With 중 5번째(중반), PITFALLS는 P5(중후반, catalog/profile/playback 이후), ARCHITECTURE는 P6(BFF+recommendation 이후, My List와 묶음) — 방향은 일치(중~후반)하나 정확한 순번은 문서마다 다르다. 로드맵 병합 시 재조정 여지 있음.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 1 (미디어 스파이크):** HIGH — Apple HLS Authoring Spec 원문을 **4개 리서치 문서 전부가 독립적으로 확보 실패**(WebFetch가 본문 미회수)했고, ffmpeg 공식 HLS muxer 문서도 ARCHITECTURE 리서치 중 `ffmpeg.org` ECONNREFUSED로 미확보. GOP/키프레임 정렬 수치, `var_stream_map`, 세그먼트 길이는 스파이크 실측 + 원문 재확보가 병행돼야 한다.
- **Phase 6 (검색):** MEDIUM — 한국어 형태소/초성 검색 경로가 [열림]. Netflix 회원 검색 자체의 1차 원문이 존재하지 않는다(ARCHITECTURE §0.2 확인 — Netflix 패턴 인용 없이 설계할 것). MySQL ngram 공식 문서(PITFALLS가 HIGH로 확보)와 STACK의 2차 한국어 블로그 사이 세부 불일치는 낮지만, "ngram vs LIKE 실측 비교" 자체가 미수행.
- **Phase 7 (인제스트 제품화):** MEDIUM — Modulith 1.1.12의 이벤트 재발행 프로퍼티 정확한 키 이름 미확정(버전 간 변경 이력), `-progress pipe:1`의 `out_time_ms` 단위(ms/μs) 미확인, JobRunr-Boot 3.2 조합(대안 트립와이어) 미검증.

Phases with standard patterns (skip research-phase):
- **Phase 0 (기반):** Spring Modulith/Boot 공식 문서 HIGH confidence로 충분.
- **Phase 2, 3 (catalog/profile/playback 골격):** ARCHITECTURE 스스로 "표준 Spring/JPA 패턴, 본 문서로 충분"이라 명시.
- **Phase 4 (BFF):** N4(Netflix API 재설계) 원문 확보 + N+1 방지가 잘 알려진 Hibernate 패턴.
- **Phase 5 (recommendation+resilience4j):** 버전·설정 키가 이미 STACK.md에 인용과 함께 확정.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | MEDIUM-HIGH | 코어 버전 핀(Boot/Modulith/BOM 관리 라이브러리)은 공식 문서 WebFetch 직접 확인=HIGH. FE/미디어 최신 패치(Next.js 16.2.x, ffmpeg 8.0.3, hls.js 번들 크기)는 2차 출처 기반=MEDIUM. 7개 항목이 명시적 LOW(스캐폴딩 시 실측 필수)로 표기됨 |
| Features | MEDIUM-HIGH | Netflix 공식 Help/TechBlog + arXiv 1903.04638 직접 확인=HIGH 다수. 일부 수치(재생속도 범위, My List 상한, 국내 OTT 비교)는 단일/상충 2차 출처=LOW로 정직 격리. 검색/My List는 Netflix 1차 원문 자체가 없음(미확보로 명시) |
| Architecture | HIGH | Netflix TechBlog 원문 13종 전부 실제 WebFetch 확보 + RFC 8216 + Spring Modulith 공식 문서 + MySQL 공식 문서. "축소 이식 정직 대조표"로 원문 조건 대 자사 스케일 괴리를 매 패턴 명시. 단 검색 설계는 원형 없음(LOW), 미디어 파이프라인 세부는 MEDIUM(ffmpeg 공식 문서 접속 실패) |
| Pitfalls | MEDIUM-HIGH | HLS/ffmpeg 데드락/MySQL ngram/Spring Modulith/hls.js 항목은 1차 출처(공식 docs·이슈 트래커) 확인=HIGH. Apple HLS Authoring Spec은 WebFetch 실패(JS 렌더 페이지)로 2차 출처 기반=MEDIUM |

**Overall confidence:** MEDIUM-HIGH — 코어 스택 버전 핀과 Netflix 아키텍처 패턴 근거는 강하다(1차 출처 다수 확보 + "정직한 축소 이식" 규율 일관 적용). 약한 지점은 전부 동일한 두 축으로 수렴한다: (1) 미디어 파이프라인 수치 디테일(Apple HLS Authoring Spec 4개 문서 공통 미확보, ffmpeg 공식 문서 접속 실패) — Phase 1 스파이크에서 실측/재확보로 닫힘, (2) 한국어 검색 구현 경로(원형 자체가 없고 실측 미수행) — Phase 6에서 측정으로 닫힘.

### Gaps to Address

- **시리즈/에피소드 모델 존재 여부**: FEATURES가 "요구사항 정의 첫 결정 항목으로 권장"이라 명시. 다음 화 자동재생·"아직 시청 중인가요"·에피소드 목록 3개 기능군이 이 결정 하나에 통째로 걸려 있다 — 요구사항 정의 단계에서 즉시 닫을 것. (※ REQUIREMENTS.md에서 v2로 결정됨 — PLAY-V2-04)
- **Apple HLS Authoring Spec 원문**: STACK/FEATURES/ARCHITECTURE/PITFALLS 4개 문서 전부 독립 확보 실패. Phase 1(미디어 스파이크) 착수 시 사람이 직접 열어 세그먼트 길이·사다리 권고 수치를 재확인.
- **한국어 검색 구현 경로**: MySQL ngram 파서 한계(1글자/초성 미매칭)는 PITFALLS가 공식 문서로 확정했으나, "ngram이 이 규모에서 LIKE보다 실제로 나은가"는 측정된 바 없음. Phase 6에서 두 방식 모두 구현 후 비교 측정 결과를 설계 문서에 남길 것(PITFALLS Pitfall 10의 명시 권고).
- **인제스트 제품화 phase 시점**: 위 Phase Ordering Rationale의 이견 — 로드맵 작성 시 명시적으로 결정하고 근거를 기록할 것.
- **Resilience4j/hls.js/Modulith 이벤트의 세부 버전 함정**: resilience4j 2.3.0 기동 실패(이슈 1건 기반, MEDIUM), hls.js 번들 크기 출처 간 모순(70KB vs 254KB), Modulith 이벤트 재발행 프로퍼티 키 이름 — 전부 스캐폴딩/구현 시점 실측으로 닫는 것으로 문서화됨. 로드맵에 "버전 핀 검증" 체크포인트를 P0/P5/P7에 명시적으로 남길 것.
- **미디어 파일명 인코딩(Windows cp949) 및 프로세스 관리**: 하네스 자체 메모리(`reference_harness_script_stdout_utf8_cp949`)와 동형 사례로 PITFALLS가 이미 교차 확인 — Phase 2/7에 ASCII-safe 파일명 정책과 UTF-8 인코딩 명시가 필수.

## Sources

### Primary (HIGH confidence)
- Netflix TechBlog 원문 13종 (N1~N13, WebFetch 실확보) — lolomo/BFF/서킷브레이커/이어보기/인제스트 파이프라인/실험/카오스/관측 원형. ARCHITECTURE.md §0.1
- Netflix Open Connect 공식 사이트(N14), Netflix Help 공식 페이지(프로필·이어보기·자동재생·My List) — FEATURES.md §Sources
- arXiv 1903.04638 (Netflix 검색 챌린지 논문) — 키스트로크 검색, 짧은 쿼리, 카탈로그 외 검색 13%
- RFC 8216 (HLS), Spring Modulith 공식 레퍼런스(fundamentals/events/verification), MySQL 8.0 공식 문서(Locking Reads/ngram/Fulltext Restrictions), hls.js API/README
- Context7 `/spring-projects/spring-modulith`, Resilience4j Context7
- docs.spring.io Boot 3.2.12 dependency-versions/gradle-plugin, search.maven.org(Modulith BOM), registry.npmjs.org(hls.js)

### Secondary (MEDIUM confidence)
- herodevs/danvega.dev(Boot EOL), endoflife.date(MySQL/Node), nextjs.org 공식 블로그(Next 16), ffmpeg.org 다운로드 페이지
- Mux 아티클(HLS 태그·진행률 저장·multi-DRM), AWS MediaConvert/MediaPackage 공식 문서, JobRunr 공식 문서
- GitHub 이슈 트래커(resilience4j #2265, flyway #3885, immich #27973, hls.js #4556/#1119, spring-modulith #395/#926/#971)
- Apple WWDC 2016 세션 510 PDF, Vlad Mihalcea 블로그(JPA N+1/HHH000104), DZone(ProcessBuilder 데드락)

### Tertiary (LOW confidence)
- 한국어 ngram 해설 블로그(hoing.io 등) — MySQL 공식 문서로 재확인됨(PITFALLS), STACK은 여전히 MEDIUM-LOW로 별도 표기
- Netflix 재생속도 범위(0.5–1.5x)·My List 상한(500 vs 2000, 출처 간 상충)·플랜별 동시 스트림 수 — 2차 매체만
- 나무위키 국내 OTT 비교 문서 — 편집형 위키, 검증 불가로 명시
- Apple HLS Authoring Specification, Netflix 회원 검색 1차 원문, Netflix My List 1차 원문, Netflix DASH 1차 출처, ChAP 원문 — **4개 문서 공통 미확보, 인용하지 않음**

---
*Research completed: 2026-08-06*
*Ready for roadmap: yes*
