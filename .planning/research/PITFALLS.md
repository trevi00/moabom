# Pitfalls Research

**Domain:** 넷플릭스형 VOD 스트리밍 서비스 (HLS ABR 재생 + lolomo 개인화 홈 + 관리자 인제스트/트랜스코딩) — Spring Modulith/JPA/MySQL 8.0 백엔드, Next.js/hls.js 프론트, Windows 11 네이티브 개발 환경, 미디어 산출물 D: 드라이브
**Researched:** 2026-08-06
**Confidence:** MEDIUM-HIGH (개별 항목별 confidence 인라인 표기. HLS/ffmpeg·MySQL ngram·Spring Modulith·hls.js는 1차 출처 확인 = HIGH. Apple HLS Authoring Spec 원문은 **WebFetch 실패**(JS 렌더 페이지) → 2차 출처 기반 MEDIUM.)

> **표기**: `[H]` = 1차 출처(공식 docs/이슈 트래커) 확인, `[M]` = 2차 출처 복수 일치, `[L]` = 단일/미검증 — 착수 전 실측 필요.
>
> **참조 phase 이름** (로드맵이 다르게 쪼개면 매핑만 조정):
> P0 기반/스캐폴딩 · P1 재생 워킹스켈레톤 · P2 인제스트/트랜스코딩 · P3 카탈로그+lolomo BFF ·
> P4 프로필+이어보기+마이리스트 · P5 검색 · P6 추천v1+복원력 · P7 관측/실험/E2E 하드닝

---

## Critical Pitfalls

### Pitfall 1: 재생(Core Value)이 마지막 페이즈로 밀린다 — 리스크 역순 배치

**What goes wrong:**
프로필 CRUD → 카탈로그 CRUD → 검색 → 마이리스트를 먼저 다 짓고 재생을 마지막에 붙인다. 그 시점에 ffmpeg 렌디션 사다리, nginx 캐시 정책, CORS, hls.js 라이프사이클, 이어보기 저장 타이밍이 **동시에** 터진다. MVP 7종 중 6종이 "완성"인데 데모에서 영상이 안 나온다.
PROJECT.md의 Core Value 진술("브라우즈→재생→이어보기 루프가 전부 실패해도 이것만은 작동해야 한다")과 정반대 순서가 된다.

**Why it happens:**
CRUD는 익숙하고 즉시 눈에 보이는 진척이 나온다. 미디어 파이프라인은 학습 곡선이 있어 "나중에"로 밀린다. 게다가 인제스트(P2)가 있어야 재생할 콘텐츠가 생긴다고 착각한다 — 실제로는 **손으로 ffmpeg 한 번 돌린 산출물** 하나면 재생 슬라이스를 끝까지 검증할 수 있다.

**How to avoid:**
P1을 **수직 슬라이스 워킹 스켈레톤**으로 못박는다: 손으로 트랜스코딩한 Big Buck Bunny 1편 → nginx 정적 서빙 → Next.js 페이지에서 hls.js 재생 → 15초 지점에서 새로고침 → 그 지점부터 재생. 관리자 UI·DB 카탈로그·검색 없이. 인제스트(P2)는 이 슬라이스가 초록불이 된 **뒤에** 자동화한다.
AC 예: `AC-P1-01: Chrome에서 재생 시작 후 15s 경과 → 탭 새로고침 → currentTime이 13~17s 범위에서 재개된다`.

**Warning signs:**
- 스프린트 3주차인데 브라우저에서 `.m3u8`을 한 번도 열어보지 않았다
- 로드맵에 "재생" phase가 4번 이후에 있다
- "트랜스코딩 파이프라인이 되면 재생 테스트할 수 있다"는 문장이 계획서에 있다

**Phase to address:** **P1** (구조 자체가 이 pitfall의 해독제). 로드맵 리뷰에서 phase 순서 자체를 게이트하라.

---

### Pitfall 2: 렌디션 간 키프레임 미정렬 — ABR 전환 시 끊김

**What goes wrong:**
1080/720/480 렌디션을 각각 따로 인코딩하면 각 인코더가 **자기 판단으로** 키프레임을 넣는다(scene-cut detection). 세그먼트 경계가 렌디션마다 어긋나 플레이어가 화질을 바꿀 때 갭/중복/멈춤이 생긴다. 로컬 고속 네트워크에서는 전환이 안 일어나 **증상이 안 보이다가**, 네트워크 스로틀링 데모에서 처음 드러난다. `[H]`

**Why it happens:**
- ffmpeg 기본값이 scene-cut 키프레임 삽입을 켠다 → 세그먼트 정렬을 깬다
- `-c copy`로 세그먼트만 자르면 `hls_time`은 **힌트**일 뿐이고 실제 컷은 기존 GOP 경계에서 일어난다 → EXTINF 값이 렌디션마다 다르게 나온다
- `keyint_min` 기본값(1)이 인코더에게 아무 때나 키프레임을 넣을 자유를 준다

**How to avoid:**
세그먼트 길이·GOP는 **사다리 전체의 공통 결정**이지 렌디션별 결정이 아니다. 모든 렌디션에 동일하게:
```
-sc_threshold 0  -g <fps*seg>  -keyint_min <fps*seg>
-force_key_frames "expr:gte(t,n_forced*<seg>)"
-hls_time <seg>  -hls_playlist_type vod
```
`<seg>`는 VOD 기준 6초 권장(Apple HLS Authoring Spec) `[M]`. 프레임레이트가 렌디션마다 다르면 `-g` 값도 달라져야 하므로 **fps를 사다리 전체에서 고정**하는 편이 실수가 적다.
마스터 플레이리스트에 `#EXT-X-INDEPENDENT-SEGMENTS`를 넣고, 각 variant에 `CODECS` 속성을 반드시 채운다 `[M]`.

**Warning signs:**
- 각 렌디션 `.m3u8`의 `EXTINF` 시퀀스를 diff했을 때 값이 다르다 ← **이게 결정적 검사다. P2 자동 검증으로 코드화하라.**
- `hls_time 6`을 줬는데 세그먼트가 9초, 4초로 들쭉날쭉
- 크롬 DevTools에서 네트워크를 3G로 조이면 화면이 잠깐 얼거나 오디오가 튄다

**Phase to address:** **P2** (트랜스코딩 프로파일 확정 시). 검증은 P2 자동 테스트 + P7 스로틀링 E2E.

> ⚠️ **Windows 개발자 특유 함정** `[M]`: Apple의 `mediastreamvalidator`(HTTP Live Streaming Tools)는 **macOS 전용**이다. 이 프로젝트의 표준 검증 수단이 될 수 없다. 대체 게이트: 모든 variant의 `EXTINF` 배열 동일성 + `ffprobe -show_frames -select_streams v -show_entries frame=key_frame,pkt_pts_time`으로 각 세그먼트 첫 프레임이 IDR인지 확인하는 **직접 만든 스크립트**를 P2 산출물에 포함하라. "검증기가 없어서 눈으로 봤다"가 되면 이 pitfall은 반드시 재발한다.

---

### Pitfall 3: 미디어 산출물이 git 히스토리에 들어간다

**What goes wrong:**
`D:/moabom` 안에서 ffmpeg를 돌리면 수백 개의 `.ts`/`.m4s`와 원본 `.mp4`가 워킹트리에 생긴다. 한 번 `git add .` 하면 끝이다. GitHub은 **100MB 파일을 하드 리젝**하고(`GH001: Large files detected`), 리젝은 push 시점에 나므로 이미 여러 커밋 깊이에 묻혀 있다. 복구는 `git filter-repo`/BFG **히스토리 재작성** — 협업 중이면 모든 클론이 깨진다. `[H]`

**Why it happens:**
저장소 루트와 미디어 루트가 같은 트리에 있다. `.gitignore`를 "나중에" 추가한다. Blender 오픈무비 원본(Sintel 1080p ≈ 수백 MB)은 파일 하나로도 리젝 라인을 넘는다.

**How to avoid:**
1. **첫 커밋 전에** `.gitignore`: `media/`, `*.ts`, `*.m4s`, `*.mp4`, `*.mkv`, `transcode-tmp/`, `uploads/`
2. 더 강하게: 미디어 루트를 **저장소 밖**에 둔다 — `D:/moabom-media/` (repo는 `D:/moabom/`). 경로는 `app.media.root` 설정값 + `.env`로 주입. 물리적으로 git이 볼 수 없으면 사고가 불가능하다.
3. pre-commit 훅: 스테이징된 파일 중 >5MB가 있으면 거부.
4. Git LFS는 **이 프로젝트에선 권장하지 않음** — LFS 대역폭/스토리지 쿼터를 소모하고, 포트폴리오 저장소에 트랜스코딩 산출물을 버전관리할 이유가 없다(재생성 가능한 파생물이다).

**Warning signs:**
- `git status`에 `.ts` 파일이 보인다 → **즉시 중단**
- `.git` 디렉터리가 50MB를 넘는다 (`git count-objects -vH`)
- push가 평소보다 오래 걸린다

**Phase to address:** **P0** — 저장소 초기화 커밋에 포함. 이것보다 앞선 것은 없다.

---

### Pitfall 4: nginx가 `.m3u8`을 `.ts`와 같은 정책으로 캐시한다

**What goes wrong:**
`location /media { expires 1y; }` 한 줄로 끝내면, 관리자가 타이틀을 **재트랜스코딩하거나 렌디션을 추가**했을 때 클라이언트/프록시가 1년짜리 낡은 마스터 플레이리스트를 붙들고 삭제된 세그먼트를 요청한다 → 404 → 플레이어가 fatal NETWORK_ERROR. 반대로 전부 `no-cache`로 도망가면 CDN 개념 증명(Open Connect 축소 이식)이라는 이 프로젝트의 학습 목표 자체가 사라진다. `[M]`

또한 잘못된 `Expires` 헤더(먼 미래)는 `Cache-Control`이 옳아도 m3u8을 캐시에서 꺼내오게 만든 실사례가 보고돼 있다 `[M]`.

**Why it happens:**
"VOD는 불변이니까 다 캐시해도 된다"는 추론. 실제로 불변인 것은 **세그먼트**이고, 플레이리스트는 퍼블리시 단위로 **교체되는 포인터**다.

