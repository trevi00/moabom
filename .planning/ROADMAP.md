# Roadmap: 모아봄 (moabom)

## Overview

M1(MVP 7종)은 **위험 역순 배치를 구조로 금지**하는 순서로 간다. 먼저 한 커맨드 기동 + 모듈 경계 + 미디어 격리 가드레일을 세우고(P1), 애플리케이션 Java 0줄로 실 HLS 자산을 브라우저에서 재생·재개시켜 최고 위험(키프레임 정렬·CORS·Windows 경로·hls.js 라이프사이클)을 최전방에서 태운다(P2). 그 위에 계정/프로필 격리 축과 발행 상태를 가진 카탈로그를 얹고(P3), 손-스파이크를 서버 저장 이어보기로 승격해 Core Value 루프(브라우즈→재생→이탈→재진입)를 닫는다(P4). 다음이 BFF가 완성된 lolomo JSON을 반환하는 비개인화 행 홈(P5) — 이 산출물이 그대로 P6 서킷브레이커의 폴백 구현체가 되므로 순서를 뒤집을 수 없다. 개인화 추천 + 복원력 + 관측(P6), 검색 + 마이리스트(P7)로 행 세트 6종을 완성하고, 마지막에 인제스트를 제품화한다(P8) — 실 자산은 이미 P2에서 확보돼 있어 재생 개발이 인제스트 완성에 인질 잡히지 않는다.

## Phases

**Phase Numbering:**
- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [ ] **Phase 1: 기반 + 가드레일** - 한 커맨드 기동 + Modulith 경계 CI 게이트 + 스키마/미디어 격리 규율
- [ ] **Phase 2: 미디어 재생 수직 슬라이스** - 앱 Java 0줄로 HLS ABR 재생 + 새로고침 재개 실증 (최고 위험 선제 해소)
- [ ] **Phase 3: 카탈로그 + 계정/프로필** - 가구 1계정 N프로필 격리 축 + 발행 상태를 가진 카탈로그
- [ ] **Phase 4: playback 도메인** - 서버 저장 이어보기 + 완료 임계값 + 자막 + 오류 복구 (Core Value 폐곡선)
- [ ] **Phase 5: BFF v1 + 행 기반 홈** - 서버가 완성된 lolomo JSON 1회 응답, 넷플릭스 문법 행 홈 UI
- [ ] **Phase 6: 추천 v1 + 복원력 + 관측** - 이력 기반 개인화 행 + 추천 실패 시 비개인화 폴백 + Grafana
- [ ] **Phase 7: 검색 + 마이리스트** - 즉시 검색(ngram vs LIKE 실측) + 마이리스트 3진입점 + 행 세트 6종 완성
- [ ] **Phase 8: 인제스트 제품화** - 관리자 잡 파이프라인 + lease/reconciler/재시도 상한 + 산출물 검증

## Ordering Decisions

리서치 SUMMARY.md가 명시적으로 로드맵 작성자에게 넘긴 두 이견을 여기서 확정한다.

### D1 — 인제스트 제품화 시점: **후반(Phase 8) 유지, 단 스코프는 PITFALLS 기준**

**이견 요약:** ARCHITECTURE는 잡 큐/이벤트 발행을 의도적으로 P7(후반)에 배치("산출물 의존 ≠ 모듈 의존"). PITFALLS는 "P2 인제스트/트랜스코딩"으로 훨씬 이른 시점을 가리키며, Pitfall 7(고아 잡/재시도 폭풍/디스크 소진) 예방책을 "첫 구현에 포함하라, 나중에 넣는 건 상태 머신 재작성"이라 경고. FEATURES는 Launch-With 8개 중 맨 마지막에 두되 번호 미부여. 세 문서 모두 **lease/reconciler/재시도 상한이 v1 필수 스코프**라는 데는 동의.

