# Feature Research

**Domain:** SVOD 스트리밍 서비스 (Netflix-class) — 포트폴리오 스케일 클론
**Researched:** 2026-08-06
**Confidence:** MEDIUM-HIGH (핵심 UX/파이프라인 주장은 Netflix 공식 help/techblog·arXiv 원문 확인. 일부 수치는 2차 출처 단독 → 개별 LOW 표기)

> 표기: 신뢰도는 주장 단위로 표기한다. **[열림]** = 이 문서가 결정하지 않는 설계 선택지(요구사항 정의 단계 결정 대상).
> 이 문서는 "무엇을 만들까"의 지도이지 승인된 스코프가 아니다 — 승인 스코프는 `PROJECT.md` Active 7종.

---

## 0. 이 프로젝트에서 "차별화"의 정의 (읽는 법)

일반 제품 리서치의 differentiator = 사용자 획득 우위. **본 프로젝트의 실제 소비자는 채용 평가자/면접관**이다
(`PROJECT.md`: "대규모 서비스 아키텍처의 실전 구현 포트폴리오"). 따라서 이 문서는 두 축을 분리해 표기한다:

- **Table stakes** = 스트리밍 제품으로서 없으면 "미완성"으로 읽히는 것 (사용자 기준)
- **Differentiator** = 클론 다수가 안 하는 것 중 **엔지니어링 판단이 드러나는** 것 (평가자 기준)

이 구분을 명시하는 이유: "넷플릭스에 있으니 넣자"는 스코프 폭발의 표준 경로이고, 실제로 넷플릭스 기능 중 상당수는
**우리 카탈로그 규모(오픈무비 수십 편)에서 의미가 소멸**한다 (아래 §Anti-Features의 지배적 사유).

---

## Feature Landscape

### Table Stakes (Users Expect These)