**How to avoid:**
정책을 두 갈래로 분리하고, 여기에 **버전 디렉터리**를 더한다:
```nginx
location ~ \.m3u8$ { add_header Cache-Control "public, max-age=10"; }
location ~ \.(ts|m4s|mp4)$ { add_header Cache-Control "public, max-age=31536000, immutable"; }
```
- 퍼블리시 경로를 `/media/{titleId}/v{n}/master.m3u8`로 두고, 새 트랜스코딩은 `v{n+1}`에 만든 뒤 DB의 `playback_url`만 원자적으로 갱신한다. 그러면 세그먼트는 **진짜로 불변**이 되고 immutable 캐시가 정당해진다. 롤백도 포인터 되돌리기다.
- `proxy_cache_lock on;` — 여러 클라이언트가 같은 미캐시 세그먼트를 동시 요청할 때 upstream 요청 1개로 접는다 `[M]`.

**Warning signs:**
- 재트랜스코딩 후 강력 새로고침(Ctrl+Shift+R)해야 새 화질이 보인다
- DevTools Network에서 세그먼트 404 + hls.js `fragLoadError`
- 같은 URL이 서로 다른 콘텐츠를 가리킨 적이 있다 (= 캐시 무효화 불가능 구조라는 뜻)

**Phase to address:** **P1**(캐시 헤더 분리) + **P2**(버전 디렉터리 + 원자적 포인터 스위치). P7에서 재트랜스코딩 시나리오 E2E.

> **Windows/Docker 추가 함정** `[M]`: nginx를 Docker에서 **Windows 호스트 바인드 마운트**(`D:/moabom-media` → 컨테이너)로 서빙하면, `sendfile`이 가상화 파일시스템에서 오작동해 **잘린/낡은 파일**을 내보내는 사례가 보고돼 있다. 개발 compose에는 `sendfile off;`를 넣어라. 이 증상은 "가끔 세그먼트가 깨진다"로 나타나 트랜스코딩 버그로 오진하기 딱 좋다.

---

### Pitfall 5: Spring Modulith 경계 침식 — JPA 양방향 연관이 모듈 사이클을 만든다

**What goes wrong:**
`catalog.Title`과 `playback.WatchProgress`, `profile.Profile`과 `mylist.MyListItem`을 JPA `@ManyToOne`/`@OneToMany` 양방향으로 엮는다. 컴파일도 되고 런타임도 돈다. 그런데 `ApplicationModules.of(App.class).verify()`가 **"A module should depend one way on another but never both"**로 실패한다. 이 시점에 팀은 두 갈래로 간다 — 옳은 길(ID 참조로 전환)과 **파괴적인 길**(`@ApplicationModule(type = Type.OPEN)` 또는 `allowedDependencies` 확장). `[H]`

`Type.OPEN`은 **사이클 탐지에서 아예 제외**되고 모든 내부 타입이 unnamed named interface에 들어간다 — 즉 그 모듈의 경계가 소멸한다 `[H]`. 공식 문서 스스로 "완전 모듈화된 앱에서 open module 사용은 **부실한 모듈화의 징후**"이며 레거시 마이그레이션 도구라고 명시한다 `[H]`.

**Why it happens:**
JPA 튜토리얼이 전부 양방향 연관을 가르친다. 그리고 `verify()`를 **CI에 늦게 붙이면** 위반이 수십 개 쌓인 상태로 처음 켜지고, 그때는 `OPEN`이 유일하게 현실적인 선택처럼 보인다.

**How to avoid:**
1. **`verify()` 테스트를 P0 첫 커밋에 넣는다.** 모듈이 빈 껍데기일 때 켜야 한다. 위반 1건에서 고치는 비용과 40건에서 고치는 비용은 다른 종이다.
2. **모듈 간 참조는 타입드 ID만**: `WatchProgress`는 `Title` 엔티티가 아니라 `TitleId(Long)`를 가진다. 조인이 필요하면 BFF/애플리케이션 서비스가 두 모듈의 API를 호출해 조합한다.
3. `@NamedInterface`의 **비직관적 규칙** `[H]`: 어떤 패키지에 `@NamedInterface("spi")`를 선언하면 그 패키지는 **unnamed named interface에서 제거**되고 오직 `"order :: spi"` 형태로만 접근 가능해진다. "노출을 늘리려고" 붙였다가 **기존 접근이 깨지는** 방향으로 작동한다. 붙이기 전에 소비자 측 `allowedDependencies`를 같이 고쳐라.
4. `allowedDependencies`를 **선언하지 않은 모듈은 제약이 없다** `[H]` — 다른 모듈의 루트 패키지 + 모든 named interface에 접근 가능. 즉 경계는 **명시적으로 선언해야만 존재한다**. 6개 모듈 전부에 `allowedDependencies`를 채워라.
5. 팀 규약: `type = OPEN` 추가는 **하네스 mutation 토큰급 결정**으로 취급 — PR에서 별도 정당화 없이는 금지.

**Warning signs:**
- `verify()`를 고치는 PR의 diff에 `Type.OPEN` 또는 `allowedDependencies` 확장만 있고 코드 이동이 없다
- 모듈 A의 리포지토리가 모듈 B의 엔티티를 반환한다
- `@Entity` 클래스가 `...module.internal` 밖(= public API 패키지)에 있다

**Phase to address:** **P0**(verify() + 6모듈 골격 + allowedDependencies 선언) — 이후 매 phase의 DoD에 `verify()` green 포함.

> **버전 핀 주의** `[M]`: Spring Boot **3.2.x ↔ Spring Modulith 1.1.x**. Modulith 1.2는 Boot 3.3 대상이며 3.2/3.1 호환을 유지한다. BOM 없이 개별 버전을 찍으면 조용한 부조화가 난다 — `spring-modulith-bom`을 쓰고 P0에서 한 번 못박아라.

---

### Pitfall 6: lolomo 조립의 N+1 — 홈 1회 요청에 수백 쿼리

**What goes wrong:**
"행 10개 × 타이틀 20개"인 홈 응답을 만들면서 행마다 쿼리, 타이틀마다 장르/이미지/이어보기 진행률을 lazy 로딩한다. 카탈로그 200편 규모에서 홈 1회 요청이 **200~400 쿼리**가 된다. 로컬에서는 80ms라 눈치 못 채고, 포트폴리오 리뷰어가 로그를 켜는 순간 드러난다.
BFF가 완성된 lolomo JSON을 반환한다는 이 프로젝트의 설계 결정이 **N+1을 한 엔드포인트에 집중시킨다** — 좋은 결정이지만 대가를 명시적으로 관리해야 한다.

고치려고 `JOIN FETCH`를 여러 컬렉션에 걸면 `MultipleBagFetchException`이 터지고 `[H]`, 페이징을 붙이면 `HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!` — Hibernate가 **전체 결과셋을 메모리로 가져와** 자바에서 자르는 상태가 된다 `[H]`.

**Why it happens:**
행 조립은 본질적으로 중첩 컬렉션(행 → 타이틀 → 장르)이고, JPA의 기본 동작이 이 형태에서 가장 나쁘게 나온다.

**How to avoid:**
1. **쿼리 예산을 테스트로 못박는다.** 이게 유일하게 썩지 않는 방어다:
   ```java
   // Hibernate Statistics 또는 datasource-proxy로 쿼리 카운트
   assertThat(queryCount).isLessThanOrEqualTo(8);  // 홈 1회 요청
   ```
   숫자는 P3에서 실측 후 정하되 **상한은 행 개수와 무관해야 한다**(O(1) not O(rows)).
2. **2-쿼리 패턴** `[H]`: (a) ID만 페이징으로 조회 → (b) `WHERE id IN (:ids)`로 전체 엔티티 페치. 또는 MySQL 8.0의 윈도 함수(`DENSE_RANK()`)로 한 방에 — 이 프로젝트가 MySQL 8.0을 고른 이유 중 하나다.
3. 컬렉션은 `List` 대신 `Set` (bag 중복 제거) `[H]`, `@BatchSize`로 N+1을 N/batch+1로 축소.
4. 행 자체가 개인화 결과라면 **`profileId` 스코프 캐시**(Caffeine, TTL 짧게)를 P6에서. 단 캐시로 N+1을 덮지 말 것 — 캐시 미스 때 그대로 터진다.

**Warning signs:**
- `spring.jpa.show-sql=true`로 홈 한 번 열었을 때 콘솔이 스크롤된다
- 로그에 `HHH000104`
- 행 하나 추가할 때마다 응답 시간이 선형으로 증가한다

**Phase to address:** **P3** (lolomo BFF 구현과 동시에 쿼리 예산 테스트). P6에서 추천 행이 붙을 때 예산 재확인.

---

### Pitfall 7: 트랜스코딩 잡 큐 — 고아 잡 · 재시도 폭풍 · 디스크 소진