**결정: 배치는 ARCHITECTURE(후반), 스코프는 PITFALLS(첫 구현 완전판).** 두 문서의 이견은 사실 서로 다른 축을 말하고 있었다 — ARCHITECTURE는 *언제*, PITFALLS는 *무엇을 빼먹지 말라*. PITFALLS의 실제 우려("나중에 넣으면 상태 머신 재작성")는 **일정이 아니라 스코프 완전성**으로 닫히므로, Phase 8은 잡 상태머신을 처음 쓸 때부터 lease 기반 reconciler + 재시도 상한 3회 + DLQ + 산출물 검증을 함께 넣는다. 사후 증분 패치 금지.

**SUMMARY이 제시한 결정 축에 대한 답:**
- *(a) Phase 2의 배치 스크립트로 Phase 7까지 버틸 실 자산이 충분한가* → **그렇다.** 콘텐츠 코퍼스가 Blender 오픈무비로 고정(저작권 제약상 확장 불가)이라 총량이 10~20편 수준으로 상한이 있다. Phase 2의 배치 스크립트가 같은 사다리로 일괄 산출할 수 있으므로, 자산 확보에 관리자 UI가 필요한 순간이 v1 안에 오지 않는다.
- *(b) 콘텐츠 다양성/볼륨 요구가 제품화를 앞당기는가* → **아니다.** 볼륨 압력이 실제로 오는 지점은 Phase 5의 행 최소 길이 규칙(BROW-07)과 Phase 7의 검색 코퍼스인데, 둘 다 "타이틀 수 늘리기"로 풀리고 그건 배치 스크립트 재실행이다. 같은 ffmpeg 어댑터/사다리를 쓰므로 버려지는 작업이 없다.

**받아들인 리스크 (정직 표기):** 인제스트 모듈이 마지막에 오므로, Modulith `@ApplicationModuleListener` 이벤트 계약(`TitleAssetPublished`)의 실 배선이 Phase 8까지 미검증으로 남는다. 완화 = Phase 1에서 8모듈 골격과 `ApplicationModules.verify()`를 켜 두어 경계 자체는 처음부터 강제, Phase 3의 발행 상태머신(INGST-12)을 catalog 소유로 먼저 세워 Phase 8은 상태 전이의 *구동자*만 붙이게 함. 잔여 리스크는 0이 아니다 — 이벤트 재발행 프로퍼티 키 미확정(SUMMARY Research Flag: Phase 7 MEDIUM)이 Phase 8 착수 시 실측 대상.

### D2 — 검색 배치: **Phase 7 (추천 이후, 마이리스트와 묶음)**

**이견 요약:** FEATURES는 중반(Launch-With 5번째), PITFALLS는 P5(중후반), ARCHITECTURE는 P6(BFF+recommendation 이후, My List와 묶음). 방향은 일치(중~후반), 순번만 다름.

**결정: ARCHITECTURE 배치 채택(가장 늦은 쪽).** 근거 셋:
1. **OCP 실증이 순서에 걸려 있다.** LIST-03(홈 My List 행)은 lolomo 행이고, RECO-01의 "새 행 유형 = 파일 추가 + 등록 1줄, 기존 파일 수정 0"을 증명할 가장 단순한 구현체다. 레지스트리(Phase 5)와 개인화 행(Phase 6)이 선행해야 이 증명이 성립한다.
2. **검색 랭킹이 인기도 집계를 재사용한다.** SRCH-05(어휘 매칭 우선 + 인기도 보조)는 RECO-04의 Top-N 집계 경로를 그대로 쓴다. 앞당기면 집계 경로를 두 번 만든다.
3. **ngram vs LIKE 실측(SRCH-02)은 코퍼스가 클수록 신뢰도가 높다.** Phase 5/6에서 행 규칙 충족을 위해 타이틀 수가 최대가 된 뒤 측정하는 편이 결론이 덜 흔들린다.