없으면 "스트리밍 서비스"로 인식되지 않는 것들.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| 프로필 분리 (가구 1계정 N프로필) | Netflix는 계정당 최대 5 프로필, 프로필마다 **시청 이력·평가·추천·My List·게임 저장이 분리**된다 (HIGH: [help/10421](https://help.netflix.com/en/node/10421)) | MEDIUM | 데이터 모델 파급이 큼 — 거의 모든 도메인 테이블 FK가 `account_id`가 아니라 **`profile_id`** |
| 프로필 선택 화면 ("누가 시청하나요?") | 프로필 개념의 진입점. 없으면 프로필이 설정 메뉴에 숨은 기능이 됨 | LOW | 로그인 직후 라우팅 + 프로필 컨텍스트 확립 지점 |
| 행(row) 기반 홈 = lolomo | Netflix 홈은 "주제적으로 일관된 행들의 2차원 레이아웃"이며 행 자체가 발견의 주 경로 (HIGH: [Learning a Personalized Homepage](https://netflixtechblog.com/learning-a-personalized-homepage-aa8ec670359a)) | MEDIUM | 조립 5단계(candidate→filter→video rank→row select→dedup/format)를 서버(BFF)에서 수행 |
| 히어로/빌보드 1건 | 행만 있으면 "무엇부터?"가 없다. 업계 관용 문법 | LOW | 규칙 기반(최신/에디터 픽)이면 충분 |
| Continue Watching 행 | Netflix는 **재생 버튼을 누른 순간** 몇 초만 봐도 이 행에 진입시킨다 (MEDIUM: [help/115312](https://help.netflix.com/en/node/115312) 및 2차 출처) | MEDIUM | 진입 규칙·이탈(완료) 규칙·수동 제거 3종이 한 세트 |
| 시청 위치 저장/이어보기 | Core Value 그 자체 (`PROJECT.md`) | MEDIUM | 저장 주기·완료 임계값·크로스 디바이스가 하위 문제 (§2.3) |
| HLS 어댑티브 재생 | 대역폭 변동에서 끊기지 않는 것이 스트리밍의 정의 | HIGH | hls.js(MSE) + Safari native HLS 폴백 2경로 |
| 자막 트랙 선택 | 오픈무비(Sintel 등)는 다국어 자막이 원본 자산에 존재 → 없으면 "왜 없지"가 됨 | MEDIUM | hls.js `subtitleTrack` / `allSubtitleTracks` API 존재 (HIGH: [hls.js API.md](https://github.com/video-dev/hls.js/blob/master/docs/API.md)) |
| 기본 재생 컨트롤 (재생/일시정지·탐색·음량·전체화면·키보드 단축키) | 커스텀 플레이어를 만드는 순간 브라우저 기본 UI를 잃는다 | MEDIUM | 커스텀 컨트롤을 만들면 접근성/단축키까지 우리 책임이 됨 |
| 타이틀 상세 (시놉시스·장르·연도·러닝타임·출연/감독) | 클릭 후 "이게 뭔지" 판단 근거 | LOW | 상세 모달 + deep link URL 유지 |
| 검색 (즉시 검색) | Netflix는 **키스트로크마다** 결과를 낸다. TV에서 쿼리 길이 중앙값 3자·모바일 5자 (HIGH: [arXiv 1903.04638](https://ar5iv.arxiv.org/html/1903.04638)) | MEDIUM | 짧은 쿼리 대응 = prefix/부분 일치가 필수. "완전 일치 검색"은 사실상 미작동 |
| 무결과 상태의 대체 제안 | Netflix 검색의 **최소 13%가 카탈로그에 없는 작품** 대상 (HIGH: 동 arXiv) — 우리 카탈로그는 수십 편이라 비율이 훨씬 높을 것 | LOW | 빈 화면 금지. 인기/장르 행으로 착지 |
| 마이리스트 | 프로필별 1개, 프로필 간 비공개 (MEDIUM: [help/10523](https://help.netflix.com/en/node/10523) + 2차) | LOW | 추가/제거 진입점이 카드·상세·플레이어 3곳 |
| 개인화된 행 (추천 v1) | 홈이 모두에게 동일하면 "카탈로그 페이지"이지 스트리밍 홈이 아님 | MEDIUM | 규칙 기반으로도 표면상 요건 충족 |
| Top 10 행 (순위 배지) | 업계 표준 문법이 됨. Netflix는 주간 시청 지표로 산출·화요일 발행 (MEDIUM: [about.netflix.com Top 10](https://about.netflix.com/en/news/top-10-things-about-netflix-top-10)) | LOW | 집계 배치 or 실시간 쿼리 — 우리 규모면 뷰 1개 |
| 시청 활동(이력) 조회 | Continue Watching 제거 ≠ 이력 삭제. Netflix도 두 개념을 분리 (MEDIUM: help/115312 요지) | LOW | 이력이 추천 v1의 입력이므로 어차피 필요한 테이블 |
| 로그인/세션/로그아웃 | — | LOW | 프로필 컨텍스트와 결합 [열림: 헤더 vs 토큰 클레임] |
| 재생 오류 복구 | 세그먼트 404/네트워크 순단이 실제로 발생 | MEDIUM | hls.js ERROR 이벤트 → `recoverMediaError`/재시도. 미처리 시 검은 화면 |
| 관리자 콘텐츠 등록 (인제스트) | 카탈로그가 없으면 위 전부가 빈 껍데기 | HIGH | 실질적으로 **다른 모든 기능의 전제** (§Dependencies) |

### Differentiators (Competitive Advantage)

평가자 관점에서 "클론 튜토리얼과 다르다"를 만드는 것들. **전부 넣지 말 것** — 아래 P 표기 참조(§Prioritization).

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| 추천 실패 → 비개인화 행 폴백 (Resilience4j 서킷브레이커) **실측 데모** | Netflix Hystrix 계보를 "설정했다"가 아니라 **의도적으로 깨뜨려 보여줄 수 있음**. 면접 시연 자산 | MEDIUM | 폴백 경로 = 콜드스타트 경로와 동일 코드로 설계하면 비용 절반 (§6) |
| 재생 QoE 계측 (기동 시간·리버퍼 횟수·비트레이트 전환 분포) | 스트리밍 회사가 실제로 최적화하는 지표를 대시보드로 보여줌 | MEDIUM | hls.js 이벤트 → 백엔드 수집 → Prometheus/Grafana (기 계획 스택) |
| 인제스트 잡 상태 머신 + 진행률 + 재시도 UI | 비동기 파이프라인을 "돌아간다"가 아니라 **관측 가능하게** 보여줌. MediaConvert는 SUBMITTED/PROGRESSING/COMPLETE/CANCELED/ERROR 5상태 + 48시간 타임아웃으로 ERROR 전이 (MEDIUM: [MediaConvert job progress](https://docs.aws.amazon.com/mediaconvert/latest/ug/how-mediaconvert-jobs-progress.html)) | MEDIUM | 우리 타임아웃은 분 단위로 축소 |
| 미디어 검사(ffprobe) → 사다리 결정 → 검증 3단 분리 | Netflix 파이프라인의 실제 서비스 분해와 **개념적으로 동형**: VIS(검사)/CAS(복잡도)/LGS(사다리 생성)/VES(인코딩)/VVS(검증)/VQS(품질) (HIGH: [Rebuilding Netflix Video Processing Pipeline](https://netflixtechblog.com/rebuilding-netflix-video-processing-pipeline-with-microservices-4e5e6310e359)) | MEDIUM | "ffmpeg 한 방 호출" 대비 차이가 명확. **업스케일 금지 규칙**(480p 소스에 1080p 렌디션 생성 안 함)만으로도 LGS 축소판 성립 |
| 행 생성기 플러그인 구조 (RowGenerator + 레지스트리) | 새 행 타입 = 파일 1개 + 등록 1줄 (OCP). 하네스 설계 원칙과 일관 | LOW | 추천 v1·Top10·마이리스트·이어보기가 **같은 인터페이스**의 구현체가 됨 |
| 실험 플래그 + 배정 로그 (행 순서 A/B) | Netflix 개인화의 실제 엔진은 A/B (HIGH: 위 homepage 원문의 실험 서술) | MEDIUM | UI 없이 **배정+로그만**이 최소 유효 형태 |
| 트릭플레이 스크럽 썸네일 | HLS I-frame 플레이리스트(`EXT-X-I-FRAME-STREAM-INF`)는 VOD 스크러빙/스캔 UI 지원을 위해 제공하도록 요구됨 (MEDIUM: [AWS MediaPackage I-frame playlists](https://docs.aws.amazon.com/mediapackage/latest/userguide/using-i-frame-playlists.html)) | MEDIUM | 대안: 썸네일 스프라이트 시트 + WebVTT (구현 난도 낮음, 표준성 낮음) |
| 한국어 검색 대응 (부분 일치/초성) | Netflix 검색 원문이 **한글 조합 입력**을 명시적 난제로 든다 — "mj 올드보이" 예시에서 한글 특화 색인이면 1 키스트로크 (HIGH: arXiv 1903.04638) | MEDIUM-HIGH | 국내 평가자에게 즉시 체감되는 차별점. 구현 경로 [열림] (§4) |
| 수동 품질 선택 (렌디션 강제) | ABR이 실제로 작동함을 **눈으로** 보여주는 가장 싼 방법. `hls.currentLevel` 설정만으로 즉시 전환 (HIGH: hls.js API) | LOW | 비용 대비 데모 효과가 가장 좋은 항목 |
| 아트워크 다중 이미지 + 규칙 기반 선택 | Netflix는 이미지 선택을 컨텍스트 밴딧으로 개인화, 피크 시 초당 2천만 요청 규모로 서빙한다고 서술 (MEDIUM: [Artwork Personalization](https://netflixtechblog.com/artwork-personalization-c589f074ad76)) | MEDIUM | 밴딧은 과함. **이미지 N장 + 실험 플래그로 배정**까지가 정직한 축소 |

### Anti-Features (Commonly Requested, Often Problematic)

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| DRM (Widevine/FairPlay/PlayReady) | "진짜 넷플릭스처럼" | 멀티 DRM은 **이중 포맷 패키징 + 3개 라이선스 서버 연동 + 인증서 관리**로 자체 운영 난도가 높다 (MEDIUM: [Mux multi-DRM](https://www.mux.com/articles/multi-drm-video-streaming-widevine-fairplay-playready)). 게다가 우리 콘텐츠는 **저작권-프리** — 보호 대상이 없다 | 서명 URL/토큰 만료로 세그먼트 접근 제어 (개념 증명). "왜 DRM을 안 넣었나"를 설계 문서에 답변으로 남기는 게 더 좋은 신호 |
| 다운로드(오프라인 시청) | 넷플릭스 대표 기능 | 실질적으로 DRM 영속 라이선스가 전제 (MEDIUM: 동 Mux 문서·FairPlay 오프라인 문서) + 클라이언트 저장 관리. 웹 포트폴리오에서 검증 비용 대비 가치 최저 | 이미 `PROJECT.md` Out of Scope. 유지 |
| 4K/HDR/Dolby | "고급스러움" | 트랜스코딩 시간과 디스크가 급증 (D 드라이브 배치 이유가 이미 용량). 1080p 상한으로도 ABR 시연에 필요한 렌디션 수는 충족 | 1080/720/480 3단 유지. 필요 시 360p 추가가 ABR 데모엔 더 유용 |
| 라이브 스트리밍 | 스펙 확장 욕구 | 인제스트·패키징·지연·오리진이 VOD와 **별도 파이프라인**. Netflix도 라이브를 별도 시리즈로 다룸 | VOD 완결 후 SPIRAL 턴에서 별도 마일스톤 |
| 협업 필터링(CF) 추천 (MVP 단계) | "진짜 추천 알고리즘" | 카탈로그 수십 편·유저 수 명이면 **co-play 행렬이 사실상 비어 있다**. Netflix 검색 원문도 co-play 데이터 부재 시 CF가 무력함을 지적 (HIGH: arXiv 1903.04638의 substitutable entities 논의) | 규칙 기반 v1 + **정직한 한계 서술**. CF는 시청 이벤트가 쌓인 뒤(또는 합성 이벤트 시딩 후) |
| 사용자 업로드(UGC) | 인제스트를 열면 자연스러워 보임 | 저작권·검증·악성 파일·쿼터 문제로 스코프가 서비스 성격 자체를 바꿈 | 인제스트는 **관리자 전용**. 이미 브리프 확정 |
| 실시간 워치파티/채팅/댓글 | "소셜" | WebSocket 인프라 + 동기화 + 모더레이션. Core Value(브라우즈→재생→이어보기)와 무관 | 미구현. 필요 시 별도 프로젝트 |
| 실제 결제(PG) 연동 | "완성도" | 심사·정산·PCI 표면. 포트폴리오에서 리스크만 증가 | 구독 **모의**(플랜 상태 머신)까지가 상한. `PROJECT.md`대로 후속 |
| 무한 카탈로그 채우기 (외부 메타 크롤링/더미 1000편) | 홈이 휑해 보여서 | 재생 불가능한 유령 타이틀이 대량 생기면 **모든 행이 거짓말**이 되고 QoE·추천 지표가 오염된다 | 재생 가능한 실 자산 소수 + 행 최소 길이 규칙(§Dependencies의 소카탈로그 긴장) |
| 클라이언트에서 행 조립 | FE 유연성 | BFF 결정과 정면 충돌. 캐시·개인화·중복 제거 로직이 두 곳으로 찢어짐 | 이미 확정: 서버가 완성된 lolomo JSON 반환 |
| 자체 비디오 디코더/플레이어 코어 | "직접 구현" | MSE/코덱 영역은 hls.js가 이미 성숙. 재발명은 버그만 얻음 | hls.js 위의 **컨트롤 UI·이벤트 계측**에 노력 집중 |
| 전면 다국어 UI(i18n) | 글로벌 흉내 | 한국어 단일 사용자. 번역 유지 비용만 발생 | 한국어 고정. 단 **콘텐츠 자막**은 다국어 지원(원본 자산이 그러함) |
| Netflix 로고/워드마크/실제 콘텐츠 | "진짜처럼" | 상표·저작권 리스크 | 이미 확정: "모아봄" 워드마크 + 오픈무비 |

---

## Feature Dependencies

```
[관리자 인제스트 (HLS 자산 + 카탈로그 메타)]
    ├──enables──> [브라우즈/lolomo]
    ├──enables──> [재생 (HLS)]
    ├──enables──> [검색]  (장르/제목 메타가 인제스트 산출)
    └──enables──> [자막 트랙]  (WebVTT 산출 → 마스터 플레이리스트 등록)

[계정·프로필 (profile_id)]
    ├──requires-by──> [이어보기]  (진행률은 profile 단위)
    ├──requires-by──> [마이리스트]
    ├──requires-by──> [추천 v1]
    └──requires-by──> [시청 이력]

[재생 이벤트 (watch progress)]
    └──requires──> [계정·프로필] + [재생]
         └──enables──> [Continue Watching 행]
              └──enables──> [추천 v1 (이력 기반 장르 행 / "OO를 봤으니")]
                   └──enables──> [Top 10 (시청 집계 기반)]   ※ 대안: 시드 인기도

[BFF lolomo 조립]
    └──requires──> [추천 v1] + [마이리스트] + [이어보기] + [카탈로그]
         └──protected-by──> [Resilience4j 폴백]  (추천 실패 시 비개인화 행)

[검색] ──shares-data-with──> [추천 v1]   (같은 장르/메타 인덱스)

[개인화 행] ──conflicts──> [응답 캐시]
[소규모 카탈로그] ──conflicts──> [행 다양성]
[BFF 완성 JSON] ──tension──> [낙관적 UI (마이리스트 즉시 반영)]
```

### Dependency Notes

- **인제스트가 사실상 모든 것의 전제:** HLS 자산 없이는 재생을, 카탈로그 메타 없이는 브라우즈/검색/추천을 **실측**할 수 없다.
  다만 이는 "관리자 UI를 먼저 만들라"가 아니다 — **인제스트를 2단 분해**하면 순서 문제가 풀린다:
  (a) *배치 스크립트 버전* (ffmpeg 수동 실행 → 자산 + 시드 SQL) = 아주 초기에, (b) *제품화된 인제스트*
  (업로드·잡 큐·상태 머신·검증·퍼블리시 상태) = 후반 페이즈. (a) 없이 UI를 만들면 목(mock) 자산에 의존하게 되고,
  나중에 실 자산으로 바꿀 때 재생 계약이 흔들린다 (기존 교훈: "surface render PASS ≠ 실 동작 PASS").
- **프로필이 데이터 모델의 1급 축:** 이력·마이리스트·이어보기·추천이 전부 `profile_id` FK. 계정 단위로 잘못 설계하면
  프로필 기능 추가 시 **전 테이블 마이그레이션**이 된다. 스키마 설계 전에 확정 필요.
- **추천 v1은 시청 이벤트가 있어야 의미가 생김:** 이벤트 0인 상태에서 개인화 행은 콜드스타트 폴백과 동일 출력.
  → **폴백 경로를 먼저 만들고 개인화를 그 위에 얹는 순서**가 자연스럽고, 서킷브레이커 데모와 코드 경로를 공유한다.
- **개인화 vs 캐시 충돌:** 개인화 행은 프로필마다 다르므로 공유 캐시 히트율이 낮다. 완화책은 **행 단위 캐시 분리** —
  비개인화 행(Top10·신규·장르)은 공유 캐시, 개인화 행만 프로필 키. lolomo 응답 전체를 한 덩어리로 캐싱하면 이 분리가 불가능해진다.
- **소카탈로그 ↔ 행 다양성 긴장 (실질 함정):** 타이틀이 수십 편이면 여러 행이 **같은 타이틀을 반복 노출**한다.
  Netflix 조립 파이프라인에도 중복 제거 단계가 명시되어 있다 (HIGH: homepage 원문 "deduplication to remove repeat videos").
  우리는 여기에 더해 **행 최소 길이 규칙**(길이 미달 행은 노출 안 함)과 **빈 행 억제**가 필요하다. 규칙이 없으면 홈이 깨져 보인다.
- **BFF 완성 JSON ↔ 낙관적 UI:** 마이리스트 추가 시 홈의 My List 행이 즉시 갱신되어야 하는데 행은 서버 조립물이다.
  → 부분 무효화(해당 행만 재요청) 계약을 API 설계 단계에서 정해야 한다 [열림].

---

## MVP 7종 슬라이스별 하위 기능 분해

승인된 7종이 **실제로 무엇을 함의하는지**의 전개. 표기: **IN** = 해당 슬라이스가 성립하려면 사실상 필요 /
**CHEAP** = 비용 대비 효과가 커서 넣을 만함 / **DEFER** = 의식적 연기 / **[열림]** = 결정 필요.

### 1) 계정·프로필

| 하위 기능 | 판정 | 근거/주의 |
|---|---|---|
| 회원가입·로그인·로그아웃·세션 | IN | 프로필의 상위 컨테이너 |
| 프로필 CRUD + 상한 N | IN | Netflix는 5 (HIGH: help/10421). 우리도 유사 상한 [열림] |
| 프로필 선택 화면 + 전환 | IN | 프로필 컨텍스트 확립 지점 |
| 프로필별 데이터 격리(이력·리스트·추천) | IN | Netflix가 명시적으로 분리 (HIGH: help/10421) |
| 키즈 프로필 플래그 | IN | 브리프 확정. **플래그 + 단순화된 홈 + 성인 태그 배제**까지가 M1 |
| 아바타 프리셋 선택 | CHEAP | 이미지 N장 고정 |
| 프로필 PIN/잠금 | DEFER | Netflix엔 존재 (HIGH: help/10421). 인증 표면만 늘고 데모 가치 낮음 |
| 성숙도 등급 엔진 (프로필별 등급 이하만 노출 + 타이틀 개별 차단) | DEFER | Netflix는 프로필별 등급 + 타이틀 차단 제공 (HIGH: [help/114276](https://help.netflix.com/en/node/114276)). `PROJECT.md`가 이미 후속으로 분류 |
| 프로필별 언어/자막 스타일 | DEFER | Netflix엔 존재 (HIGH: help/10421). 단일 언어 UI에선 무가치 |
| 프로필 삭제 시 하위 데이터 처리 | IN(설계) | 카스케이드 정책은 스키마 결정 사항 [열림] |

**설계 함의:** 프로필 컨텍스트를 어떻게 실어 보낼지(요청 헤더 vs 토큰 클레임 vs 경로 변수) — API 명세 전 결정 [열림].
잘못 정하면 모든 엔드포인트가 영향받는다.

### 2) 카탈로그 브라우즈 (lolomo)

| 하위 기능 | 판정 | 근거/주의 |
|---|---|---|
| 행 조립 5단계 (후보→필터→행 내 랭킹→행 선택→중복제거·디바이스 포맷) | IN | Netflix 원문의 파이프라인과 동형 (HIGH: homepage 원문) |
| 행 타입 최소 세트: 이어보기 / 마이리스트 / Top10 / 장르행 / 신규 / 개인화 | IN | 원문이 드는 행 예시와 대응 (HIGH) |
| 히어로/빌보드 | IN | 규칙 기반 |
| 행 수평 스크롤 + 다음 페이지 지연 로드 | IN | 행 길이는 디바이스별 제약 대상 (HIGH: 원문 "number of rows, min/max length of a row") |
| 카드 호버 확장 (정지 이미지) | IN | Netflix UI 문법의 핵심 시그니처 |
| 호버 시 프리뷰 **영상** 자동재생 | DEFER | Netflix엔 존재(자동 미리보기 설정, MEDIUM: [help/2102](https://help.netflix.com/en/node/2102)). 프리뷰 클립 인코딩 자산이 추가로 필요 |
| 상세 모달 + deep link URL | IN | 뒤로가기/공유가 깨지면 즉시 티가 남 |
| 중복 제거 + 빈 행 억제 + 행 최소 길이 | IN | 소카탈로그에서 **없으면 홈이 망가짐** (§Dependencies) |
| 이미 다 본 타이틀 필터 | IN | 원문 필터 단계에 명시 (HIGH) |
| 행 응답 캐시/ETag | CHEAP | 개인화 행 제외 |
| 수직 무한 스크롤(행 지연 로드) | DEFER | 행 수가 적으면 무의미 |
| 장르 전용 페이지 / 정렬·필터 UI | DEFER | 행 홈과 기능 중복 |

### 3) 타이틀 상세 + 재생 (HLS)

| 하위 기능 | 판정 | 근거/주의 |
|---|---|---|
| 마스터 플레이리스트 + 3 렌디션(1080/720/480) ABR | IN | 브리프 확정 |
| hls.js 부착 + Safari native HLS 폴백 | IN | hls.js는 MSE 기반 (HIGH: hls.js 문서). 폴백 없으면 Safari에서 재생 실패 |
| 시청 위치 저장 (주기 저장 + 일시정지/이탈 시 flush) | IN | 업계 관용 하트비트 ≈ 10초 (MEDIUM: 2차 출처 다수). `timeupdate` 매 틱 저장은 서버 부하 → **클라 스로틀 필수** (MEDIUM: [Mux: save playback progress](https://www.mux.com/blog/save-video-playback-progress)) |
| 이어보기 재개 (`startPosition`/`currentTime` 주입) | IN | Core Value |
| **완료 임계값** (예: 95% 이상 또는 잔여 < N초 → 완료 처리) | IN | Mux 가이드가 "끝에 도달하면 저장값 제거"를 명시 (MEDIUM). 없으면 **다 본 영화가 영원히 이어보기 행에 남는다** |
| Continue Watching 진입 규칙 (재생 즉시 진입) | IN | Netflix 동작 (MEDIUM: help/115312 요지) |
| 이어보기 행에서 수동 제거 | IN | Netflix는 "행에서 제거"와 "이력 삭제"를 분리 (MEDIUM) |
| 자막 트랙 선택/토글 | IN | `hls.subtitleTrack`, `allSubtitleTracks`, `subtitleDisplay` (HIGH: hls.js API) |
| 수동 화질 선택 | CHEAP | `hls.currentLevel` = -1이면 자동 (HIGH: hls.js API). ABR 데모 최고 효율 |
| 키보드 단축키 (space/←→/f/m) | CHEAP | 커스텀 컨트롤을 만든 순간 우리 책임 |
| 재생 오류 복구 (ERROR → 재시도/레벨 강등) | IN | 미처리 시 검은 화면 |
| QoE 이벤트 로깅 (기동 시간·리버퍼·전환) | CHEAP→Differentiator | Prometheus 스택이 이미 계획됨 |
| 크로스 디바이스 이어보기 | IN(부수) | 서버 저장이면 자동으로 얻어짐. localStorage로 하면 못 얻음 (MEDIUM: Mux 문서의 한계 서술) |
| 재생 속도 (0.5–1.5x) | CHEAP | HTML5 `playbackRate` 한 줄. Netflix 범위는 0.5–1.5로 보고됨 (LOW-MEDIUM: 2차 출처만, 공식 help 미확인) |
| 다음 에피소드 자동재생 + post-play 카운트다운 | [열림] | Netflix엔 설정 존재 (MEDIUM: [help/121518](https://help.netflix.com/en/node/121518)). **우리 카탈로그에 시리즈(에피소드) 개념이 있는지가 선행 결정** — 오픈무비는 대부분 단편/단일 작품 |
| 오디오 트랙 선택 | DEFER | `setAudioOption` 존재하나 (HIGH: hls.js) 원본에 다국어 오디오가 없으면 빈 기능 |
| 인트로 스킵 | DEFER | 마커 데이터 필요. 축소하면 "관리자가 인트로 구간 수동 입력" |
| "아직 시청 중인가요?" | DEFER | Netflix는 TV에서 **3편 + 90분 무조작** 시 노출 (HIGH: [help/114059](https://help.netflix.com/en/node/114059)). 연속 재생이 없으면 트리거 자체가 안 생김 |
| 트릭플레이 스크럽 썸네일 | DEFER→Differentiator | I-frame 플레이리스트 또는 스프라이트+VTT (MEDIUM: AWS MediaPackage 문서) |
| PiP / 캐스팅 / 전체화면 제스처 | DEFER | 브라우저 기본에 위임 |

### 4) 검색

| 하위 기능 | 판정 | 근거/주의 |
|---|---|---|
| 즉시 검색(디바운스 150–250ms) | IN | 키스트로크 단위가 Netflix 기준. 낮은 지연이 클릭률과 연결된다고 서술 (HIGH: arXiv 1903.04638) |
| 짧은 쿼리 대응 (prefix/부분 일치) | IN | TV 쿼리 중앙값 3자 (HIGH: 동 논문). **2글자 검색이 0건이면 검색이 죽은 것** |
| 검색 대상: 제목(원제+한국어 제목) + 장르 | IN | 브리프 범위 |
| 인물(감독/출연) 검색 | [열림] | 논문의 "find" 의도 유형에 해당 (HIGH). 인물 테이블이 필요 → 인제스트 메타 확장과 연동 |
| 무결과 → 대체 추천 착지 | IN | 카탈로그 외 검색 비율이 높음 (HIGH: 13% 수치) |
| 결과 정렬 (어휘 매칭 우선 + 인기도 보조) | IN | 논문이 "강한 어휘 매칭을 행동 기반 추천이 묻어버리지 않게" 를 문제로 지적 (HIGH) |
| 한국어 부분 일치 인덱싱 | IN | `LIKE '%키워드%'`는 인덱스를 못 타므로 데이터가 커지면 붕괴. MySQL 8.0은 CJK용 **ngram 전문 검색 파서** 제공(기본 토큰 크기 2 → 1글자 검색은 미매칭) (MEDIUM-LOW: [한글 ngram 해설](https://hoing.io/archives/16854) 등 2차 출처. **MySQL 공식 문서로 재확인 필요 → STACK 리서치 항목**) |
| 초성 검색 (ㅇㄷㅂㅇ → 올드보이) | [열림]→Differentiator | 논문이 한글 조합 입력을 명시적 난제로 듦 (HIGH). 구현은 색인 시 초성 컬럼 파생이 가장 단순 |
| 오타 허용 | DEFER | 2차 출처의 "길이별 허용 오타 수" 규칙은 일반 검색엔진 관례이지 Netflix 사양 아님 (LOW) |
| 최근 검색어 / 자동완성 제안 | CHEAP | 로컬 저장으로 충분 |
| 검색 로그(쿼리→클릭) | DEFER | 추천 고도화 재료. v1엔 소비처 없음 |

### 5) 마이리스트

| 하위 기능 | 판정 | 근거/주의 |
|---|---|---|
| 추가/제거 (카드 호버·상세·플레이어 3진입점) | IN | 진입점이 1곳이면 발견되지 않음 |
| 프로필별 격리 | IN | Netflix도 프로필별 (HIGH: help/10421) |
| 홈 My List 행 (비면 숨김) | IN | 빈 행 억제 규칙과 동일 |
| 중복 방지 (unique(profile_id, title_id)) | IN | 스키마 제약 |
| 낙관적 UI + 실패 롤백 | CHEAP | BFF 행 부분 무효화와 연결 [열림] |
| 정렬 = 추가 역순 | IN | Netflix 실제 정렬은 자체 알고리즘이라는 2차 보도가 있으나 공식 사양 미확인 (LOW) → **우리는 추가 역순으로 단순화 명시** |
| 상한 개수 | [열림] | 2차 출처가 500 vs 2000으로 **상충** (LOW). 우리 임의값 지정 후 문서화 |
| 수동 재정렬 / 다중 리스트 / 리스트 공유 | DEFER~ANTI | 사용자 1인 규모에서 가치 없음 |

### 6) 추천 v1 (규칙 기반)

| 하위 기능 | 판정 | 근거/주의 |
|---|---|---|
| RowGenerator 인터페이스 + 레지스트리 | IN | 이어보기·마이리스트·Top10·개인화가 모두 같은 계약의 구현체 → OCP |
| 최근 시청 장르 Top-K → 장르 행 | IN | 이력 기반 v1의 핵심 |
| "OO를 시청했기 때문에" 행 (video-video 유사도) | IN | Netflix 알고리즘 목록의 video-video similarity에 대응 (MEDIUM: [Gomez-Uribe & Hunt, TMIS 2015](https://dl.acm.org/doi/10.1145/2843948)). **우리 구현은 장르/태그 자카드 유사도** (CF 아님을 명시) |
| Top 10 (기간 집계 + 순위 배지) | IN | Netflix는 주간 집계 (MEDIUM: about.netflix.com). 우리는 최근 N일 재생수 |
| 신규 추가 행 | CHEAP | 인제스트 publish 시각 정렬 |
| 콜드스타트 폴백 (이력 0 → 인기/신규/장르 대표) | IN | **서킷브레이커 폴백과 같은 코드 경로로 설계** |
| 근거(evidence) 문구 = 행 제목에 노출 | IN | Netflix가 행에 근거를 붙이는 것을 명시 (HIGH: homepage 원문) |
| 행 순서 개인화 (점수 기반 정렬) | IN(최소) | "행 선택/순서"가 원문 파이프라인의 독립 단계 (HIGH) |
| 이미 본 타이틀 제외 | IN | 원문 필터 단계 (HIGH) |
| 행 내 다양성 제약 | DEFER | 소카탈로그에선 중복 제거가 더 급함 |
| 협업 필터링 / 임베딩 / 학습 모델 | DEFER | §Anti-Features |
| 아트워크 개인화 (밴딧) | DEFER | Netflix는 밴딧 사용 (MEDIUM: artwork 원문). 우리는 이미지 N장 + 실험 배정까지 |
| 실험 플래그 기반 행 순서 A/B | DEFER→Differentiator | 배정 + 로그만이 최소 유효 형태 |

### 7) 콘텐츠 인제스트 (관리자)

| 하위 기능 | 판정 | 근거/주의 |
|---|---|---|
| 관리자 인증/역할 분리 | IN | 인제스트 = 관리자 전용 (브리프) |
| 원본 등록: **파일 업로드 vs 서버 경로 지정** | [열림] | 대용량은 청크/재개 업로드가 정석(tus 등; Cloudflare Stream은 200MB 초과 시 재개 업로드 요구 — MEDIUM: [Cloudflare resumable uploads](https://developers.cloudflare.com/stream/uploading-videos/resumable-uploads)). **M1은 "서버 로컬 경로 등록"으로 축소 가능** — 업로드 재개는 별도 난제 |
| 미디어 검사 (ffprobe: 해상도/코덱/길이/오디오/FPS) | IN | Netflix VIS 대응 (HIGH: 파이프라인 원문). **조기 실패**가 목적 |
| 렌디션 사다리 결정 + **업스케일 금지** | IN | LGS 축소판. 480p 소스에 1080p를 만들면 용량만 낭비 |
| ffmpeg 트랜스코드 → HLS 세그먼트/플레이리스트 | IN | 브리프 확정 |
| 잡 상태 머신 (QUEUED→RUNNING→COMPLETED/FAILED/CANCELED) + 진행률 | IN | MediaConvert 5상태와 동형 (MEDIUM: MediaConvert 문서) |
| 잡 타임아웃 | IN | MediaConvert는 48시간 후 ERROR (MEDIUM). 우리는 분 단위 |
| 재시도 + 멱등성 (부분 산출물 정리 후 재실행) | IN | 재시도가 쓰레기를 누적시키면 디스크가 죽는다 |
| 산출물 검증 (플레이리스트 파싱·세그먼트 존재·길이 일치) | IN | Netflix VVS 대응 (HIGH). **이게 없으면 "COMPLETED인데 재생 불가"가 발생** |
| 아트워크 (포스터/와이드) 생성 or 업로드 | IN | 행 UI가 이미지 없이는 성립 안 함 |
| 자막 등록 (SRT→WebVTT 변환) + 마스터 플레이리스트 연결 | IN | 재생의 자막 트랙이 여기 의존. Netflix 파트너 규격은 TTML1 계열이지만(MEDIUM: [Timed Text Style Guide](https://partnerhelp.netflixstudios.com/hc/en-us/articles/215758617-Timed-Text-Style-Guide-General-Requirements)) **웹 HLS에서는 WebVTT가 실용 선택** |
| 메타데이터 편집 (제목·시놉시스·장르·연도·러닝타임) | IN | 검색·추천의 입력 |
| 퍼블리시 상태 머신 (DRAFT→READY→PUBLISHED→UNPUBLISHED) | IN | 미완성 자산이 카탈로그에 새어나가는 것을 막는 유일한 장치 |
| 스토리지 레이아웃 규약 + 고아 산출물 정리 | IN | D 드라이브 용량이 실제 제약 |
| 배치 인제스트 스크립트 (초기 시딩용) | IN | §Dependencies의 (a) — **가장 먼저 필요** |
| 품질 점수(VMAF) / per-title 최적화 | DEFER | Netflix는 per-title 인코딩으로 비트레이트 절감을 보고 (MEDIUM: [Per-Title Encode Optimization](https://netflixtechblog.com/per-title-encode-optimization-7e99442b62a2)). 시도당 수십 회 시행 인코딩이 전제라 우리 스케일엔 과함 |
| 워터마킹 / 오디오 노멀라이즈 / 다중 오디오 | DEFER | — |

---

## MVP Definition

### Launch With (v1 = M1, 승인된 7종)

- [ ] **인제스트 배치 경로 + 실 HLS 자산** — 나머지 전부의 전제. 목 자산으로 시작하면 재생 계약이 나중에 흔들림
- [ ] **계정·프로필(키즈 플래그 포함)** — `profile_id`가 전 도메인의 축이라 나중에 넣으면 마이그레이션
- [ ] **재생 + 위치 저장/이어보기 + 완료 임계값** — Core Value. 완료 임계값 없으면 이어보기 행이 오염됨
- [ ] **행 기반 홈(비개인화 조립부터)** — 중복 제거·빈 행 억제 규칙 포함
- [ ] **검색(부분 일치 + 무결과 착지)** — 짧은 쿼리에서 결과가 나와야 살아있는 검색
- [ ] **마이리스트** — 행 생성기 계약의 가장 단순한 구현체 → 계약 검증용으로도 유용
- [ ] **추천 v1(이력 기반 행) + 폴백** — 폴백/콜드스타트를 먼저, 개인화를 그 위에
- [ ] **인제스트 제품화(잡 상태·검증·퍼블리시)** — 배치 경로를 관측 가능한 파이프라인으로 승격

### Add After Validation (v1.x)

- [ ] 수동 화질 선택 · 재생 속도 · 키보드 단축키 — 재생 루프가 안정된 직후 (저비용·데모 효과 큼)
- [ ] QoE 계측 + Grafana 대시보드 — 재생 이벤트 스키마가 굳은 뒤
- [ ] 서킷브레이커 폴백 실측 데모 — 추천 v1이 살아 있어야 "깨뜨리는" 시연이 성립
- [ ] 실험 플래그 + 배정 로그 (행 순서 A/B) — 행 조립기가 안정된 뒤
- [ ] 초성/한국어 검색 강화 — 검색 인덱스 전략 확정 후
- [ ] 트릭플레이 썸네일 — 인제스트에 산출 단계 추가가 선행
- [ ] 프로필 PIN · 성숙도 등급 엔진 — 키즈 플래그가 실제로 쓰인 뒤

### Future Consideration (v2+)

- [ ] 협업 필터링 추천 — 시청 이벤트가 유의미하게 쌓인 뒤 (또는 합성 이벤트 시딩 설계 후)
- [ ] 구독/결제 **모의** — 도메인 확장 가치는 있으나 Core Value와 무관
- [ ] 동시 시청 제한 — 세션/디바이스 모델이 필요. Netflix는 플랜별 2~4 동시 스트림으로 보고됨 (LOW: 2차 출처)
- [ ] 다운로드/오프라인 · DRM — §Anti-Features 유지
- [ ] 시리즈/에피소드 모델 + 다음 화 자동재생 — 카탈로그에 시리즈가 생길 때만
- [ ] 라이브 스트리밍 — 별도 파이프라인, 별도 마일스톤

---

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|-----------|---------------------|----------|
| HLS 재생 + 이어보기 | HIGH | HIGH | P1 |
| 인제스트 배치 경로(실 자산 확보) | HIGH(간접) | MEDIUM | P1 |
| 프로필 + 데이터 격리 | HIGH | MEDIUM | P1 |
| 행 홈 조립(중복제거·빈 행 억제 포함) | HIGH | MEDIUM | P1 |
| 완료 임계값 + 이어보기 행 관리 | HIGH | LOW | P1 |
| 검색(부분 일치 + 무결과 착지) | HIGH | MEDIUM | P1 |
| 마이리스트 | MEDIUM | LOW | P1 |
| 추천 v1 + 비개인화 폴백 | HIGH | MEDIUM | P1 |
| 인제스트 제품화(상태 머신·검증·퍼블리시) | MEDIUM(관리자) | HIGH | P1 |
| 자막 트랙 | MEDIUM | MEDIUM | P1~P2 |
| 수동 화질 선택 | LOW(사용자) / HIGH(평가자) | LOW | P2 |
| 키보드 단축키 · 재생 속도 | MEDIUM | LOW | P2 |
| QoE 계측 대시보드 | LOW(사용자) / HIGH(평가자) | MEDIUM | P2 |
| 서킷브레이커 폴백 데모 | LOW(사용자) / HIGH(평가자) | MEDIUM | P2 |
| 실험 플래그 + 배정 로그 | LOW / HIGH(평가자) | MEDIUM | P2 |
| 초성 검색 | MEDIUM(국내) | MEDIUM | P2 |
| 트릭플레이 썸네일 | MEDIUM | MEDIUM | P3 |
| 아트워크 다중 이미지 | LOW | MEDIUM | P3 |
| 프로필 PIN / 성숙도 등급 | LOW | MEDIUM | P3 |
| 호버 프리뷰 **영상** | MEDIUM | HIGH | P3 |
| 다음 화 자동재생 | 조건부 | MEDIUM | P3 (시리즈 도입 시) |
| CF 추천 · 다운로드 · DRM · 라이브 | — | HIGH | 비대상 |

---

## Competitor Feature Analysis

| Feature | Netflix | 국내 OTT (TVING/웨이브/왓챠) | 모아봄(우리) |
|---------|---------|------------------------------|--------------|
| 프로필 | 계정당 최대 5, 프로필별 이력/추천/리스트 분리 (HIGH: help/10421) | 프로필 제공(사업자별 상이) | 동일 구조, 상한 [열림], 키즈 플래그 |
| 홈 구성 | lolomo 2차원 행, 행·타이틀·배치 전반 개인화 (HIGH) | 행 기반 유사 문법 | 서버(BFF) 조립 lolomo, 개인화는 규칙 기반 v1 |
| 이어보기 | 재생 즉시 행 진입, 기기 간 이어보기 (MEDIUM) | TVING은 기기 전환 이어보기를 마케팅 포인트로 안내 (LOW-MEDIUM: [나무위키 TVING](https://namu.wiki/w/TVING) 등 2차) | 서버 저장 → 기기 간 자동 획득 |
| 검색 | 키스트로크 즉시 검색, 인물/장르/의도 유형 대응 (HIGH: arXiv) | 인물 검색 깊이는 사업자별 편차가 크다는 2차 관측 (LOW: 나무위키 비교 문서) | 제목·장르 우선, 인물 [열림], 초성은 차별점 후보 |
| 추천 근거 표시 | 행 제목에 근거 노출("~를 시청했기 때문에") (HIGH) | 유사 문법 사용 | 동일 채택 (근거 문구 = 행 제목) |
| Top 10 | 주간 시청 지표 기반, 국가별 목록 (MEDIUM: about.netflix.com) | 유사 랭킹 행 | 최근 N일 재생수 집계 |
| 화질 | 4K/HDR 플랜별 | 4K 일부 | 1080p 상한 (의도적) |
| DRM/다운로드 | 있음 | 있음 | **없음(의도적)** — 저작권-프리 콘텐츠라 보호 대상 부재 |
| 파이프라인 노출 | 내부 (VIS/CAS/LGS/VES/VVS/VQS로 서비스 분해, HIGH) | 내부 | **관리자 UI로 노출** = 포트폴리오 차별점 |

---

## Known Gaps / 잔여 불확실성 (정직 표기)

이 리서치에서 **닫지 못한 것들** — 요구사항/설계 단계에서 결정하거나 추가 조사 필요:

1. **시리즈(에피소드) 모델 존재 여부** — 오픈무비 카탈로그는 대체로 단일 작품. 시리즈가 없으면 "다음 화 자동재생·아직 시청 중인가요·에피소드 목록" 3개 기능군이 통째로 무의미해진다. **요구사항 정의 첫 결정 항목으로 권장.**
2. **한국어 검색 구현 경로** — MySQL 8.0 ngram 파서의 토큰 크기/1글자 검색 한계를 **공식 문서로 재확인 못 함**(2차 한국어 블로그만 확인, MEDIUM-LOW). STACK 리서치에서 확정 필요.
3. **Apple HLS Authoring Specification 원문 미확보** — 페이지 본문이 회수되지 않아 세그먼트 길이·사다리 권고 수치는 **인용하지 않았다**. 렌디션 사다리 구체 수치는 STACK/설계 단계에서 원문 재확인 권장.
4. **Netflix 재생 속도 범위(0.5–1.5x)·My List 상한(500 vs 2000)·플랜별 동시 스트림 수** — 2차 출처만 확인, 일부는 출처 간 상충(LOW). 우리 값은 어차피 임의 지정 대상이므로 차단 요소는 아님.
5. **Gomez-Uribe & Hunt(TMIS 2015) 원문 정밀 인용** — PDF 회수는 됐으나 요약이 얕게 돌아왔다(알고리즘 명칭 대응은 MEDIUM 신뢰). 추천 설계 문서 작성 시 원문 재독 권장 (인용-강제 규율 대상).
6. **호버 프리뷰 클립의 자산 비용** — 프리뷰 클립을 인제스트에서 생성할지, 정지 이미지로 갈지 미결정. 비용 추정 안 됨.

---

## Sources

**HIGH (공식 문서/원문 직접 확인)**
- [Netflix Help — 프로필 생성/편집/삭제 (계정당 5, 프로필별 이력·리스트·추천 분리, 프로필 잠금)](https://help.netflix.com/en/node/10421)
- [Netflix Help — 프로필 성숙도 등급/타이틀 차단](https://help.netflix.com/en/node/114276)
- [Netflix Help — "아직 시청 중인가요?" (TV 기준 3편+90분)](https://help.netflix.com/en/node/114059)
- [Netflix TechBlog — Learning a Personalized Homepage (lolomo 2차원 레이아웃, 조립 5단계, 행 제약, 근거)](https://netflixtechblog.com/learning-a-personalized-homepage-aa8ec670359a)
- [Netflix TechBlog — Rebuilding Netflix Video Processing Pipeline with Microservices (VIS/CAS/LGS/VES/VVS/VQS)](https://netflixtechblog.com/rebuilding-netflix-video-processing-pipeline-with-microservices-4e5e6310e359)
- [arXiv 1903.04638 — Challenges in Search on Streaming Services: Netflix Case Study (키스트로크 검색, 쿼리 길이 중앙값 3/5자, 13% 카탈로그 외 검색, fetch/find/explore, 한글 조합 입력)](https://ar5iv.arxiv.org/html/1903.04638)
- [hls.js API 문서 (subtitleTrack/allSubtitleTracks/setAudioOption/currentLevel/startLevel/autoLevelCapping)](https://github.com/video-dev/hls.js/blob/master/docs/API.md) — Context7 경유 확인

**MEDIUM (공식 문서지만 검색 스니펫 경유이거나, 2차 출처 다수 일치)**
- [Netflix Help — Continue Watching 행에서 제거](https://help.netflix.com/en/node/115312)
- [Netflix Help — 다음 화 자동재생 설정](https://help.netflix.com/en/node/121518) · [미리보기 자동재생 설정](https://help.netflix.com/en/node/2102) · [My List 사용법](https://help.netflix.com/en/node/10523)
- [About Netflix — Top 10 산출 방식(주간 집계·화요일 발행)](https://about.netflix.com/en/news/top-10-things-about-netflix-top-10)
- [Netflix TechBlog — Artwork Personalization (컨텍스트 밴딧, 대규모 이미지 서빙)](https://netflixtechblog.com/artwork-personalization-c589f074ad76)
- [Netflix TechBlog — Per-Title Encode Optimization](https://netflixtechblog.com/per-title-encode-optimization-7e99442b62a2)
- [Gomez-Uribe & Hunt, The Netflix Recommender System (TMIS 2015)](https://dl.acm.org/doi/10.1145/2843948) ([PDF 사본](https://ailab-ua.github.io/courses/resources/netflix_recommender_system_tmis_2015.pdf))
- [AWS Elemental MediaConvert — 잡 상태(SUBMITTED/PROGRESSING/COMPLETE/CANCELED/ERROR), 48h 타임아웃](https://docs.aws.amazon.com/mediaconvert/latest/ug/how-mediaconvert-jobs-progress.html)
- [AWS MediaPackage — HLS I-frame 플레이리스트(트릭플레이)](https://docs.aws.amazon.com/mediapackage/latest/userguide/using-i-frame-playlists.html)
- [Mux — Saving video playback progress (진행률 저장·종료 시 값 제거·로컬 저장 한계)](https://www.mux.com/blog/save-video-playback-progress)
- [Mux — Multi-DRM for Developers (자체 운영 복잡도)](https://www.mux.com/articles/multi-drm-video-streaming-widevine-fairplay-playready)
- [Netflix Partner Help — Timed Text Style Guide: General Requirements (TTML1 계열 요구)](https://partnerhelp.netflixstudios.com/hc/en-us/articles/215758617-Timed-Text-Style-Guide-General-Requirements)
- [Cloudflare Stream — Resumable uploads (200MB 초과 시 재개 업로드 요구)](https://developers.cloudflare.com/stream/uploading-videos/resumable-uploads)

**LOW (단독 2차 출처 / 상충 / 미검증 — 사실로 취급 금지)**
- MySQL 8.0 한글 ngram 전문 검색 파서 해설 (토큰 크기 기본 2, 1글자 미매칭): [hoing.io](https://hoing.io/archives/16854) — **MySQL 공식 문서 재확인 필요**
- Netflix 재생 속도 0.5–1.5x 범위: 2차 매체만 (공식 help 미확인)
- My List 상한 500 vs 2000: 출처 간 상충
- 플랜별 동시 스트림 2~4 / 다운로드 기기 수: 2차 매체만
- 국내 OTT 기능 비교(인물 검색 깊이 등): [나무위키 TVING](https://namu.wiki/w/TVING), [나무위키 왓챠 비교](https://namu.wiki/w/%EC%99%93%EC%B1%A0/%EB%8B%A4%EB%A5%B8%20OTT%EC%99%80%EC%9D%98%20%EB%B9%84%EA%B5%90) — 편집형 위키, 검증 불가

**미확보 (인용하지 않음)**
- Apple HLS Authoring Specification — 페이지 본문 회수 실패. 세그먼트 길이·사다리 권고 수치는 본 문서에서 주장하지 않음

---
*Feature research for: SVOD 스트리밍 서비스 (Netflix-class), 포트폴리오 스케일*
*Researched: 2026-08-06*