**What goes wrong:** (세 가지가 한 덩어리로 온다)
- **고아 잡**: 워커가 죽거나 개발 중 앱을 재시작하면 `status=PROCESSING` 행이 영원히 남는다. 동시 실행 제한이 있다면 **슬롯을 점유해 이후 모든 잡이 영원히 대기**한다. `[M]`
- **재시도 폭풍**: 재시도 상한/백오프 없이 실패 잡을 재큐잉하면 "항상 실패하는 잡"(코덱 미지원 원본 등)이 CPU를 100%로 물고 로그를 채운다. `[M]`
- **디스크 소진**: 1080/720/480 × 6초 세그먼트를 D: 드라이브에 쓴다. 공간이 떨어지면 **ffmpeg가 0바이트 파일을 만들고** 재생이 멈춘다 — 예외도 안 나고 조용히 깨진다. `[M]`
- **부분 산출물**: ffmpeg가 최종 경로에 직접 쓰면 kill 시 잘린 파일이 남는다(`moov atom not found`). 다음 재시도가 그 잔해 위에 겹쳐 쓰면 진단 불가능한 상태가 된다. `[H]` (immich #27973)

**Why it happens:**
"업로드 → `@Async`로 ffmpeg 호출 → 끝" 이 3줄이 데모에서 완벽히 작동하기 때문이다. 실패 경로는 데모에 없다.

**How to avoid:**
1. **lease + heartbeat**: 잡에 `leased_until` 컬럼. 워커가 30초마다 갱신. 만료된 `PROCESSING` 잡은 스케줄러가 `QUEUED`로 되돌린다(단, `attempts++`). 앱 재시작 후 자동 회수된다.
2. **원자적 퍼블리시** `[H]`: ffmpeg 출력은 `D:/moabom-media/.tmp/{jobId}/`에. 성공하면 **같은 볼륨 안에서** `{titleId}/v{n}/`로 rename. 실패하면 tmp 디렉터리 통째로 삭제. 시작 시 stale tmp 정리. → 미완성 산출물이 서빙 경로에 존재할 수 없다.
3. **사전 용량 체크**: 잡 시작 전 `Files.getFileStore(path).getUsableSpace()`가 `추정 산출물 크기 × 3` 미만이면 즉시 `FAILED_INSUFFICIENT_SPACE`로 종료. 추정은 (원본 크기 × 렌디션 수 × 계수) 휴리스틱으로 충분.
4. **max attempts(3) + 지수 백오프 + DLQ**: 3회 실패한 잡은 `DEAD` 상태로 이동, 관리자 UI에 "재처리" 버튼 하나. 자동 무한 재시도 금지.
5. **동시 실행도 1~2로 고정**: ffmpeg는 이미 멀티스레드다. 잡을 4개 병렬로 돌리면 전부 느려지고 디스크 소진 속도만 4배가 된다. 세마포어 크기를 설정값으로.
6. **멱등성**: 재시도가 처음부터 다시 도는 것을 전제로 설계 — 부분 재개는 이 규모에서 과설계다. 대신 tmp 정리로 "깨끗한 재시작"을 보장하라.

**Warning signs:**
- `SELECT * FROM transcode_job WHERE status='PROCESSING'`에 몇 시간 전 행이 있다
- 큐에 잡이 쌓이는데 CPU는 놀고 있다 (= 고아가 슬롯 점유)
- `attempts` 컬럼이 없다 / 상한이 없다
- D: 여유 공간을 아무도 안 본다

**Phase to address:** **P2**. 잡 상태 머신(`QUEUED/PROCESSING/PUBLISHED/FAILED/DEAD`)과 lease를 **첫 구현에 포함**하라 — 나중에 넣는 것은 상태 머신 재작성이다. P7에서 컨테이너 kill 카오스 테스트(브리프의 "신뢰성 테스트 스테이지")로 회수 동작을 실증.

---

### Pitfall 8: Java에서 ffmpeg를 호출할 때 stderr 미배수 데드락

**What goes wrong:**
`ProcessBuilder`로 ffmpeg를 띄우고 `waitFor()`를 부른다. ffmpeg는 **진행률과 로그를 전부 stderr로** 쏟는다. OS 파이프 버퍼(약 64KB)가 차면 커널이 ffmpeg를 정지시키고, Java는 `waitFor()`에서 자고 있다 — **양쪽 다 영원히 대기**. 장편 인코딩일수록(= 로그가 많을수록) 확실히 걸린다. 짧은 테스트 영상에서는 재현되지 않아 "우리 환경은 괜찮다"고 결론 내리게 만든다. `[H]`

**Why it happens:**
`ProcessBuilder`는 기본적으로 stderr를 소비하지 않는다. 그리고 5초짜리 테스트 클립은 로그가 64KB를 못 넘긴다.

**How to avoid:**
```java
var pb = new ProcessBuilder(args);      // List<String> — 셸 문자열 금지 (Pitfall 15 참조)
pb.redirectErrorStream(true);            // stdout/stderr 병합
var p = pb.start();
// 반드시 별도 스레드로 드레인 (진행률 파싱 겸용)
try (var r = new BufferedReader(new InputStreamReader(p.getInputStream(), UTF_8))) {
    r.lines().forEach(this::onFfmpegLine);
}
if (!p.waitFor(timeout, MINUTES)) { p.destroyForcibly(); throw new TranscodeTimeoutException(); }
```
- `waitFor(timeout, unit)` 없는 호출 금지 — 타임아웃 없는 대기는 워커 스레드 영구 점유다(Pitfall 7의 고아 잡과 직결).
- `destroyForcibly()` 후 **tmp 디렉터리 정리**까지가 한 세트.
- 드레인 루프를 진행률 파서로 재활용하면(`-progress pipe:1` + `out_time_ms`) 관리자 UI 진행률이 공짜로 나온다.

**Warning signs:**
- 짧은 클립은 되는데 장편에서 멈춘다
- 작업관리자에 ffmpeg.exe가 CPU 0%로 살아있다
- 코드에 `p.waitFor()` (인자 없음)가 있다

**Phase to address:** **P2**. 회귀 테스트: 의도적으로 `-loglevel debug`를 줘서 로그 수 MB를 만들고 완주하는지 확인 — 이게 이 pitfall의 반사실 검증이다.

---

### Pitfall 9: Windows 경로·인코딩이 ffmpeg 호출을 조용히 깬다

**What goes wrong:** (Windows 네이티브 개발 환경 + D: 드라이브 조합 특유)
- **백슬래시**: `D:\moabom-media\...`를 ffmpeg 인자로 넘기면 일부 필터/옵션 문법에서 백슬래시가 이스케이프로 해석된다. `hls_segment_filename`은 경로를 받되 **m3u8에는 파일명 부분만** 기록한다 `[H]` — 경로 조작이 조용히 다르게 동작할 여지.
- **드라이브 문자 콜론**: `D:`의 `:`는 여러 ffmpeg 옵션(필터 그래프, `-f segment` 옵션 리스트)에서 구분자다. 필터 인자 안에 절대경로를 넣으면 파싱이 깨진다.
- **MAX_PATH 260** `[H]`: `D:/moabom-media/{titleId}/v3/1080p/segment_00123.ts` 정도는 안전하지만, 원본 파일명을 디렉터리로 쓰면(한글 제목 + UUID + 렌디션) 쉽게 넘는다. 초과 시 앱마다 반응이 다르다 — **조용한 건너뛰기/무결성 없는 절단**이 최악.
- **한글 파일명 / cp949** `[H,M]`: 관리자가 "인터스텔라_1080p.mp4"를 업로드한다. Windows 콘솔 코드페이지(cp949)와 UTF-8 사이에서 ffmpeg 인자가 깨져 `No such file or directory`가 난다. Java `ProcessBuilder`가 인자를 native encoding으로 변환하는 경로 + Python/배치 래퍼가 섞이면 재현이 어렵다. (하네스 메모리에도 동형 사례: `reference_harness_script_stdout_utf8_cp949`)

**How to avoid:**
1. **저장 파일명은 ASCII-safe로 생성한다.** 원본 파일명은 DB `original_filename` 컬럼에만 보관하고, 디스크에는 `{titleId}/{jobId}/source.mp4`처럼 슬러그/UUID만 쓴다. → 인코딩 문제가 **구조적으로 발생 불가능**해진다. 이게 가장 값싼 방어다.
2. 경로는 항상 **forward slash 절대경로**로 정규화해서 ffmpeg에 넘긴다: `path.toAbsolutePath().toString().replace('\\','/')`. ffmpeg는 Windows에서 `/`를 받는다.
3. **경로 깊이 예산**: 미디어 루트를 짧게(`D:/mb-media/`), 세그먼트 파일명도 짧게(`s00123.ts`). 목표는 전체 경로 180자 이내.
4. Java 쪽: `-Dfile.encoding=UTF-8` + `-Dsun.jnu.encoding=UTF-8`, JDK 18+는 file.encoding 기본이 UTF-8이지만 **jnu(파일명 인코딩)는 별도**다. Java 17이므로 둘 다 명시.
5. 필터 그래프에 절대경로를 넣지 말 것(예: `subtitles=`, `movie=`) — 넣어야 하면 작업 디렉터리를 바꾸고 상대경로를 쓴다.

**Warning signs:**
- 영문 파일명은 되는데 한글 파일명만 실패한다
- 예외 메시지의 파일명이 `?????.mp4` 또는 `ÀÎÅÍ½ºÅ×¶ó.mp4`로 보인다
- 특정 타이틀만 트랜스코딩이 실패하고 원인을 못 찾는다

**Phase to address:** **P2** (파일명 정책은 인제스트 설계의 일부). 골든 테스트: 파일명 `테스트 영상 (1080p) #1.mp4` 업로드 → 성공. 이 케이스를 CI 픽스처에 박아라.

---

### Pitfall 10: MySQL FULLTEXT 한국어 — 기본 파서로는 검색이 사실상 동작하지 않는다

**What goes wrong:**
`ALTER TABLE title ADD FULLTEXT(name)` 후 "인터스텔라"로 검색하면 나오는데 "인터스"로는 안 나온다. 기본 full-text 파서가 **공백을 구분자로** 쓰기 때문에 "인터스텔라의 우주"는 `인터스텔라의`, `우주` 두 토큰이 되고, 조사가 붙은 형태는 원형으로 안 잡힌다. 한국어는 교착어라 이 문제가 영어보다 훨씬 심하다.

`WITH PARSER ngram`으로 고쳤다고 끝이 아니다 `[H]`:
- `ngram_token_size`는 **read-only 시스템 변수** — 런타임 `SET GLOBAL` 불가. `my.cnf` 또는 mysqld 인자로만 설정. **docker-compose command에 반드시 명시**해야 한다.
- 값을 바꾸면 **기존 FULLTEXT 인덱스를 전부 DROP/ADD로 재구축**해야 한다.
- `innodb_ft_min_token_size` / `ft_min_word_len`은 ngram 파서에서 **무시된다**.
- 검색어가 phrase로 변환된다: `ngram_token_size=2`일 때 `"abc"` → `"ab bc"`. 그래서 **"abcdef"를 담은 문서는 "abc def" 검색에 안 걸린다**.
- 와일드카드: prefix가 토큰 크기보다 **길면 와일드카드가 무시되고** phrase 검색으로 바뀐다 (`"abc*"` → `"ab bc"`).
- **CJK stopword 리스트는 직접 만들어야 한다.** 게다가 ngram 파서는 stopword와 "같은" 토큰이 아니라 stopword를 **포함하는** 토큰을 제외한다 — 쉼표를 stopword로 두면 쉼표를 낀 모든 bigram이 색인에서 사라진다.
- 파티션 테이블은 FULLTEXT 미지원. `MATCH()`의 컬럼 목록이 인덱스 정의와 **정확히 일치**해야 한다(InnoDB에서 인덱스 없는 boolean mode 불가).

**Why it happens:**
튜토리얼이 전부 영어 기준이고, 로컬 시드 데이터가 영문 제목("Big Buck Bunny", "Sintel")이라 **한글 검색이 깨진 걸 끝까지 모른다**.

**How to avoid:**
1. **P0의 docker-compose에 못박는다**: `command: --ngram-token-size=2 --character-set-server=utf8mb4 --collation-server=utf8mb4_0900_ai_ci`. 나중에 바꾸면 인덱스 재구축 + Flyway 마이그레이션이 필요하므로 **처음에 정하라**.
2. **한글 골든 케이스를 P0 시드에 넣는다**: "인터스텔라", "기생충", "오징어 게임" 등. 영문만 있는 시드는 이 pitfall을 은폐한다.
3. 검색 AC를 부분 문자열 기준으로 쓴다: `AC-P5-01: "인터스"로 검색 → "인터스텔라"가 결과에 포함된다`.
4. **현실적 대안 명시**: 이 규모(카탈로그 수백 편)에서는 `WHERE name LIKE CONCAT('%', :q, '%')`가 **정확도·구현 비용 모두 ngram보다 낫다**. ngram FULLTEXT를 쓰는 이유는 "대규모 검색 아키텍처를 실전 구현했다"는 포트폴리오 가치다. 그렇다면 **둘 다 구현하고 비교 측정 결과를 설계 문서에 쓰는 것**이 정직하고 값어치 있다. 무비판적 ngram 채택은 브리프의 "차용-조사 과낙관 경계" 규율 위반이다.
5. 한국어 형태소 분석(nori/mecab)은 MySQL에 없다 — Elasticsearch가 필요하다. PROJECT.md가 이걸 `[열림]`으로 둔 것은 정확한 판단. **M1에서 열지 말 것.**

**Warning signs:**
- 시드 데이터에 한글 제목이 없다
- 검색 테스트가 전부 완전 일치 케이스다
- docker-compose의 mysql 서비스에 `command:`가 없다

**Phase to address:** **P0**(compose 설정 + 한글 시드) → **P5**(구현 + 비교 측정).

---

### Pitfall 11: CORS·Range·인증이 세그먼트 레이어에서 동시에 터진다

**What goes wrong:**
Next.js는 `localhost:3000`, nginx 미디어는 `localhost:8080` — **크로스 오리진**이다. hls.js는 XHR/fetch로 세그먼트를 가져오므로 `<video src>`와 달리 CORS 규칙을 전부 받는다. 여기에 "세그먼트도 로그인해야 볼 수 있게" 요구가 붙으면 다음이 한꺼번에 걸린다:
- `Access-Control-Allow-Origin: *` 와 credentials(쿠키)는 **동시 사용 불가** → 와일드카드를 정확한 오리진으로 바꿔야 하고 `Access-Control-Allow-Credentials: true` 필요
- Range 요청의 206 응답에도 CORS 헤더가 있어야 하고, JS가 `Content-Range`/`Accept-Ranges`를 읽으려면 **`Access-Control-Expose-Headers`로 명시 노출**해야 한다 `[M]`
- OPTIONS 프리플라이트를 nginx의 미디어 location이 처리하지 않으면 전부 실패
- **Safari 네이티브 HLS 재생 경로는 커스텀 헤더를 붙일 수 없다** `[M]` — `Authorization: Bearer ...` 방식은 Safari/iOS에서 원천적으로 불가. hls.js의 `xhrSetup`/`fetchSetup`은 MSE 경로에서만 동작한다 `[H]`.

**Why it happens:**
개발 초기에 `Access-Control-Allow-Origin: *`로 열어두고 넘어간다. 인증을 P4에서 붙일 때 위 제약이 한꺼번에 나타나고, 그때는 이미 플레이어/서빙 구조가 굳어 있다.

**How to avoid:**
1. **인증 방식을 P1에 결정한다** (구현은 나중이어도 결정은 먼저). 이 프로젝트 권장: **HttpOnly + Secure 쿠키 기반 세션/서명 쿠키**. 이유 — 헤더 없이 브라우저가 자동으로 보내므로 hls.js와 Safari 네이티브 양쪽에서 동작한다. 토큰 헤더 방식은 Safari에서 막힌다.
2. hls.js 설정에 credentials 전달을 명시 `[H]`:
   ```js
   new Hls({ xhrSetup: (xhr) => { xhr.withCredentials = true; } })
   // 또는 fetch 로더: fetchSetup: (ctx, init) => { init.credentials='include'; return new Request(ctx.url, init); }
   ```
3. nginx 미디어 location: 정확한 오리진 + `Access-Control-Allow-Credentials: true` + `Access-Control-Expose-Headers: Content-Length, Content-Range, Accept-Ranges` + OPTIONS 204 처리.
4. **가능하면 크로스 오리진을 없애라** — Next.js rewrite 또는 nginx를 프론트 앞에 두어 `/media/*`를 같은 오리진으로 노출. CORS 문제 전체가 사라진다. P1의 기본 선택지로 삼아라.

**Warning signs:**
- 콘솔에 `Access to XMLHttpRequest at '...m3u8' ... has been blocked by CORS policy`
- Chrome에서는 되는데 Safari에서 안 된다 (또는 그 반대)
- 마스터 플레이리스트는 로드되는데 세그먼트만 실패한다 (= CORS는 통과, credentials가 안 붙음)

**Phase to address:** **P1**(오리진 구조 + 인증 방식 결정), **P4**(실제 인증 적용), **P7**(Safari/크로스브라우저 E2E).

---

### Pitfall 12: hls.js 라이프사이클 — Next.js SSR + StrictMode 이중 인스턴스

**What goes wrong:**
- `import Hls from 'hls.js'`를 서버 컴포넌트/SSR 경로에서 평가하면 `window is not defined`로 빌드/렌더가 깨진다.
- React 18 StrictMode는 개발 모드에서 effect를 **mount→unmount→mount** 시킨다. cleanup에서 `hls.destroy()`를 안 하면 **인스턴스가 리마운트마다 하나씩 샌다** — 두 인스턴스가 같은 `<video>`에 버퍼를 붙이면 재생이 이상해지고, 디버깅이 극도로 어렵다. `[H]`
- `startPosition`(이어보기)은 **`MANIFEST_PARSED` 이후에만 유효**하다 `[M]`. 컴포넌트 마운트 직후 `video.currentTime = 120`을 쓰면 조용히 0으로 되돌아간다.
- 네이티브 HLS 분기 판정을 `canPlayType()`만으로 하면 실패한다 — hls.js 공식 README는 **`canPlayType && 'ManagedMediaSource' in window`**를 권장한다(그 외 브라우저는 canPlayType이 true여도 신뢰할 수 없다) `[H]`.

**How to avoid:**
```tsx
'use client';
const VideoPlayer = dynamic(() => import('./VideoPlayer'), { ssr: false });
// VideoPlayer 내부
useEffect(() => {
  let hls: Hls | null = null;
  if (video.canPlayType('application/vnd.apple.mpegurl') && 'ManagedMediaSource' in window) {
    video.src = src;
    video.addEventListener('loadedmetadata', () => { video.currentTime = resumeAt; }, { once: true });
  } else if (Hls.isSupported()) {
    hls = new Hls({ startPosition: resumeAt });   // 또는 MANIFEST_PARSED 후 설정
    hls.loadSource(src); hls.attachMedia(video);
  }
  return () => { hls?.destroy(); };   // ← StrictMode 이중 마운트 방어
}, [src]);
```
에러 처리는 hls.js 공식 권고대로 `fatal` 만 특별 취급하고, MEDIA_ERROR는 `recoverMediaError()`를 **5초 쿨다운**과 함께(무한 복구 루프 방지), NETWORK_ERROR는 즉시 재시작하지 말 것 — 공식 문서가 "loop loading"을 경고한다 `[H]`.

**Warning signs:**
- `next build`에서 `window is not defined`
- 개발 모드에서 세그먼트 요청이 **정확히 2배**로 보인다
- 이어보기가 "가끔" 동작한다 (= 매니페스트 파싱 레이스)

**Phase to address:** **P1**. 이 4개는 전부 플레이어 컴포넌트 첫 구현에서 결정된다.

---

### Pitfall 13: 이어보기(resume) 시맨틱의 엣지 케이스

**What goes wrong:** (Core Value의 절반인데 "저장하고 불러오면 끝"으로 취급된다)
- **끝까지 본 콘텐츠가 이어보기 행에 남는다** — 99% 지점에 저장되어 있어 다시 누르면 크레딧 3초를 보고 끝난다. Jellyfin에도 동일 버그가 보고돼 있다 `[M]`.
- **재생 시작 시 0으로 덮어쓴다** — 플레이어가 `timeupdate`를 currentTime=0에서 먼저 쏘고, 그게 저장되어 진행률이 날아간다. **가장 흔하고 가장 치명적**이다.
- **탭을 그냥 닫으면 마지막 위치가 안 남는다** — 주기적 저장 간격(예: 30초) 안에 닫으면 최대 30초 손실.
- **프로필 격리 실패** — 가구 1계정 N프로필인데 진행률이 계정 단위로 저장되어 아빠가 본 위치에서 아이가 이어본다. 이 프로젝트에서는 **PROJECT.md가 명시한 프로필 요구사항의 직접 위반**이다.
- **저장 폭주** — `timeupdate`는 초당 4회쯤 발화한다. 스로틀 없이 POST하면 재생 1시간에 만 수천 요청.
- **뒤로 크게 점프** — 여러 기기/탭에서 동시 시청 시 낡은 위치가 최신을 덮어쓴다.

**How to avoid:**
| 규칙 | 값 |
|---|---|
| 저장 스로틀 | 10~15초 간격 + 상태 변화(pause/seeked/ended) 시 즉시 |
| 이탈 시 저장 | `visibilitychange`(hidden) + `pagehide`에서 `navigator.sendBeacon()` — `unload`는 신뢰 불가 |
| 완료 임계 | `position >= duration * 0.95` 또는 `duration - position <= 60s` → `COMPLETED`, 이어보기 행에서 제거 + 진행률 리셋 |
| 시작 임계 | `position < 10s` → 저장하지 않음 (0 덮어쓰기 방어) |
| 스코프 | PK = `(profile_id, title_id)` — **계정이 아니라 프로필** |
| 충돌 | `updated_at` 비교로 최신 우선. 큰 역행(>5분)은 사용자에게 "11:11부터 이어볼까요?" 선택지 `[M]` |
| 재생 시작 | 서버 위치는 `MANIFEST_PARSED`/`loadedmetadata` 이후에만 적용 (Pitfall 12) |

**Warning signs:**
- 진행률 테이블에 `position=0` 행이 많다
- 끝까지 본 영화가 "이어보기"에 남아 있다
- 네트워크 탭에 progress POST가 초 단위로 보인다
- 스키마 PK에 `profile_id`가 없다

**Phase to address:** **P1**(저장/복원 기본 + 0-덮어쓰기 방어 + sendBeacon), **P4**(프로필 스코프 + 완료 임계 + 이어보기 행). AC를 위 표의 각 행에 1:1로 매핑하라.

---

### Pitfall 14: docker-compose MySQL init 스크립트가 "한 번만" 돈다

**What goes wrong:**
`docker-entrypoint-initdb.d`에 `schema.sql`/`seed.sql`을 넣는다. 첫 실행은 완벽하다. 스키마를 고치고 `docker compose restart` 또는 `down && up` 하면 **아무 일도 안 일어난다** — MySQL 이미지는 `/var/lib/mysql`에 데이터가 있으면 init을 통째로 건너뛴다 `[H]`. 팀원 A는 새 스키마, 팀원 B(볼륨 재사용)는 옛 스키마로 몇 시간을 태운다.

**How to avoid:**
1. **스키마의 단일 진실은 Flyway**(또는 Liquibase)로 못박는다. `initdb.d`는 **DB/계정 생성 정도만** — 스키마·시드는 애플리케이션 기동 시 Flyway가 적용. 그러면 볼륨 재사용이 문제가 되지 않고, 이 프로젝트의 "22 스테이지 파이프라인 / DDL 설계" 규율과도 맞는다.
2. `docker compose down -v`(볼륨 삭제)를 리셋 명령으로 README에 명시. `down`만으로는 볼륨이 안 지워진다.
3. ngram 설정처럼 **서버 파라미터가 필요한 것은 `command:` 또는 `conf.d` 마운트**로 (Pitfall 10). `initdb.d`로는 안 된다.

**Warning signs:**
- "내 로컬에서는 되는데" + 스키마 불일치
- init SQL을 고쳤는데 반영이 안 된다
- README의 리셋 절차에 `-v`가 없다

**Phase to address:** **P0**.

---

### Pitfall 15: 인제스트 경로의 보안 — ffmpeg에 신뢰할 수 없는 입력을 그대로 먹인다

**What goes wrong:**
관리자 업로드 → ffmpeg. "관리자만 쓰니까 안전"이라는 가정이 두 층에서 깨진다:
- **커맨드 인젝션**: 파일명/제목을 문자열로 이어붙여 셸에 넘기면 인젝션. 또한 `-`로 시작하는 파일명은 ffmpeg가 **옵션으로 해석**한다.
- **ffmpeg 자체가 공격면** `[M]`: 조작된 `.m3u8`/컨테이너 파일을 입력으로 주면 ffmpeg가 내부 URL을 fetch하거나(SSRF) `file:` 프로토콜로 **로컬 파일을 읽어 출력 영상에 렌더링**한다 — 잘 알려진 취약점 계열이다. 업로드 파일을 그대로 `-i`에 주는 모든 서비스가 대상.
- **경로 순회**: 세그먼트를 애플리케이션이 서빙하면서 `titleId`/파일명을 경로에 그대로 결합 → `../../`.

**How to avoid:**
1. `ProcessBuilder(List<String>)` **만** 사용. 셸 문자열(`sh -c "..."`) 금지. 사용자 문자열은 인자 값으로만, 절대 옵션 위치에 오지 않게.
2. 입력 파일 경로는 **우리가 생성한 경로**(Pitfall 9의 `{titleId}/{jobId}/source.mp4`)만 사용. 원본 파일명은 ffmpeg에 도달하지 않는다.
3. **프로토콜 제한**: `-protocol_whitelist file` (+ 필요 시 `-f` 로 입력 포맷 고정). 업로드 파일을 임의 입력으로 다루지 말 것.
4. 업로드 검증: 확장자 신뢰 금지 → `ffprobe`로 실제 스트림 존재/코덱 확인 후 진행. `ffprobe`도 위 프로토콜 제한을 동일 적용.
5. 세그먼트 서빙은 **nginx 정적 경로**로 (경로 순회 방어 + 스레드 문제 동시 해결, Pitfall 16 참조). 애플리케이션이 서빙해야 한다면 `Path.normalize().startsWith(mediaRoot)` 검사 필수.
6. 인제스트 엔드포인트는 관리자 롤 + 업로드 크기 상한 + 레이트 리밋.

**Warning signs:**
- 코드에 `Runtime.exec(String)` 또는 문자열 포맷으로 만든 ffmpeg 명령
- 업로드 파일명이 디스크 경로나 ffmpeg 인자에 등장한다
- `-protocol_whitelist`가 없다

**Phase to address:** **P2**(인제스트 구현과 동시). P7 보안 점검.

---

### Pitfall 16: Spring Boot(Tomcat)가 HLS 세그먼트를 직접 서빙한다

**What goes wrong:**
편의상 `@GetMapping("/media/**")`로 세그먼트를 내보낸다. HLS는 **6초마다 요청 1개 × 동시 시청자 수**를 만든다. Tomcat 기본 스레드 풀은 200이고 파일 I/O는 블로킹이다. 동시 시청자가 몇십 명만 돼도 스레드가 파일 전송에 묶여 **API 요청(홈·검색)까지 같이 굶는다** `[M]`. 게다가 Range 요청/조건부 GET/캐시 헤더를 직접 구현해야 하고, 대부분 잘못 구현한다.

**Why it happens:**
"인증을 붙이려면 애플리케이션을 거쳐야 한다"는 추론. 그리고 nginx를 나중에 붙이려고 미룬다.

**How to avoid:**
- 세그먼트/플레이리스트는 **nginx가 정적 서빙**. 이 프로젝트는 이미 compose에 nginx가 있고 "Open Connect 축소 이식"이 학습 목표다 — 구조가 목표와 일치한다.
- 인증이 필요하면 **nginx `auth_request`** 또는 서명 쿠키로. 파일 바이트는 nginx가, 인가 판단은 Spring이 — 이게 정석 분리다.
- 정 애플리케이션을 거쳐야 하면 `X-Accel-Redirect`(nginx internal location)로 위임: 인가는 Spring, 전송은 nginx.

**Warning signs:**
- 재생 중에 홈 API 응답이 느려진다
- 스레드 덤프에 `SocketOutputStream.write`가 다수
- 컨트롤러에 `Resource`/`InputStreamResource` 반환 + 수동 Range 파싱 코드가 있다

**Phase to address:** **P1** (서빙 경로를 처음부터 nginx로). 나중 전환은 인증·CORS·캐시 정책을 전부 다시 짜는 일이다.

---

### Pitfall 17: 라이선스·상표 — 포트폴리오를 공개하는 순간의 리스크

**What goes wrong:**
- Blender 오픈무비(Sintel, Big Buck Bunny, Tears of Steel 등)는 **CC BY 3.0** — 자유롭게 쓰되 **저작자 표시가 의무**다. "저작권 프리"라고 크레딧을 생략하면 라이선스 위반이다. `[M]`
- Netflix의 로고/워드마크/서체는 이미 배제했지만(PROJECT.md), **빨간색+검정 톤 + 특유 레이아웃 + "N" 인트로 애니메이션**을 그대로 복제하면 트레이드 드레스 영역이다. 포트폴리오 공개 저장소/데모 URL이면 실제 노출이 있다.
- 스크린샷/썸네일에 실제 넷플릭스 작품 포스터를 쓰면 그것도 저작물이다.

**How to avoid:**
- 앱 내 "정보/크레딧" 페이지 + 각 타이틀 상세에 **원저작자·라이선스 표기**(예: "© Blender Foundation | blender.org — CC BY 3.0"). 시드 데이터의 스키마에 `license`, `attribution` 컬럼을 **처음부터** 넣어라. 나중에 추가하면 모든 시드를 다시 만든다.
- README에 "이 프로젝트는 Netflix와 무관하며 상표를 사용하지 않습니다. 레이아웃 문법만 참조합니다" 명시.
- 썸네일/포스터는 원본 영상에서 ffmpeg로 추출(같은 CC BY 하위) — 외부 이미지 금지.

**Warning signs:**
- 타이틀 테이블에 `attribution` 컬럼이 없다
- 크레딧 페이지가 로드맵에 없다

**Phase to address:** **P0**(스키마에 라이선스 컬럼) + **P3**(크레딧 UI).

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|----------------|-----------------|
| 렌디션 1개(720p)만 만들고 "ABR은 나중에" | 트랜스코딩 시간 1/3, P2가 빨리 끝남 | 사다리를 나중에 추가하면 **키프레임 정렬 문제가 그때 처음 발견**되고 기존 산출물 전량 재인코딩 | **절대 금지.** 사다리 3단은 P2 첫 구현에 포함. 대신 소스를 짧은 클립(30초)으로 줄여 반복 속도를 확보하라 |
| 세그먼트를 Spring이 서빙 | nginx 설정 없이 P1 진행 | 인증·CORS·캐시·스레드 4개를 나중에 동시에 재작업 (Pitfall 16) | 첫 하루 스파이크까지만. P1 종료 조건에 "nginx 서빙"을 넣어라 |
| `verify()` 테스트를 나중에 | 초기 속도 | 위반 누적 → `Type.OPEN`으로 도망 → 모듈 경계 소멸 (Pitfall 5) | **절대 금지.** P0 첫 커밋 |
| 진행률을 `localStorage`에만 저장 | 서버 API 불필요, P1이 빨라짐 | 프로필/기기 간 동기화 불가 = Core Value의 절반이 가짜. 마이그레이션 시 기존 데이터 폐기 | P1 첫 이틀 스파이크만. P1 종료 전 서버 저장으로 교체 |
| 이어보기 저장을 스로틀 없이 `timeupdate`마다 | 코드 3줄 | 요청 폭주 → DB 쓰기 부하 → 나중에 스로틀 넣으면 "마지막 저장 누락" 버그가 새로 생김 | 절대 금지 (스로틀은 5줄이다) |
| 추천 v1을 "장르 랜덤"으로 | P6이 하루면 끝남 | 시청 이력 기반이 아니면 개인화 데모가 성립 안 함 — 포트폴리오 서사가 무너짐 | 시청 이력 0인 신규 프로필의 **폴백**으로는 정당. 기본 경로로는 금지 |
| 검색을 `LIKE '%q%'`로 | 30분 | **이 규모에서는 실제로 합리적** — 단, "왜 FULLTEXT를 안 썼나/썼나"의 측정 결과를 설계 문서에 남겨야 포트폴리오 가치가 산다 | 측정+문서화가 동반되면 허용 (Pitfall 10) |
| ffmpeg 결과 파일을 최종 경로에 직접 쓰기 | temp/rename 로직 불필요 | kill/디스크풀 시 **서빙 경로에 깨진 파일**이 남고, 재시도가 그 위에 덮어써 진단 불가 (Pitfall 7) | 절대 금지 |
| 트랜스코딩을 `@Async` 메서드로만 (DB 잡 테이블 없음) | 코드 최소 | 재시작 시 진행 중 작업 증발, 상태 조회 불가, 재시도 불가 → 상태 머신 도입 시 전면 재작성 | 절대 금지 — 잡 테이블은 P2 첫 커밋 |

---

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| **ffmpeg (프로세스)** | `waitFor()` 인자 없이 호출 + stderr 미배수 → 64KB에서 데드락 | `redirectErrorStream(true)` + 별도 스레드 드레인 + `waitFor(timeout)` + `destroyForcibly()` (Pitfall 8) `[H]` |
| **ffmpeg (경로)** | Windows 백슬래시/한글 파일명을 그대로 인자로 | ASCII-safe 생성 파일명 + forward-slash 절대경로 + `-Dsun.jnu.encoding=UTF-8` (Pitfall 9) |
| **ffmpeg (보안)** | 업로드 파일을 그대로 `-i` | `-protocol_whitelist file` + `ffprobe` 사전 검증 + 우리가 만든 경로만 (Pitfall 15) `[M]` |
| **ffmpeg (HLS 출력)** | `-c copy`로 세그먼트만 자름 → EXTINF 불일치 | 렌디션별 재인코딩 + `-sc_threshold 0 -g/-keyint_min` 고정 + `-force_key_frames` (Pitfall 2) `[H]` |
| **nginx** | `expires 1y`를 미디어 location 전체에 | `.m3u8`(짧게) / 세그먼트(immutable) 분리 + 버전 디렉터리 (Pitfall 4) `[M]` |
| **nginx + Docker(Windows 바인드마운트)** | 기본 `sendfile on` → 잘린 파일 서빙 | 개발 compose에 `sendfile off;` `[M]` |
| **nginx (인증)** | 세그먼트 인증을 위해 Spring으로 프록시 | `auth_request` 또는 `X-Accel-Redirect`로 인가/전송 분리 (Pitfall 16) |
| **hls.js** | `canPlayType()`만으로 네이티브 분기 | `canPlayType && 'ManagedMediaSource' in window` `[H]` |
| **hls.js** | `withCredentials` 미설정 상태로 쿠키 인증 기대 | `xhrSetup`/`fetchSetup`으로 명시 `[H]` |
| **hls.js + React** | cleanup에서 `destroy()` 누락 → StrictMode 이중 인스턴스 | `useEffect` cleanup에서 `hls.destroy()` `[H]` |
| **hls.js (에러)** | NETWORK_ERROR fatal에서 즉시 재시작 | 공식 문서 경고: loop loading. 로딩 정책(`fragLoadPolicy` 등) 조정으로 대응 `[H]` |
| **MySQL FULLTEXT** | `SET GLOBAL ngram_token_size` 시도 / 런타임 변경 | read-only — compose `command:` 또는 `conf.d` + 인덱스 재구축 `[H]` |
| **MySQL (Docker init)** | `initdb.d`에 스키마·시드 → 볼륨 재사용 시 미실행 | Flyway를 단일 진실로, `initdb.d`는 DB/계정만, 리셋은 `down -v` `[H]` |
| **Spring Modulith** | JPA 양방향 연관 → 사이클 → `Type.OPEN`으로 우회 | 모듈 간은 타입드 ID 참조. `OPEN`은 레거시 마이그레이션 전용 `[H]` |
| **Spring Modulith** | `@NamedInterface`를 "노출 추가"로 이해 | 선언한 패키지는 unnamed interface에서 **제거**됨 — 소비자 `allowedDependencies`도 같이 수정 `[H]` |
| **Spring Modulith 이벤트** | 이벤트 외부화 실패인데 publication이 completed로 마킹된 사례 존재 | 이벤트를 M1의 필수 경로로 쓰지 말 것. 쓴다면 버전 픽스 확인 + `EVENT_PUBLICATION` 잔여 모니터링 `[M]` |
| **Spring Boot ↔ Modulith 버전** | 개별 버전 지정 | `spring-modulith-bom` 사용. Boot 3.2.x ↔ Modulith 1.1.x `[M]` |

---

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| lolomo 조립 N+1 | 홈 응답에 쿼리 수백 개, 행 추가 시 응답시간 선형 증가 | 2-쿼리 패턴 / 윈도 함수, `Set` 컬렉션, `@BatchSize`, **쿼리 예산 테스트** | 카탈로그 100편·행 8개부터 로그로 감지 가능. 300편에서 체감 |
| Hibernate 인메모리 페이징 (`HHH000104`) | 로그 경고 + 힙 사용량 급증 | 컬렉션 fetch join + 페이징 금지. ID 페이징 후 IN 페치 | 부모 행이 수천 개일 때 OOM. 이 프로젝트 규모에선 경고만 — **그래도 로그를 고쳐라, 안 그러면 진짜 문제를 덮는다** |
| Tomcat 스레드가 세그먼트 전송에 묶임 | 재생 중 API 지연, 스레드 덤프에 write 다수 | 세그먼트는 nginx 정적 서빙 | 동시 시청 ~30명 (200스레드/세그먼트당 수 초 점유) |
| 트랜스코딩 병렬도 과다 | 전체 잡이 다 느려짐, D: 여유 급감, 개발 머신 응답성 저하 | 세마포어 1~2 고정, ffmpeg 자체 스레드에 맡김 | 병렬 3개 이상 (개발 머신 기준) |
| D: 디스크 소진 | ffmpeg가 0바이트 파일 생성, 재생 정지, 예외 없음 | 잡 시작 전 여유 공간 체크 + 실패 잡 tmp GC + 오래된 버전 디렉터리 정리 정책 | 원본 10편 × 3렌디션 ≈ 수십 GB. 개발 머신에서 며칠이면 도달 `[M]` |
| ngram FULLTEXT 인덱스 비대 | 인덱스가 데이터보다 큼, INSERT 느려짐 | bigram은 토큰 수가 문자 수에 비례 — 검색 대상 컬럼을 제목/장르로 **한정**. 줄거리 전문 색인 금지 | 줄거리까지 색인하면 즉시 |
| 행 UI 호버 확장 + 이미지 다량 | 홈 스크롤 버벅임, 네트워크 워터폴 | `next/image` + `loading="lazy"` + 뷰포트 밖 행은 렌더 지연 | 행 10개 × 20타이틀 = 200 이미지부터 |
| 진행률 저장 폭주 | POST가 초 단위, DB 쓰기 부하 | 10~15초 스로틀 + `sendBeacon` | 동시 시청 10명 × 4req/s = 40 write/s |
| 세그먼트 요청의 캐시 미스 폭주 | 같은 세그먼트를 동시 다중 요청 | `proxy_cache_lock on` `[M]` | 동시 시청자가 같은 지점을 볼 때 |

---

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| 업로드 파일을 그대로 ffmpeg `-i`에 전달 | 조작된 m3u8/컨테이너로 **SSRF + 로컬 파일 읽기**(내용이 출력 영상에 렌더링됨) `[M]` | `-protocol_whitelist file`, 입력 포맷 고정, 우리가 생성한 경로만, `ffprobe` 사전 검증 |
| ffmpeg 명령을 문자열 연결/셸로 구성 | 커맨드 인젝션. `-`로 시작하는 파일명이 옵션으로 해석 | `ProcessBuilder(List<String>)` 전용, 사용자 문자열은 인자 값 위치에만 |
| `profileId`를 요청 본문/쿼리로 받고 소유권 미검증 | **IDOR** — 타 계정 프로필의 이어보기/마이리스트 조회·수정. 키즈 프로필 우회 | 모든 프로필 스코프 API에서 `profile.accountId == session.accountId` 검증. 서버 세션에 활성 프로필 보관 |
| 세그먼트 URL이 무인증 정적 경로 | m3u8 URL만 알면 누구나 시청/핫링크 | 서명 쿠키(경로+만료) 또는 `auth_request`. **Safari 대응 때문에 헤더 토큰은 불가** (Pitfall 11) |
| 미디어 경로에 사용자 입력 결합 | 경로 순회 → 임의 파일 노출 | nginx 정적 서빙(내부 경로 매핑) 또는 `normalize().startsWith(root)` 검증 |
| 관리자 인제스트에 크기/타입 상한 없음 | 디스크 소진 DoS (Pitfall 7과 결합) | 업로드 크기 상한 + 롤 검증 + 레이트 리밋 + 사전 용량 체크 |
| 키즈 프로필을 클라이언트에서만 필터링 | 등급 우회 (M1은 플래그만이지만 서사 손상) | 서버 lolomo 조립에서 필터. BFF가 완성 JSON을 반환하는 이 설계의 이점을 활용 |
| CORS 와일드카드 + credentials 시도 | 브라우저가 거부 → 우회하려다 `*`로 되돌아가 **인증 없는 미디어 공개** | 정확한 오리진 화이트리스트, 또는 same-origin 구조로 CORS 자체 제거 |
| 미디어 파일이 git에 | 저장소 공개 시 되돌릴 수 없는 노출 + 100MB 리젝 | Pitfall 3 |

---

## UX Pitfalls

| Pitfall | User Impact | Better Approach |
|---------|-------------|-----------------|
| 자동재생이 브라우저 정책에 막혀 아무것도 안 일어남 | "고장난 것 같다" — 에러 메시지도 없다 | `muted` 자동재생 + 명시적 재생 버튼 폴백. `play()`의 rejected Promise를 반드시 처리 |
| 버퍼링 표시 없음 | 멈춘 건지 로딩인지 구분 불가 | `waiting`/`playing` 이벤트로 스피너, 15초 이상 지속 시 오류 UI |
| 이어보기가 조용히 동작 | 사용자가 "왜 중간부터 시작하지?" 혼란 | "12:34부터 이어보기" 토스트 + "처음부터" 버튼 (`[M]` 업계 관행) |
| 끝까지 본 콘텐츠가 이어보기 행에 잔류 | 행이 다 본 것들로 오염 | 95% 완료 임계로 제거 (Pitfall 13) |
| 빈 상태 미처리 | 마이리스트 0건/이어보기 0건 = 빈 행 헤더만 덩그러니 | **행 자체를 숨긴다** + 첫 방문용 빈 상태 카피. 추천 결과 0건도 동일 |
| 검색 디바운스 없음 | 타이핑마다 요청, 결과가 깜빡임 | 250~300ms 디바운스 + 이전 요청 취소(AbortController) |
| 화질 전환이 사용자에게 안 보임 | "왜 갑자기 흐려지지?" | 현재 레벨 표시 + 수동 화질 선택(hls.js `currentLevel`) — ABR 데모의 포트폴리오 가치도 여기서 나온다 |
| 행 가로 스크롤이 키보드/스크린리더로 접근 불가 | 접근성 실패 | 화살표 키 핸들링 + `aria-label` + 포커스 가시화 |
| 프로필 전환 후 이전 프로필 데이터가 잠깐 보임 | 신뢰 손상 (특히 키즈) | 프로필 전환 시 클라이언트 캐시 전면 무효화 + 서버 세션 프로필 갱신 후 리다이렉트 |
| 재생 중 새로고침 시 처음부터 | Core Value 실패 | Pitfall 13 전체 |

---

## "Looks Done But Isn't" Checklist

- [ ] **HLS 재생:** Chrome만 확인 — **Safari(네이티브 HLS 경로)에서 검증했나?** 두 경로는 코드가 다르다(Pitfall 12)
- [ ] **ABR:** 렌디션 3개가 "존재"한다 — **DevTools 네트워크 스로틀링으로 실제 전환이 일어나고 끊김이 없는가?** 전환 로그(`LEVEL_SWITCHED`)를 캡처했나
- [ ] **렌디션 정렬:** 3개 `.m3u8`의 `EXTINF` 배열이 **완전히 동일한가?** (diff 스크립트가 CI에 있나)
- [ ] **이어보기:** 저장/복원이 된다 — **탭을 X로 닫았을 때도** 마지막 위치가 남는가(`sendBeacon`)? 95% 시청 후 이어보기 행에서 사라지는가? **다른 프로필로 로그인하면 격리되는가?**
- [ ] **인제스트:** 성공 경로가 된다 — **중간에 앱을 kill하면** 잡이 회수되는가? tmp 잔해가 정리되는가? 서빙 경로에 미완성 파일이 없는가?
- [ ] **인제스트:** **한글/공백/특수문자 파일명**(`테스트 영상 (1080p) #1.mp4`)으로 성공하는가
- [ ] **디스크:** D: 여유가 500MB인 상태에서 잡을 넣으면 **깨끗이 실패하는가**, 0바이트 파일을 만드는가
- [ ] **검색:** 영문 시드로만 테스트 — **"인터스"로 "인터스텔라"가 나오는가**
- [ ] **lolomo:** 응답이 온다 — **쿼리 수를 세어봤나?** 행을 하나 더 추가해도 쿼리 수가 그대로인가
- [ ] **모듈 경계:** `verify()`가 green — **`Type.OPEN`이나 광범위 `allowedDependencies`로 통과시킨 게 아닌가**
- [ ] **캐시:** 재트랜스코딩 후 **강력 새로고침 없이** 새 산출물이 재생되는가
- [ ] **CORS:** 로그인 상태에서 세그먼트가 로드된다 — **로그아웃 상태에서 세그먼트 URL 직접 접근이 차단되는가**
- [ ] **git:** `git count-objects -vH`가 몇 MB인가. `git log --all --diff-filter=A --name-only`에 미디어 확장자가 있는가
- [ ] **라이선스:** 각 타이틀 상세에 CC BY 저작자 표시가 있는가
- [ ] **복원력(P6):** 추천 서비스를 죽였을 때 **홈이 비개인화 행으로 뜨는가**, 500이 뜨는가

---

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| 미디어가 git 히스토리에 (Pitfall 3) | **HIGH** | `git filter-repo --strip-blobs-bigger-than 5M` → force push → **모든 클론 재클론**. 협업 중이면 조율 필요. 공개 저장소였다면 GitHub 캐시 제거 요청까지 |
| 렌디션 키프레임 미정렬 (Pitfall 2) | **MEDIUM** | 인코딩 프로파일 수정 후 **전량 재트랜스코딩**. 버전 디렉터리 구조가 있으면(Pitfall 4) v2로 만들고 포인터만 교체 — 무중단. 없으면 다운타임 |
| Modulith 사이클 누적 (Pitfall 5) | **MEDIUM→HIGH** | 위반 1~5건이면 ID 참조 전환으로 몇 시간. 20건+면 엔티티 재배치 = 며칠. **`OPEN`으로 덮은 상태에서 되돌리는 것이 가장 비싸다** |
| lolomo N+1 (Pitfall 6) | **LOW→MEDIUM** | 쿼리 레벨 수정(2-쿼리 패턴)이라 API 계약 불변. BFF가 조립을 독점하므로 수정 지점이 한 곳 — **이 설계 결정의 배당금** |
| 고아 트랜스코딩 잡 (Pitfall 7) | **LOW** | `UPDATE transcode_job SET status='QUEUED' WHERE status='PROCESSING' AND leased_until < NOW()` — lease 컬럼이 처음부터 있으면. **없으면 스키마 마이그레이션 + 워커 재작성 = MEDIUM** |
| ngram_token_size 잘못 설정 (Pitfall 10) | **LOW→MEDIUM** | 서버 파라미터 변경 + 재시작 + FULLTEXT 인덱스 DROP/ADD. 데이터는 무손실. 카탈로그가 작아 재구축이 빠름 — **지금 고치면 싸고 나중엔 다운타임** |
| 캐시된 죽은 플레이리스트 (Pitfall 4) | **LOW** | nginx `proxy_cache_purge` 또는 캐시 디렉터리 삭제 + 헤더 정책 수정. 단 **브라우저 캐시는 회수 불가** — max-age가 길었으면 사용자 쪽은 만료를 기다려야 함 |
| 진행률 0 덮어쓰기 (Pitfall 13) | **LOW (코드) / 복구 불가 (데이터)** | 클라이언트 가드 추가는 쉽다. **이미 날아간 진행률은 복원 불가** — 그래서 P1에서 잡아야 한다 |
| Safari에서 인증 실패 (Pitfall 11) | **MEDIUM** | 헤더 토큰 → 쿠키로 전환은 인증 레이어 + nginx + 플레이어 3곳 동시 수정. **P1에서 방식을 결정하면 0** |

---

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| 1. 재생이 마지막으로 밀림 | **P1** (로드맵 구조 자체) | 로드맵 리뷰: P1 종료 조건에 "브라우저 재생 + 새로고침 이어보기"가 있는가 |
| 3. 미디어가 git에 | **P0** | `git check-ignore -v media/`가 매칭. pre-commit 훅 5MB 거부 테스트 |
| 5. Modulith 경계 침식 | **P0** | `ModularityTests.verify()` CI green + 6모듈 전부 `allowedDependencies` 선언됨 + `Type.OPEN` 0건 (grep 게이트) |
| 10. MySQL 한국어 검색 | **P0**(설정·시드) → **P5**(구현) | compose에 `--ngram-token-size` 존재. 시드에 한글 제목 ≥5건. `"인터스" → "인터스텔라"` 통합 테스트 |
| 14. docker init 미실행 | **P0** | 볼륨 있는 상태로 `up` → Flyway가 마이그레이션 적용 로그. README에 `down -v` |
| 17. 라이선스 표기 | **P0**(스키마) → **P3**(UI) | `title` 테이블에 `attribution`/`license` NOT NULL |
| 4. m3u8 캐시 정책 | **P1**(헤더) → **P2**(버전 디렉터리) | `curl -I *.m3u8` vs `*.ts`의 Cache-Control 상이. 재트랜스코딩 후 하드리프레시 없이 반영되는 E2E |
| 11. CORS/Range/인증 | **P1**(결정) → **P4**(적용) | 로그아웃 상태 세그먼트 직접 GET → 403. Safari 재생 E2E |
| 12. hls.js 라이프사이클 | **P1** | `next build` 성공. StrictMode에서 세그먼트 요청이 2배가 아님. 이어보기 20회 반복 성공(레이스 없음) |
| 13. 이어보기 엣지 | **P1**(기본) → **P4**(프로필·완료) | 표의 7개 규칙 각각에 AC 1건. 탭 강제 종료 E2E |
| 16. Tomcat 세그먼트 서빙 | **P1** | 컨트롤러에 미디어 스트리밍 코드 0. nginx access log에 세그먼트 요청 |
| 2. 키프레임 미정렬 | **P2** | 렌디션 `EXTINF` diff 스크립트 CI green + 스로틀링 하 전환 E2E |
| 7. 잡 큐 실패 모드 | **P2** | 잡 테이블에 `attempts`/`leased_until`. 워커 kill → 60초 내 자동 회수 테스트. 디스크 부족 시 clean fail |
| 8. ProcessBuilder 데드락 | **P2** | `-loglevel debug`(수 MB 로그)로 장편 완주 회귀 테스트 |
| 9. Windows 경로/인코딩 | **P2** | `테스트 영상 (1080p) #1.mp4` 픽스처 CI green. 경로 길이 <200 어서션 |
| 15. 인제스트 보안 | **P2** | 악성 m3u8 픽스처 업로드 → 거부. `Runtime.exec(String)` grep 0건 |
| 6. lolomo N+1 | **P3** | 홈 1회 요청 쿼리 카운트 ≤ N 테스트 (행 개수 늘려도 불변) |
| 복원력 폴백 | **P6** | 추천 모듈 강제 실패 주입 → 홈 200 + 비개인화 행 |
| 전 항목 회귀 | **P7** | Playwright 크로스브라우저(Chrome/Safari) + 컨테이너 kill 카오스 + 스로틀링 |

---

## Confidence & Gaps (정직 표기)

**검증 못 한 것 / 실측 필요:**
- **Apple HLS Authoring Spec 원문** — WebFetch 2회 실패(JS 렌더 페이지, 본문 미노출). "VOD 6초 세그먼트", "`EXT-X-INDEPENDENT-SEGMENTS`", "모든 variant에 `CODECS` 필수", "I-frame 플레이리스트로 스크러빙 지원", "IDR 2초 간격"은 **2차 출처(Mux, Apple WWDC 2016 세션 510 PDF, OTTVerse) 기반 MEDIUM**. P2 착수 시 원문을 직접 확보해 설계 문서에 인용하라 — 이 프로젝트의 "인용-강제 리서치 규율"이 적용되는 지점이다.
- **`mediastreamvalidator`의 Windows 미지원** — MEDIUM. Apple HTTP Live Streaming Tools는 macOS 배포로 알려져 있으나 1차 확인 못 함. P2 착수 시 실측 후 대체 검증 스크립트 확정.
- **D: 드라이브 실제 소진 속도** — 원본 편수 × 렌디션 × 길이에 의존. 추정만 했고 실측 없음. P2 첫 잡 실행 후 실제 비율(원본 대비 산출물 배수)을 측정해 용량 체크 계수를 보정하라.
- **Spring Modulith 이벤트 발행 레지스트리 결함** (#395 외부화 실패 시 completed 마킹, #926 재시작 중복) — 이슈 존재는 확인했으나 **1.1.x/1.2.x에서 수정 여부는 미확인**. M1에서 모듈 간 이벤트를 필수 경로로 쓸 계획이면 버전별 픽스 상태를 먼저 확인하라. (권장: M1은 직접 호출, 이벤트는 SPIRAL 턴에서)
- **한국어 ngram vs LIKE 실측 비교** — 미측정. Pitfall 10의 권고(둘 다 구현 후 비교)는 **가설**이며, P5에서 실제 측정이 결론을 내야 한다.

**알려진 결함 수:** 이 문서가 커버하지 못한 영역 — 실험 플래그/A/B 배정(P7), Prometheus/Grafana 계측의 카디널리티 함정, Playwright에서 video 요소 테스트의 flake 특성(하네스 메모리 `reference_parallel_suite_flake_class` 참조). 잔여 리스크는 비-제로다.

---

## Sources

**HLS / 인코딩**
- [MySQL/Apple 대체] Mux — HLS Tags 레퍼런스 (EXT-X-INDEPENDENT-SEGMENTS, CODECS, I-frame playlist): https://www.mux.com/articles/hls-ext-tags `[M]`
- Apple WWDC 2016 세션 510 "Validating HTTP Live Streams" (PDF, Apple 배포): https://devstreaming-cdn.apple.com/videos/wwdc/2016/510ndmh9wkcvzneegv2/510/510_validating_http_live_streams.pdf `[M]`
- MpegFlow — Keyframe interval tuning for HLS: GOP discipline and segment alignment: https://www.mpegflow.com/recipes/keyframe-interval-tuning-for-hls `[M]`
- FFmpeg 공식 — hls / segment 머서 옵션(hls_segment_filename 경로 동작): https://ffmpeg.org/ffmpeg-formats.html#hls-2 `[H]`
- 5centsCDN — Encoding ladder 가이드: https://www.5centscdn.net/blog/encoding-ladder/ `[L]`
- Unified Streaming — fMP4 HLS 패키징 (EXT-X-MAP / BYTERANGE): https://docs.unified-streaming.com/documentation/package/fmp4-hls.html `[M]`

**hls.js**
- hls.js API 문서 (xhrSetup / fetchSetup / startLoad(startPosition) / 에러 처리·recoverMediaError / loop loading 경고): https://github.com/video-dev/hls.js/blob/master/docs/API.md `[H]`
- hls.js README (`canPlayType && 'ManagedMediaSource' in window` 네이티브 분기 권장): https://github.com/video-dev/hls.js/blob/master/README.md `[H]`
- hls.js #4556 — React 리렌더 시 메모리 누수: https://github.com/video-dev/hls.js/issues/4556 `[H]`
- hls.js #1119 — detach/attach 시 start-position 부정확: https://github.com/video-dev/hls.js/issues/1119 `[H]`

**Spring Modulith / JPA**
- Spring Modulith 레퍼런스 — Fundamentals (모듈 탐지, @NamedInterface가 unnamed interface에서 제거하는 동작, allowedDependencies, Type.OPEN): https://docs.spring.io/spring-modulith/reference/fundamentals.html `[H]`
- Spring Modulith — Verifying Application Module Structure: https://docs.spring.io/spring-modulith/reference/verification.html `[H]`
- Spring Modulith Discussion #557 — "How to solve 'cycle detected' problem with common module and JPA annotations": https://github.com/spring-projects/spring-modulith/discussions/557 `[H]`
- Spring Modulith #395 — 외부화 실패에도 publication이 completed로 마킹: https://github.com/spring-projects/spring-modulith/issues/395 `[H]`
- Spring Modulith #926 — 재시작 시 미발행 이벤트 중복: https://github.com/spring-projects/spring-modulith/issues/926 `[H]`
- Spring Modulith #971 — Spring Boot 호환 매트릭스 (Boot 3.2 ↔ Modulith 1.1.x): https://github.com/spring-projects/spring-modulith/issues/971 `[M]`
- Vlad Mihalcea — Spring Data JPA MultipleBagFetchException: https://vladmihalcea.com/spring-data-jpa-multiplebagfetchexception/ `[H]`
- Vlad Mihalcea — HHH000104 in-memory pagination 수정법: https://vladmihalcea.com/fix-hibernate-hhh000104-entity-fetch-pagination-warning-message/ `[H]`

**MySQL**
- MySQL 8.0 Reference — 14.9.8 ngram Full-Text Parser (token size read-only·재구축·phrase 변환·wildcard·CJK stopword): https://dev.mysql.com/doc/refman/8.0/en/fulltext-search-ngram.html `[H]`
- MySQL 8.0 Reference — 14.9.5 Full-Text Restrictions (엔진·파티션·문자셋·MATCH 컬럼 일치): https://dev.mysql.com/doc/refman/8.0/en/fulltext-restrictions.html `[H]`
- MySQL 8.0 Reference — 14.9.6 Fine-Tuning Full-Text Search (인덱스 재구축): https://dev.mysql.com/doc/refman/8.0/en/fulltext-fine-tuning.html `[H]`
- MySQL Bug #92705 — ngram이 token size보다 짧은 단어를 못 찾음: https://bugs.mysql.com/bug.php?id=92705 `[H]`

**인프라 / 운영**
- Bugzilla 1648075 — no-cache 헤더에도 m3u8이 캐시됨: https://bugzilla.mozilla.org/show_bug.cgi?id=1648075 `[H]`
- SRS — HLS Cluster (nginx m3u8/ts 캐시 시간 분리): https://ossrs.net/lts/en-us/docs/v6/doc/nginx-for-hls `[M]`
- docker-library/mysql #276 — initdb.d가 기존 데이터 디렉터리에서 미실행: https://github.com/docker-library/mysql/issues/276 `[H]`
- docker/for-win #2996 — 바인드 마운트 내용 불안정: https://github.com/docker/for-win/issues/2996 `[M]`
- Microsoft Learn — Maximum Path Length Limitation (MAX_PATH 260): https://learn.microsoft.com/en-us/windows/win32/fileio/maximum-file-path-limitation `[H]`
- GitHub Docs — 대용량 파일/100MB 제한 및 LFS 이전: https://docs.github.com/en/repositories/working-with-files/managing-large-files `[H]`

**잡 큐 / ffmpeg 운영**
- immich #27973 — 트랜스코딩 프로세스 kill 시 원자적 rename 부재로 파일 손상: https://github.com/immich-app/immich/issues/27973 `[H]`
- MpegFlow — Running FFmpeg at scale: queue, retry, and the audit trail: https://www.mpegflow.com/blog/running-ffmpeg-at-scale-queue-retry-audit `[M]`
- DZone — Java ProcessBuilder: Deadlocks, Zombies, and the 64 KB Wall: https://dzone.com/articles/java-processbuilder-deadlocks-zombies `[M]`
- Java `Process` API 문서 (스트림 미배수 시 블로킹/데드락 명시): https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Process.html `[H]`
- FFmpeg-devel — Windows UTF-8 파일명 지원 논의: https://ffmpeg.org/pipermail/ffmpeg-devel/2012-April/123449.html `[M]`
- ffmpeg-python #90 — Windows 비-ASCII 파일명 실패: https://github.com/kkroening/ffmpeg-python/issues/90 `[M]`

**보안**
- "SSRF vulnerability via FFmpeg HLS processing": https://krevetk0.medium.com/ssrf-vulnerability-via-ffmpeg-hls-processing-f3823c16f3c7 `[M]`
- Black Hat — "Viral Video: Exploiting SSRF in Video Converters": https://www.classcentral.com/course/youtube-viral-video-exploiting-ssrf-in-video-converters-185485 `[M]`
- awslabs/amplify-video #323 — 서명 URL HLS가 iOS/Safari 네이티브에서 재생 불가: https://github.com/awslabs/amplify-video/issues/323 `[M]`
- AWS Samples — CloudFront에서 HLS 매니페스트를 서명 URL로 보호: https://github.com/aws-samples/amazon-cloudfront-protecting-hls-manifest-with-signed-url `[M]`

**도메인 참조 (Netflix 패턴)**
- Netflix TechBlog — Learning a Personalized Homepage (lolomo 행 선택/랭킹): http://techblog.netflix.com/2015/04/learning-personalized-homepage.html `[M]`
- Netflix TechBlog — System Architectures for Personalization and Recommendation: http://techblog.netflix.com/2013/03/system-architectures-for.html `[M]`
- Jellyfin #13289 — resume percentage 100% 설정 시 "다 본 콘텐츠"가 이어보기에 잔류: https://github.com/jellyfin/jellyfin/issues/13289 `[H]`

---
*Pitfalls research for: 넷플릭스형 VOD 스트리밍 서비스 (moabom)*
*Researched: 2026-08-06*