**받아들인 리스크:** 검색은 사용자 기준 table stakes인데 v1 후반까지 없다. 완화 = 발견 경로는 Phase 5의 행 홈 + 상세 모달이 이미 커버하므로 Core Value 루프가 검색에 막히지 않는다. 다만 "검색 없는 데모"가 Phase 5~6 구간에 존재한다는 사실은 그대로 인정한다.

## Phase Details

### Phase 1: 기반 + 가드레일
**Goal**: 전 스택이 한 커맨드로 기동하고, 모듈 경계·스키마 단일 진실·미디어 격리가 첫 커밋부터 자동으로 강제된다
**Depends on**: Nothing (first phase)
**Requirements**: ARCH-01, ARCH-05
**Success Criteria** (what must be TRUE):
  1. 앱 이미지 선행 빌드 1회(`cd backend && ./gradlew bootBuildImage --imageName=moabom-app:local`) 후 `docker compose --profile full up` **한 커맨드**로 mysql·nginx·prometheus·grafana + 앱 + FE 가 기동하고 `curl http://localhost:3000/api/health` 가 200 을 반환한다 — compose 는 Gradle 을 실행할 수 없으므로 "한 커맨드"는 빌드 스텝 이후를 뜻한다 (RESEARCH CP-3 A안, D-01·D-03 유지)
  2. Modulith 8모듈(catalog/playback/profile/recommendation/experiment/ingest + bff + shared) 빈 골격에서 `ApplicationModules.verify()`가 green이고, 경계를 위반한 커밋은 CI에서 실패한다 (반사실 프로브 V-SC2d 로 증명 — 게이트의 존재는 게이트의 작동이 아니다)
  3. 빈 DB에 Flyway 마이그레이션만 돌려도 스키마 + 한글 골든 시드가 재현되고, title 테이블에 attribution/license 컬럼이 존재한다
  4. 미디어 파일이 저장소 히스토리에 들어가지 못한다 — 미디어 루트가 저장소 밖(`D:/moabom-media/`)이고 `.gitignore`·pre-commit 훅·CI 히스토리 스캔이 **첫 push 이전**에 존재한다 (planning 커밋 5개가 선행하므로 "첫 커밋 이전"은 사후 만족 불가 — 정직 재서술). 차단 여부는 반사실 프로브 V-SC4d 로 증명한다
**Plans**: 8 plans (5 waves)
- [ ] 01-01-PLAN.md — 저장소 가드레일: .gitignore(CP-1 준수)/.gitattributes/.editorconfig/pre-commit 훅/README + 저장소 밖 미디어 루트 [wave 1]
- [ ] 01-02-PLAN.md — backend Gradle 8.14.5 스캐폴딩 + Modulith 8모듈 골격 + ModularityTests(verify) [wave 1]
- [ ] 01-03-PLAN.md — frontend Next 16.2.12 얇은 골격 + rewrites 단일 진입 오리진 + FE Dockerfile [wave 1]
- [ ] 01-04-PLAN.md — Flyway 스키마 단일 진실(V1 title + V2 한글 골든/CC-BY 시드) + SchemaMigrationTest [wave 2]
- [ ] 01-05-PLAN.md — compose profiles 이원화 + mysql 기동 파라미터(G3) + nginx/prometheus 설정 [wave 2]
- [ ] 01-06-PLAN.md — ADR-0001(EOL/CVE 정책) + ADR-0002(미디어·인코딩·D-14) + CI 코어 게이트 + Dependabot [wave 3]
- [ ] 01-07-PLAN.md — 반사실 프로브 2건(V-SC2d/V-SC4d) + full 프로필 실기동 + 가정 A2/A3/A5/A6 해소 + 사람 검수 [wave 4]
- [ ] 01-08-PLAN.md — master→main 전환 + GitHub public repo 생성/push + CI 첫 런 게이트 도달성 확인 [wave 5]

