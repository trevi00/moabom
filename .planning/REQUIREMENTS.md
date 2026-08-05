# Requirements: 모아봄 (moabom)

**Defined:** 2026-08-06
**Core Value:** 행(row) 기반 홈에서 콘텐츠를 골라 HLS 어댑티브 스트리밍으로 끊김 없이 재생하고, 보던 위치에서 이어본다

> 근거: PROJECT-BRIEF.md (승인 2026-08-05) + `.planning/research/FEATURES.md` MVP 7종 하위 기능 분해 (IN 판정 채택).
> 상세 US/AC 명세는 각 phase 스펙에서 **spec-notation v1** (debate-1785889297)로 작성한다 — 본 문서는 요구사항 레지스터.

## v1 Requirements

Requirements for initial release (M1 = MVP 7종 전체, 2026-08-05 승인). Each maps to roadmap phases.

### 계정·세션 (AUTH)

- [ ] **AUTH-01**: 사용자는 이메일/비밀번호로 계정을 만들 수 있다
- [ ] **AUTH-02**: 사용자는 로그인/로그아웃할 수 있고 세션이 브라우저 새로고침 후에도 유지된다 — **쿠키 기반** (Safari 네이티브 HLS가 커스텀 헤더를 못 붙이므로 쿠키/동일오리진이 강제됨; PITFALLS 근거)
- [ ] **AUTH-03**: 관리자 역할이 분리되어 있고 인제스트 기능은 관리자만 접근할 수 있다

### 프로필 (PROF)

- [ ] **PROF-01**: 계정당 프로필을 상한 N개(기본 5)까지 생성/수정/삭제할 수 있다
- [ ] **PROF-02**: 로그인 후 프로필 선택 화면에서 프로필을 고르고, 사용 중 전환할 수 있다
- [ ] **PROF-03**: 시청 이력·마이리스트·추천이 프로필별로 격리된다
- [ ] **PROF-04**: 키즈 프로필 — 플래그 + 단순화된 홈 + 성인 태그 콘텐츠 배제 (등급 엔진은 v2)
- [ ] **PROF-05**: 프로필 아바타를 프리셋에서 선택할 수 있다

### 카탈로그 브라우즈 (BROW)

- [ ] **BROW-01**: 행(row) 기반 홈이 표시된다 — BFF가 **완성된 lolomo JSON**을 반환하고 클라이언트는 조립하지 않는다
- [ ] **BROW-02**: 행 타입 최소 세트 — 이어보기 / 마이리스트 / Top10 / 장르 / 신규 / 개인화
- [ ] **BROW-03**: 히어로/빌보드 영역이 규칙 기반으로 표시된다
- [ ] **BROW-04**: 행은 수평 스크롤되고 다음 페이지는 지연 로드된다
- [ ] **BROW-05**: 카드 호버 시 확장된다 (정지 이미지; 프리뷰 영상은 v2)
- [ ] **BROW-06**: 타이틀 상세 모달 + deep link URL (뒤로가기/공유 동작)
- [ ] **BROW-07**: 행 조립에 중복 제거·빈 행 억제·행 최소 길이 규칙이 적용된다 (소카탈로그 필수 — FEATURES 근거)
- [ ] **BROW-08**: 다 본 타이틀은 브라우즈 행에서 필터링된다

### 재생 (PLAY)

- [ ] **PLAY-01**: HLS 어댑티브 재생 — 1080/720/480 3렌디션 ABR
- [ ] **PLAY-02**: hls.js 재생 + Safari 네이티브 HLS 폴백
- [ ] **PLAY-03**: 시청 위치가 주기 저장된다 (클라 스로틀 + 일시정지/이탈 시 flush, **서버 저장** — 크로스 디바이스 이어보기가 부수로 성립)
- [ ] **PLAY-04**: 보던 위치에서 이어보기가 재개된다
- [ ] **PLAY-05**: 완료 임계값 — 끝까지 본 타이틀은 완료 처리되어 이어보기 행에서 제거된다
- [ ] **PLAY-06**: 이어보기 행 진입 규칙(재생 즉시) + 행에서 수동 제거
- [ ] **PLAY-07**: 자막 트랙 선택/토글 (WebVTT)
- [ ] **PLAY-08**: 재생 오류 복구 — ERROR 시 재시도/레벨 강등 (검은 화면 방지)

### 검색 (SRCH)

