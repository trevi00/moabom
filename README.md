# 모아봄 (moabom)

넷플릭스 클론 — Netflix 테크 블로그의 공개 아키텍처 패턴을 참조 설계로 삼아
스트리밍·개인화·복원력을 축소 스케일로 재구현한 포트폴리오 프로젝트입니다.

> 이 프로젝트는 Netflix와 무관하며 Netflix의 상표·로고·워드마크를 사용하지 않습니다.
> 참조하는 것은 공개된 아키텍처 패턴과 행(row) 기반 레이아웃 문법뿐입니다.

## 콘텐츠 라이선스

재생 자산은 Blender Foundation 오픈무비(Big Buck Bunny, Sintel, Tears of Steel,
Elephants Dream, Cosmos Laundromat)를 사용합니다.
© Blender Foundation | blender.org — CC BY 3.0

저작자 표시는 의무입니다. 타이틀 메타데이터의 `attribution` / `license` 컬럼과
앱 크레딧 표기가 그 이행 수단입니다.

## Quick start

```bash
docker compose up          # 인프라 기동 (mysql · nginx · prometheus · grafana)
docker compose down        # 정지
docker compose down -v     # 볼륨까지 삭제 = 스키마/시드 리셋 (Flyway 가 재적용)
```

`down -v`가 리셋 명령인 이유: MySQL 이미지는 `/var/lib/mysql`에 데이터가 있으면 초기화를
건너뛴다. 스키마·시드의 단일 진실은 Flyway이고, 리셋은 볼륨 삭제다 (PITFALLS Pitfall 14).

**래퍼 스크립트를 만들지 않습니다** — Makefile·`start.sh`·`start.bat`·Gradle 커스텀
`composeUp` 태스크는 이 저장소에 없습니다. 기동·정지·리셋은 표준 `docker compose` 커맨드
그대로입니다. 배워야 할 어휘가 하나 줄어드는 대신, 우리 전용 어휘를 하나도 만들지 않습니다.

## 전체 서비스 한 번에 (데모/검수)

```bash
cd backend && ./gradlew bootBuildImage --imageName=moabom-app:local   # 선행 1회
cd .. && docker compose --profile full up
```

브라우저는 http://localhost:3000 만 봅니다 (단일 진입 오리진).

**compose는 Gradle을 실행할 수 없다.** 따라서 여기서 말하는 "한 커맨드"는 *앱 이미지를 한 번
빌드한 뒤의* compose 한 커맨드다. `pull_policy: never`가 이미지 부재를 즉시·명확히 실패시킨다
(레지스트리 pull 시도 후의 인증 오류로 헷갈리지 않는다).

## 평시 개발

```bash
cd backend && ./gradlew bootRun     # spring-boot-docker-compose 가 인프라를 자동 기동
cd frontend && npm run dev          # http://localhost:3000
```

## 클론 후 1회

```bash
git config core.hooksPath .githooks   # 미디어/대용량 커밋 차단 훅 활성화
cp .env.example .env                  # MEDIA_ROOT 등 (실제 .env 는 커밋 금지)
```

`core.hooksPath`는 저장소가 아니라 로컬 git 설정이므로 **클론마다 1회** 필요합니다.
설정하지 않으면 훅은 그냥 돌지 않습니다 — 조용히 실패하는 종류라 이 줄을 건너뛰지 마세요.

`.env.example`은 저장소 루트에 있습니다(Plan 01-05에서 추가). compose가 이 디렉터리의 `.env`를
읽어 `MEDIA_ROOT`·비밀번호를 치환합니다 — 복사하지 않으면 `${MEDIA_ROOT:-D:/moabom-media}`의
기본값이 그대로 쓰입니다.

⚠️ 프런트엔드의 `BACKEND_ORIGIN`/`MEDIA_ORIGIN`은 **런타임 변수가 아닙니다**: `next build` 시점에
목적지가 구워집니다(01-03 실측). full 프로필에서는 compose의 `web.build.args`가 정본이고,
`npm run dev`로 값을 바꾸려면 루트가 아니라 `frontend/.env`에 둬야 합니다.

## 미디어 루트

미디어는 저장소 **밖**에 있습니다: `D:/moabom-media/`
경로는 `.env` 의 `MEDIA_ROOT` 로 주입됩니다. 물리적으로 git 이 볼 수 없으므로 사고가 불가능합니다.

### 미디어 격리 가드레일 (3중)

| 층 | 수단 | 성격 |
|---|---|---|
| 1 | 미디어 루트가 저장소 밖(`D:/moabom-media/`) | 물리 격리 — 사고가 성립하지 않는다 |
| 2 | `.gitignore` 경로 스코프 패턴 | 우발적 `git add .` 차단 |
| 3 | `.githooks/pre-commit` (미디어 확장자 + 5MB) | push 전에 값싸게 잡는다 |

정직 표기: 이 저장소에는 이미 planning 커밋이 존재하므로 "첫 커밋 이전"은 사후적으로 만족할 수
없습니다. 보장되는 것은 **가드레일이 첫 push 이전에 존재한다**는 것입니다.

로컬 훅은 defense-in-depth이지 boundary가 아닙니다 — `git commit --no-verify`로 우회됩니다.
실제 boundary는 CI의 히스토리 전체 blob 스캔(push 시점)입니다.

⚠️ `.gitignore`에 전역 `*.ts`를 넣지 마세요. `.ts`는 MPEG-TS 세그먼트이자 TypeScript 확장자라
`frontend/`의 소스 전체가 조용히 무시됩니다. MPEG-TS는 경로 스코프(`media/**/*.ts`)로만 배제하고,
5MB 크기 게이트가 2차 방어입니다.

## 아키텍처 진화 서사

Spring Modulith 모듈리스 단일 배포로 시작합니다. 물리 MSA·Redis·DB 읽기 복제본은
"처음부터 넣는 것"이 아니라 **부하를 주입해 병목을 실측한 뒤** 증설합니다 (M1 후반 phase).
Modulith 경계 검증(`ApplicationModules.verify()`)이 그 분리를 위한 준비물입니다.