**비-요구사항 산출물 (가드레일 — plan-phase에서 누락 금지):** ADR-0001(Boot 3.2.12 EOL 근거 + 라인 최종 패치 고정 + non-blocking CVE 스캔), resilience4j 2.2.0 버전 핀 + 옆 주석 + `dependencyInsight` 회귀 게이트(2.3.0 금지), mysql 기동 파라미터(`--ngram_token_size=2 --collation-server=utf8mb4_0900_ai_ci`), Windows 파일명 ASCII-safe 정책 + UTF-8 명시.
**Research**: skip — Spring Modulith/Boot 공식 문서 HIGH confidence로 충분 (SUMMARY Research Flags).

### Phase 2: 미디어 재생 수직 슬라이스 (앱 Java 0줄)
**Goal**: 손으로 만든 HLS 자산이 브라우저에서 어댑티브 재생되고 새로고침 후 그 지점부터 재개된다 — 프로젝트 최고 위험을 DB·관리자 UI·검색 없이 선제 해소
**Depends on**: Phase 1
**Requirements**: PLAY-01, PLAY-02, ARCH-03, INGST-01
**Success Criteria** (what must be TRUE):
  1. 사용자가 최소 페이지에서 오픈무비 1편을 재생하고, 대역폭을 조이면 1080→720→480 렌디션 전환이 화면 끊김 없이 일어난다
  2. 같은 URL이 Chrome(hls.js)과 Safari(네이티브 HLS 폴백) 양쪽에서 재생된다
  3. 15초 재생 후 새로고침하면 그 지점부터 재개된다 (이 phase는 클라 저장 — 서버 저장 승격은 Phase 4)
  4. `EXTINF` 정렬 검증 스크립트가 3렌디션 세그먼트 경계 일치를 자동 보고한다 (사람 눈 검사 아님 — `mediastreamvalidator`는 macOS 전용이라 이 프로젝트에서 못 쓴다)
  5. 배치 인제스트 스크립트로 오픈무비 N편을 동일 사다리로 일괄 산출할 수 있다 (Phase 3 이후 전 phase가 쓸 실 자산 확보)
**Plans**: TBD
**UI hint**: yes (최소 재생 페이지 — 행 UI가 아니므로 목업 sign-off 대상 아님, 기능 스파이크)

**INGST-01 배치 근거:** REQUIREMENTS가 "전 페이즈 중 가장 이른 산출물"로 못박았고, 손-트랜스코딩에서 확정한 GOP/사다리/EXTINF 규약을 다음 phase로 넘기면 레시피가 드리프트한다. 스크립트가 카탈로그로 무엇을 넘기는 형식(seed SQL vs manifest)은 Phase 3 계획 결정 사항이며, 이 phase가 소유하는 것은 "N편이 검증된 사다리로 산출된다"까지다.
**Research**: HIGH — Apple HLS Authoring Spec 원문을 4개 리서치 문서 전부가 확보 실패(WebFetch 미회수). ffmpeg 공식 HLS muxer 문서도 미확보. **사람이 직접 열어 세그먼트 길이·사다리 권고 수치를 재확인하는 태스크가 이 phase 착수 조건이다.** GOP/키프레임 정렬 수치와 `var_stream_map`은 스파이크 실측과 병행.