- [ ] **SRCH-01**: 즉시 검색 — 입력 디바운스 150–250ms
- [ ] **SRCH-02**: 짧은 쿼리(2글자) 부분 일치가 동작한다 — 한국어 포함 (**ngram vs LIKE 실측 비교 후 확정**, 가설 채택 금지; PITFALLS 규율)
- [ ] **SRCH-03**: 제목(원제+한국어)·장르로 검색할 수 있다
- [ ] **SRCH-04**: 무결과 시 대체 추천 착지 화면이 표시된다
- [ ] **SRCH-05**: 결과 정렬 — 어휘 매칭 우선 + 인기도 보조

### 마이리스트 (LIST)

- [ ] **LIST-01**: 카드 호버·상세 모달·플레이어 3진입점에서 추가/제거할 수 있다
- [ ] **LIST-02**: 마이리스트는 프로필별이며 중복이 방지된다 (unique(profile_id, title_id))
- [ ] **LIST-03**: 홈에 My List 행이 표시된다 — 추가 역순 정렬, 비어 있으면 숨김

### 추천 v1 (RECO)

- [ ] **RECO-01**: RowGenerator 계약 + 레지스트리 — 새 행 유형은 파일 추가만으로 등록된다 (OCP)
- [ ] **RECO-02**: 최근 시청 장르 Top-K 기반 장르 행이 생성된다
- [ ] **RECO-03**: "OO를 시청했기 때문에" 행 — 장르/태그 자카드 유사도 (**CF 아님을 명시**)
- [ ] **RECO-04**: Top 10 행 — 최근 N일 재생수 집계 + 순위 배지
- [ ] **RECO-05**: 신규 추가 행 — 퍼블리시 시각 정렬
- [ ] **RECO-06**: 콜드스타트 폴백(이력 0 → 인기/신규/장르 대표) — **서킷브레이커 폴백과 동일 코드 경로**로 설계
- [ ] **RECO-07**: 행 근거(evidence) 문구 노출 + 점수 기반 행 순서 + 이미 본 타이틀 제외

### 콘텐츠 인제스트 (INGST)

- [ ] **INGST-01**: 배치 인제스트 스크립트 — 초기 시딩용, **전 페이즈 중 가장 이른 산출물** (실 HLS 자산 확보)
- [ ] **INGST-02**: 관리자가 원본을 등록할 수 있다 — M1은 서버 로컬 경로 지정 (청크/재개 업로드는 v2)
- [ ] **INGST-03**: ffprobe 미디어 검사 — 해상도/코덱/길이/오디오, 부적합 시 조기 실패
- [ ] **INGST-04**: 렌디션 사다리 결정 + 업스케일 금지 (480p 소스에 1080p 생성 금지)
- [ ] **INGST-05**: ffmpeg 트랜스코드 → HLS 세그먼트/플레이리스트 산출
- [ ] **INGST-06**: 잡 상태 머신 (QUEUED→RUNNING→COMPLETED/FAILED/CANCELED) + 진행률 + 타임아웃
- [ ] **INGST-07**: 재시도 멱등성 — 부분 산출물 정리 후 재실행 + 크래시 좀비 잡 reconciler (STACK 근거: 자체 상태머신 선택의 대가)
- [ ] **INGST-08**: 산출물 검증 — 플레이리스트 파싱·세그먼트 존재·길이 일치 ("COMPLETED인데 재생 불가" 방지)
- [ ] **INGST-09**: 아트워크(포스터/와이드) 등록 — 행 UI의 전제
- [ ] **INGST-10**: 자막 등록 SRT→WebVTT 변환 + 마스터 플레이리스트 연결
- [ ] **INGST-11**: 메타데이터 편집 — 제목·시놉시스·장르·연도·러닝타임
- [ ] **INGST-12**: 퍼블리시 상태 머신 (DRAFT→READY→PUBLISHED→UNPUBLISHED) — 미완성 자산의 카탈로그 유출 방지
- [ ] **INGST-13**: 스토리지 레이아웃 규약 + 고아 산출물 정리 (D 드라이브 용량이 실 제약)

### 아키텍처·운영 (ARCH)

- [ ] **ARCH-01**: Spring Modulith 6모듈 경계 (catalog/playback/profile/recommendation/ingest/experiment) + `ApplicationModules.verify()` CI 게이트
- [ ] **ARCH-02**: Resilience4j — 추천 실패 시 비개인화 행 폴백 (in-JVM이라 스레드 격리 아님을 정직 표기; ARCHITECTURE 근거)
- [ ] **ARCH-03**: nginx 정적 HLS 서빙 + 캐시 — m3u8와 세그먼트 캐시 정책 분리
- [ ] **ARCH-04**: 관측 — Actuator + Prometheus + Grafana
- [ ] **ARCH-05**: docker-compose 한 번으로 전 스택 기동 (mysql·nginx·prometheus·grafana)

### 법적·콘텐츠 (LGL)