### Phase 3: 카탈로그 + 계정/프로필 도메인
**Goal**: 가구 1계정 N프로필 격리 축과 발행 상태를 가진 카탈로그가 자리 잡고, Phase 2의 실 자산이 카탈로그로 시딩된다
**Depends on**: Phase 1, Phase 2
**Requirements**: AUTH-01, AUTH-02, PROF-01, PROF-02, PROF-03, PROF-04, PROF-05, INGST-12
**Success Criteria** (what must be TRUE):
  1. 사용자가 이메일/비밀번호로 가입·로그인·로그아웃하고, 브라우저를 새로고침해도 세션이 유지된다 (세션 쿠키 + 동일 오리진 — Safari 네이티브 HLS가 커스텀 헤더를 못 붙이므로 구조적 강제)
  2. 로그인 후 프로필 선택 화면에서 프로필을 고르고 사용 중 전환할 수 있으며, 계정당 상한 5개까지 생성/수정/삭제하고 아바타를 프리셋에서 고른다
  3. 키즈 프로필로 들어가면 성인 태그 콘텐츠가 목록에 나타나지 않고 홈이 단순화된다
  4. 프로필 A에서 만든 데이터(시청 이력·마이리스트·추천)가 프로필 B에서 보이지 않는다
  5. DRAFT/READY/UNPUBLISHED 상태 타이틀은 카탈로그 조회 결과에 절대 나타나지 않고 PUBLISHED만 노출된다
**Plans**: TBD
**UI hint**: yes (가입/로그인 폼, 프로필 선택 게이트 화면, 아바타 피커)

**INGST-12 배치 근거:** ARCHITECTURE 원칙상 발행 결정권은 catalog 소유이고 ingest는 이벤트만 발행한다. 카탈로그가 생기는 순간부터 "미완성 자산 유출 방지"가 필요하므로 상태머신은 여기서 태어나고, Phase 8의 인제스트는 이 상태 전이의 *구동자*로 붙는다.
**Research**: skip — ARCHITECTURE가 "표준 Spring/JPA 패턴, 본 문서로 충분"이라 명시.

### Phase 4: playback 도메인 (Core Value 폐곡선)
**Goal**: 시청 위치가 서버에 저장되어 어느 기기·어느 세션에서도 보던 데서 이어보고, 재생 실패가 검은 화면으로 끝나지 않는다
**Depends on**: Phase 3
**Requirements**: PLAY-03, PLAY-04, PLAY-05, PLAY-06, PLAY-07, PLAY-08, INGST-10
**Success Criteria** (what must be TRUE):
  1. 15초 시청 후 창을 닫고 다른 브라우저에서 같은 프로필로 들어가면 그 지점부터 재개된다 (클라 스로틀 + 일시정지/이탈 flush + 서버 UPSERT)
  2. 완료 임계값까지 본 타이틀은 완료 처리되어 이어보기 목록에서 사라진다
  3. 재생을 시작한 타이틀은 즉시 이어보기 목록에 들어가고, 사용자가 수동 제거하면 사라진다
  4. 사용자가 자막을 켜고 끄고 트랙을 바꿀 수 있다 (SRT→WebVTT 변환 산출물이 마스터 플레이리스트에 연결됨)
  5. 네트워크 오류를 주입해도 플레이어가 재시도/레벨 강등으로 복구하고 검은 화면에 머무르지 않는다
**Plans**: TBD
**UI hint**: yes (플레이어 컨트롤, 자막 토글/트랙 선택, 이어보기 제거 액션)

**INGST-10 배치 근거:** 자막 생산자(SRT→WebVTT + 마스터 플레이리스트 연결)와 소비자(PLAY-07 트랙 선택)를 같은 phase에 둔다 — 분리하면 소비자 없는 산출물 또는 산출물 없는 UI가 생긴다 (producer↔consumer coherence).
**Research**: skip — 표준 패턴. 단 Pitfall 13(이어보기 엣지 케이스 7종)과 Pitfall 11(CORS/Range/인증)은 이 phase에서 방식을 확정해야 나중 비용이 0이 된다.

### Phase 5: BFF v1 + 행(row) 기반 홈
**Goal**: 서버가 완성된 lolomo JSON 하나를 반환하고, 사용자가 넷플릭스 문법의 행 홈에서 콘텐츠를 골라 상세로 진입한다
**Depends on**: Phase 4
**Requirements**: BROW-01, BROW-02, BROW-03, BROW-04, BROW-05, BROW-06, BROW-07, BROW-08, RECO-01, RECO-04, RECO-05, LGL-01
**Success Criteria** (what must be TRUE):
  1. 홈 진입 시 `/api/home` 단일 응답만으로 화면이 그려진다 — 클라이언트 행 조립 0, 쿼리 수는 행 개수와 무관하게 상한이 테스트로 고정된다
  2. 홈에 최소 4종 행(이어보기·Top10 순위 배지·장르·신규)과 규칙 기반 히어로/빌보드가 표시되고, 새 행 유형은 RowGenerator 구현 파일 추가 + 등록 1줄로 붙는다 (기존 파일 수정 0)
  3. 행이 수평 스크롤되고 다음 페이지가 지연 로드되며, 중복 타이틀·빈 행·최소 길이 미달 행·다 본 타이틀이 조립 단계에서 걸러진다
  4. 카드 호버 시 확장되고 클릭하면 상세 모달이 열리며, URL이 바뀌어 새로고침·공유·뒤로가기가 동작한다
  5. 모든 타이틀 화면에 출처·라이선스(CC-BY 저작자) 표기가 노출된다
**Plans**: TBD
**UI hint**: yes — **목업-우선 리뷰 대상 (PROJECT.md 진행 방식 [확정]).** 행 UI 목업 → 사람 sign-off 후 구현. `/kha-spec-ui-phase` 착수 권장 지점.

**순서 불변식 (뒤집기 금지):** 이 phase의 비개인화 조립기가 Phase 6 서킷브레이커의 **폴백 구현체 그 자체**가 된다. 순서를 뒤집으면 폴백이 사후 스텁이 되어 데모 가치가 사라진다 (ARCHITECTURE·FEATURES 공통 강조).
**BROW-02 배치 근거:** BROW-02는 행 타입 *계약*의 소유 요구사항이다. 비개인화 4종이 여기서 라이브되고, 개인화(Phase 6)·마이리스트(Phase 7)가 같은 계약에 플러그인으로 합류해 Phase 7 종료 시 6종이 완성된다. umbrella 요구사항을 마지막 phase로 미루지 않기 위한 판단.
**Research**: skip — N4(Netflix API 재설계) 원문 확보 + N+1 방지는 잘 알려진 Hibernate 패턴.

### Phase 6: 추천 v1 + 복원력 + 관측
**Goal**: 홈이 프로필의 시청 이력에 반응해 개인화되고, 추천이 죽어도 홈은 비개인화 행으로 계속 뜬다
**Depends on**: Phase 5
**Requirements**: RECO-02, RECO-03, RECO-06, RECO-07, ARCH-02, ARCH-04
**Success Criteria** (what must be TRUE):
  1. 특정 장르를 시청한 프로필의 홈에 그 장르 행(최근 시청 장르 Top-K)과 "OO를 시청했기 때문에" 행(장르/태그 자카드 유사도 — CF 아님)이 나타난다
  2. 각 행에 근거(evidence) 문구가 붙고, 행 순서가 점수에 따라 바뀌며, 이미 본 타이틀은 개인화 행에서 제외된다
  3. 이력이 0인 새 프로필도 비어 있지 않은 홈(인기·신규·장르 대표)을 받는다 — 이 콜드스타트 폴백은 서킷브레이커 폴백과 **같은 코드 경로**다
  4. 추천 경로를 강제로 실패시키면 홈이 500 없이 비개인화 행으로 응답한다 (in-JVM 호출이라 스레드 격리가 아님을 설계 문서에 정직 표기)
  5. Actuator 지표가 Prometheus로 수집되고 Grafana 대시보드에서 서킷 CLOSED→OPEN 전이와 앱 지표를 볼 수 있다
**Plans**: TBD
**UI hint**: yes (얇음 — Phase 5 행 목업 재사용, 근거 문구 카피와 행 순서 변화만 신규)