- [ ] **LGL-01**: 모든 타이틀에 출처·라이선스 표기 노출 (Blender 오픈무비 CC-BY 저작자 표기 의무; 스키마에 attribution/license 컬럼 — PITFALLS P0 근거)

## v2 Requirements

Deferred to future release. Tracked but not in current roadmap.

### 재생 고도화

- **PLAY-V2-01**: 수동 화질 선택 · 재생 속도 · 키보드 단축키 (재생 루프 안정 직후 저비용 항목)
- **PLAY-V2-02**: QoE 이벤트 계측 (기동 시간·리버퍼·전환) + Grafana 대시보드
- **PLAY-V2-03**: 트릭플레이 스크럽 썸네일 (인제스트 산출 단계 추가 선행)
- **PLAY-V2-04**: 시리즈/에피소드 모델 + 다음 화 자동재생 (카탈로그에 시리즈가 생길 때만 — FEATURES 판정)

### 실험·복원력

- **EXP-V2-01**: 실험 플래그 + 배정 로그 + 지표 수집 (행 순서 A/B 최소 구현 — Ablaze 축소)
- **EXP-V2-02**: 서킷브레이커 실측 데모 (부하 하 서킷 오픈 시연 — 추천 v1 가동 후)
- **EXP-V2-03**: Chaos — 파이프라인 신뢰성 테스트 스테이지 (컨테이너 kill)

### 검색·프로필 고도화

- **SRCH-V2-01**: 초성 검색 (ㅇㄷㅂㅇ → 올드보이)
- **SRCH-V2-02**: 인물(감독/출연) 검색 — 인물 테이블 + 인제스트 메타 확장 연동
- **PROF-V2-01**: 프로필 PIN/잠금 · 성숙도 등급 엔진 (프로필별 등급 제한)

### 도메인 확장

- **BIZ-V2-01**: 구독/결제 모의
- **BIZ-V2-02**: 동시 시청 제한
- **RECO-V2-01**: 협업 필터링 추천 (시청 이벤트 축적 또는 합성 이벤트 시딩 설계 후)
- **BROW-V2-01**: 카드 호버 프리뷰 영상 자동재생 (프리뷰 클립 인코딩 자산 선행)

## Out of Scope

Explicitly excluded. Documented to prevent scope creep.

| Feature | Reason |
|---------|--------|
| Netflix 로고/워드마크 | 상표 회피 — 레이아웃 문법만 복제, 워드마크는 "모아봄" |
| 저작권 있는 콘텐츠 | 저작권-프리(Blender 오픈무비)만 — 법적 제약 |
| 다운로드/오프라인 + DRM | 별도 난제군, 포트폴리오 가치 대비 과대 비용 (anti-feature) |
| 물리 마이크로서비스 분리 | Modulith 모듈리스 유지 — 물리 분리는 트래픽이 요구할 때 (트립와이어) |
| 라이브 스트리밍 | 별도 파이프라인, 별도 마일스톤 |
| per-title 인코딩 최적화 (VMAF) | 시도당 수십 회 인코딩 전제 — 우리 스케일에 과함 |
| 다중 리스트/리스트 공유/수동 재정렬 | 사용자 1인 규모에서 무가치 |
| 학습 모델/임베딩 추천 | M1은 규칙 기반 — CF조차 v2 |

## Traceability

Which phases cover which requirements. Filled during roadmap creation (2026-08-06).