**Phase 8(원 P8) 흡수 기록:** 리서치의 Phase 8(실험 + 관측/카오스 하드닝) 중 v1 요구사항으로 남은 것은 ARCH-04(관측) 하나뿐이다 — 실험 플래그(EXP-V2-01), 서킷브레이커 실측 데모(EXP-V2-02), 카오스(EXP-V2-03), QoE 계측(PLAY-V2-02)이 모두 v2로 결정됐다. 관측은 계측 대상(서킷 전이)이 생기는 이 phase로 흡수했다. "실험이 정렬 대상(추천) 이후"라는 4문서 합의는 v2에서도 유효 — EXP-V2-01은 Phase 6 이후에만 배치 가능하다.
**Research**: skip — resilience4j 버전·설정 키가 STACK.md에 인용과 함께 확정. 단 **버전 핀 검증 체크포인트**(resilience4j 2.2.0 고정, 2.3.0 기동 실패)를 이 phase 착수 시 실측.

### Phase 7: 검색 + 마이리스트
**Goal**: 사용자가 원하는 타이틀을 검색으로 바로 찾고, 마이리스트에 담아 홈 행에서 다시 만난다 (행 세트 6종 완성)
**Depends on**: Phase 6
**Requirements**: SRCH-01, SRCH-02, SRCH-03, SRCH-04, SRCH-05, LIST-01, LIST-02, LIST-03
**Success Criteria** (what must be TRUE):
  1. 검색창에 입력하면 150–250ms 디바운스 후 결과가 즉시 갱신되고, 2글자 한국어 부분 일치가 동작한다
  2. 제목(원제 + 한국어)과 장르로 검색되며, 결과가 어휘 매칭 우선 + 인기도 보조로 정렬된다
  3. 결과가 없으면 빈 화면 대신 대체 추천 착지 화면이 표시된다
  4. 카드 호버·상세 모달·플레이어 3진입점 어디서든 마이리스트에 담고 뺄 수 있고, 같은 타이틀이 중복 추가되지 않는다
  5. 홈에 My List 행이 추가 역순으로 표시되고 비어 있으면 숨겨진다 — 이로써 행 세트 6종(이어보기·마이리스트·Top10·장르·신규·개인화)이 완성된다
**Plans**: TBD
**UI hint**: yes (검색 입력/결과 그리드, 무결과 착지 화면, 3진입점 담기 버튼, My List 행)

**Research**: MEDIUM — 한국어 검색 구현 경로가 4개 문서 전부 [열림]. Netflix 회원 검색의 1차 원문이 존재하지 않으므로 **Netflix 패턴 인용 없이 설계할 것**. MySQL ngram 파서의 1글자/초성 미매칭 한계는 공식 문서로 확정됨. **규율: ngram과 LIKE 두 방식을 모두 구현해 측정한 뒤 채택하고, 비교 결과를 설계 문서에 남긴다 — 가설 채택 금지(PITFALLS Pitfall 10 명시 권고).**

### Phase 8: 인제스트 제품화
**Goal**: 관리자가 원본 한 편을 등록하면 검사→사다리 결정→트랜스코딩→패키징→검증→퍼블리시가 자동으로 돌고, 프로세스가 죽어도 잡이 좀비로 남지 않는다
**Depends on**: Phase 3 (발행 상태머신), Phase 5 (아트워크/메타데이터 반영 대상)
**Requirements**: AUTH-03, INGST-02, INGST-03, INGST-04, INGST-05, INGST-06, INGST-07, INGST-08, INGST-09, INGST-11, INGST-13
**Success Criteria** (what must be TRUE):
  1. 관리자 역할만 인제스트 화면·API에 접근할 수 있고 일반 사용자는 차단된다
  2. 관리자가 원본(서버 로컬 경로)을 등록하면 ffprobe 검사(부적합 시 조기 실패) → 사다리 결정(480p 소스에 1080p 생성 금지) → ffmpeg 트랜스코드 → HLS 패키징이 QUEUED→RUNNING 상태와 진행률과 함께 자동 진행된다
  3. 산출물 검증(플레이리스트 파싱·세그먼트 존재·길이 일치)을 통과한 잡만 COMPLETED가 된다 — "COMPLETED인데 재생 불가"가 발생하지 않는다
  4. 잡 실행 중 앱을 강제 종료했다가 재기동하면 lease 만료 잡이 회수되어 재시도되고, 재시도 상한(3회) 초과 시 격리되며, 부분 산출물이 정리된다
  5. 관리자가 아트워크(포스터/와이드)와 메타데이터(제목·시놉시스·장르·연도·러닝타임)를 등록/편집하면 홈 행과 상세에 반영되고, 고아 산출물이 정리 작업으로 회수된다
**Plans**: TBD
**UI hint**: yes (관리자 콘솔 — 등록 폼, 잡 목록/진행률, 메타데이터 편집기. 행 UI 목업 규율과는 별개 트랙)

**스코프 불변식 (D1 결정):** 잡 상태머신은 **첫 구현에 lease 기반 reconciler + 재시도 상한 + DLQ + 산출물 검증을 포함**한다. 나중에 얹는 것은 상태머신 재작성이다(PITFALLS Pitfall 7). 증분 패치로 나눠 넣지 말 것.
**Research**: MEDIUM — Modulith 1.1.12 이벤트 재발행 프로퍼티 키 이름 미확정(버전 간 변경 이력), `-progress pipe:1`의 `out_time_ms` 단위(ms/μs) 미확인, JobRunr–Boot 3.2 조합(대안 트립와이어) 미검증. **필수 예방책:** ffmpeg stderr 미배수 데드락(`redirectErrorStream` + 별도 스레드 드레인 + `waitFor(timeout)`), ffmpeg 인자 SSRF/주입 방어, Windows 파일명 cp949 인코딩 — 전부 짧은 클립에서는 재현 안 되고 장편에서 확실히 걸린다.

## Coverage

| Category | Count | Phases |
|----------|-------|--------|
| AUTH | 3 | 3 (×2), 8 (×1) |
| PROF | 5 | 3 |
| BROW | 8 | 5 |
| PLAY | 8 | 2 (×2), 4 (×6) |
| SRCH | 5 | 7 |
| LIST | 3 | 7 |
| RECO | 7 | 5 (×3), 6 (×4) |
| INGST | 13 | 2 (×1), 3 (×1), 4 (×1), 8 (×10) |
| ARCH | 5 | 1 (×2), 2 (×1), 6 (×2) |
| LGL | 1 | 5 |
| **Total** | **58** | **8 phases, 0 orphans, 0 duplicates** |

⚠️ **요구사항 개수 정정:** REQUIREMENTS.md 헤더가 "53 total"이라 적었으나 실측 체크박스는 **58개**다 (`grep -c '^- \[ \] \*\*'` = 58). 로드맵은 실측 58개 전부를 매핑했다. REQUIREMENTS.md의 Coverage 블록을 58로 정정했다.

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5 → 6 → 7 → 8

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. 기반 + 가드레일 | 0/8 | Planned | - |
| 2. 미디어 재생 수직 슬라이스 | 0/TBD | Not started | - |
| 3. 카탈로그 + 계정/프로필 | 0/TBD | Not started | - |
| 4. playback 도메인 | 0/TBD | Not started | - |
| 5. BFF v1 + 행 기반 홈 | 0/TBD | Not started | - |
| 6. 추천 v1 + 복원력 + 관측 | 0/TBD | Not started | - |
| 7. 검색 + 마이리스트 | 0/TBD | Not started | - |
| 8. 인제스트 제품화 | 0/TBD | Not started | - |

---
*Roadmap created: 2026-08-06 (granularity: standard, mode: yolo)*
*Source: PROJECT.md + REQUIREMENTS.md(58 v1) + research/SUMMARY.md(9-phase spine, 압축 8)*