| Requirement | Phase | Status | 배정 근거 (자명하지 않은 것만) |
|-------------|-------|--------|--------------------------------|
| AUTH-01 | Phase 3 | Pending | — |
| AUTH-02 | Phase 3 | Pending | — |
| AUTH-03 | Phase 8 | Pending | 유일한 admin-gated 표면(인제스트)과 같은 phase |
| PROF-01 | Phase 3 | Pending | — |
| PROF-02 | Phase 3 | Pending | — |
| PROF-03 | Phase 3 | Pending | — |
| PROF-04 | Phase 3 | Pending | — |
| PROF-05 | Phase 3 | Pending | — |
| BROW-01 | Phase 5 | Pending | — |
| BROW-02 | Phase 5 | Pending | 행 타입 계약 소유. 비개인화 4종 P5 라이브, 개인화(P6)·마이리스트(P7)가 같은 계약에 플러그인 합류 → P7 종료 시 6종 완성 |
| BROW-03 | Phase 5 | Pending | — |
| BROW-04 | Phase 5 | Pending | — |
| BROW-05 | Phase 5 | Pending | — |
| BROW-06 | Phase 5 | Pending | — |
| BROW-07 | Phase 5 | Pending | — |
| BROW-08 | Phase 5 | Pending | — |
| PLAY-01 | Phase 2 | Pending | — |
| PLAY-02 | Phase 2 | Pending | — |
| PLAY-03 | Phase 4 | Pending | — |
| PLAY-04 | Phase 4 | Pending | — |
| PLAY-05 | Phase 4 | Pending | — |
| PLAY-06 | Phase 4 | Pending | — |
| PLAY-07 | Phase 4 | Pending | — |
| PLAY-08 | Phase 4 | Pending | 플레이어가 완성되는 도메인 phase. P2는 재생 계약 실증까지 |
| SRCH-01 | Phase 7 | Pending | — |
| SRCH-02 | Phase 7 | Pending | — |
| SRCH-03 | Phase 7 | Pending | — |
| SRCH-04 | Phase 7 | Pending | — |
| SRCH-05 | Phase 7 | Pending | — |
| LIST-01 | Phase 7 | Pending | — |
| LIST-02 | Phase 7 | Pending | — |
| LIST-03 | Phase 7 | Pending | — |
| RECO-01 | Phase 5 | Pending | 레지스트리는 행이 여러 개 생기는 P5에서 탄생. P6·P7의 신규 행이 '기존 파일 수정 0'을 실증 |
| RECO-02 | Phase 6 | Pending | — |
| RECO-03 | Phase 6 | Pending | — |
| RECO-04 | Phase 5 | Pending | Top10은 비개인화 폴백 행 세트의 일부 → P5 |
| RECO-05 | Phase 5 | Pending | 신규 추가 행은 비개인화 폴백 행 세트의 일부 → P5 |
| RECO-06 | Phase 6 | Pending | 폴백 구현체 자체는 P5 산출물. 서킷브레이커와 코드 경로를 공유한다는 속성이 P6 |
| RECO-07 | Phase 6 | Pending | — |
| INGST-01 | Phase 2 | Pending | 요구사항이 명시한 '가장 이른 산출물'. 손-트랜스코딩 사다리 규약과 같은 phase |
| INGST-02 | Phase 8 | Pending | — |
| INGST-03 | Phase 8 | Pending | — |
| INGST-04 | Phase 8 | Pending | — |
| INGST-05 | Phase 8 | Pending | — |
| INGST-06 | Phase 8 | Pending | — |
| INGST-07 | Phase 8 | Pending | — |
| INGST-08 | Phase 8 | Pending | — |
| INGST-09 | Phase 8 | Pending | 아트워크 '등록'은 관리자 기능이라 P8. 행 UI가 쓰는 아트워크 값은 P2 배치 시딩으로 선확보 |
| INGST-10 | Phase 4 | Pending | 소비자 PLAY-07(자막 토글)과 같은 phase (producer↔consumer coherence) |
| INGST-11 | Phase 8 | Pending | — |
| INGST-12 | Phase 3 | Pending | 발행 결정권은 catalog 소유. ingest는 P8에서 상태 전이 구동자로 합류 |
| INGST-13 | Phase 8 | Pending | — |
| ARCH-01 | Phase 1 | Pending | — |
| ARCH-02 | Phase 6 | Pending | — |
| ARCH-03 | Phase 2 | Pending | — |
| ARCH-04 | Phase 6 | Pending | 계측 대상(서킷 CLOSED→OPEN 전이)이 생기는 지점 |
| ARCH-05 | Phase 1 | Pending | — |
| LGL-01 | Phase 5 | Pending | 표기가 사용자에게 노출되는 지점. 스키마 컬럼(attribution/license)은 P1 가드레일 산출물 |

**Coverage:**
- v1 requirements: **58 total**
- Mapped to phases: 58
- Unmapped: 0 (orphan 0, 중복 배정 0)

**Phase별 분포:** P1 2 · P2 4 · P3 8 · P4 7 · P5 12 · P6 6 · P7 8 · P8 11 = 58

**총계 정정 (2026-08-06):** 종전 이 문서는 v1 요구사항을 "53 total"이라 적었으나 실측 체크박스는 58개다
(`grep -c '^- \[ \] \*\*'` = 58; 카테고리별 AUTH 3 · PROF 5 · BROW 8 · PLAY 8 · SRCH 5 · LIST 3 · RECO 7 · INGST 13 · ARCH 5 · LGL 1).
5개 차이의 출처는 확인되지 않았다 — 로드맵은 **실측 58개 전부**를 매핑했으므로 어느 쪽이 오기였든 누락은 없다.

순서상 두 이견(인제스트 제품화 시점 D1, 검색 배치 D2)의 확정 근거는 ROADMAP.md §Ordering Decisions에 있다.

---
*Requirements defined: 2026-08-06*
*Last updated: 2026-08-06 after roadmap creation (traceability 채움 + v1 총계 53→58 실측 정정)*
