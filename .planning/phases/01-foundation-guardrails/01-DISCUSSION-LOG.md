# Phase 1: 기반 + 가드레일 - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-08-06
**Phase:** 1-기반 + 가드레일
**Areas discussed:** 스코프 재편(사전 게이트), 기동 토폴로지, 레포 구조·FE 시점, Git·CI 운영, 스키마·시드 상세

---

## 스코프 재편 (사전 게이트)

사용자가 영역 선택 시 자유 텍스트로 "대용량 트래픽 MSA DB이중화 레디스 서킷브레이커 등등을 도입할것임"을 추가 → 확정 문서와 대조 후 게이트 질문 2건.

| Option | Description | Selected |
|--------|-------------|----------|
| v2 백로그 + P1은 분리-대비 경계 | M1은 확정 로드맵대로, 증설 요소는 v2 백로그 | |
| M1 로드맵 재편 | v1에 반영 — 로드맵 수준 재편 필요 | ✓ |
| P1에 일부 선반영 | compose에 Redis/replica 미리 포함 | |

재편의 모양:

| Option | Description | Selected |
|--------|-------------|----------|
| 후반 증설 — 진화 서사 | Phase 1~8 유지 + Phase 9~ 증설 (부하 실측→Redis→DB 복제본→물리 분리) | ✓ |
| 처음부터 MSA | P1부터 물리 분리 — 리서치·로드맵 무효화, 재수립 전환 | |
| 중간 — 인프라만 선행 | 앱은 Modulith 유지, P1 compose에 Redis·replica 포함 | |

**User's choice:** 후반 증설 (진화 서사)
**Notes:** 서킷브레이커는 기존 Phase 6에 이미 있음을 확인. 로드맵 반영은 /kha-add-phase로 후속.

---

## 기동 토폴로지

| Option | Description | Selected |
|--------|-------------|----------|
| 프로필 이원화 | dev=인프라 compose+bootRun, demo=--profile full로 앱 컨테이너까지 | ✓ |
| 항상 전부 컨테이너 | compose up 하나만 — 개발 루프 느림 | |
| 인프라만 compose | SC1 완화 필요 | |

| Option | Description | Selected |
|--------|-------------|----------|
| 단일 진입 오리진 확정 | :3000만 노출, /api→8080·/media→nginx rewrites, P1에서 못박음 | ✓ |
| 오리진 분리 + CORS | STACK.md 실패 조건이라 비권장 | |
| Phase 2로 미룸 | 배선 규약을 P2 실측으로 | |

| Option | Description | Selected |
|--------|-------------|----------|
| docker compose 그대로 | 표준 커맨드 = 인터페이스, README 3줄 | ✓ |
| 스크립트 래퍼 | .sh+.ps1 이중 유지 부담 | |
| Gradle 태스크 | 결합 증가 | |

---

## 레포 구조·FE 시점

| Option | Description | Selected |
|--------|-------------|----------|
| backend/ + frontend/ 모노레포 | 루트=compose·docs, 후반 물리 분리 시 services/ 확장 자연 | ✓ |
| 루트 = Gradle 단일 | FE 2급 시민화 | |
| FE/BE 별도 레포 2개 | 단일 기동·단일 서사와 마찰 | |

| Option | Description | Selected |
|--------|-------------|----------|
| Phase 1에 얇게 포함 | Next 16 + rewrites + 헬스 스텁 1페이지 — 오리진 규약 실동작 검증 | ✓ |
| Phase 2로 미룸 | rewrites 실증이 P2로 밀림 | |

| Option | Description | Selected |
|--------|-------------|----------|
| full 프로필에 FE 포함 | 데모에서 Next도 컨테이너 — 한 커맨드 전체 서비스 | ✓ |
| FE는 항상 수동 기동 | 데모 커맨드 2개 | |
| 후반 phase에서 결정 | | |

---

## Git·CI 운영

| Option | Description | Selected |
|--------|-------------|----------|
| public 생성 | 포트폴리오 목적, 가드레일이 첫 커밋부터 강제 | ✓ |
| private → 나중 공개 | 공개 시점 전체 이력 재감사 부담 | |
| 로컬만 유지 | CI 불가 — SC2 위반 | |

| Option | Description | Selected |
|--------|-------------|----------|
| main 직접 push (solo) | master→main, Conventional Commits, push 트리거 CI | ✓ |
| PR-only main | nailog 방식 — GSD 잦은 커밋과 오버헤드 | |
| 하이브리드 | planning=직접, 코드=PR — 규칙 복잡 | |

| Option | Description | Selected |
|--------|-------------|----------|
| 최소 코어 게이트 | build+verify()+단위+미디어 차단+dependencyInsight 회귀 | ✓ |
| 22-스테이지 전면 채택 | 빈 골격에 과함 | |
| build+verify만 | 가드레일 산출물 누락 위험 | |

| Option | Description | Selected |
|--------|-------------|----------|
| pre-commit 훅 추가 | 미디어 확장자/크기 차단만 로컬 (verify()는 CI에만) | ✓ |
| CI만 | push 후 발견 시 이력 세탁 비용 | |

---

## 스키마·시드 상세

| Option | Description | Selected |
|--------|-------------|----------|
| title 최소 골격만 | title(+장르 최소)+attribution/license, 나머지는 소유 phase 증분 | ✓ |
| catalog 전반 선행 | 미확정 설계 선점 위험 | |
| 시드 검증 전용 최소 | P3에서 바로 증분 필요 | |

| Option | Description | Selected |
|--------|-------------|----------|
| 한글 골든 + Blender 메타 | 한글 ≥5건(메타-only) + Blender 실자산 행(CC-BY, 에셋 경로는 P2) | ✓ |
| 한글 골든만 | | |
| 더미 대량 포함 | P5 사안, 과함 | |

| Option | Description | Selected |
|--------|-------------|----------|
| 본선 V 마이그레이션 | 모든 환경이 데모 성격 — SC3 문구 그대로 | ✓ |
| 프로필 분기 locations | 구분 실익 없음 | |
| R__ repeatable 시드 | 불필요 복잡도 | |

| Option | Description | Selected |
|--------|-------------|----------|
| 규율만 P1, 테이블은 첫 사용 phase | 자동생성 금지 규율만 ADR에, DDL은 P3 예상 | ✓ |
| P1에서 미리 생성 | 쓰는 코드 없음 | |
| 자동 생성 설정 | 단일 진실 규율 위반 | |

---

## Claude's Discretion

compose 파일 구성·헬스체크 배선, ADR 형식/위치, pre-commit 훅 구현 방식·임계값, CI yml 구조, Next 스캐폴딩 세부, 모듈 내부 패키지 구조·allowedDependencies 방식, title 컬럼 상세 — CONTEXT.md §Claude's Discretion 참조.

## Deferred Ideas

- M1 후반 증설 Phase 9~ (부하 실측 → Redis → DB 읽기 복제본 → 모듈 물리 분리) — /kha-add-phase로 로드맵 반영 필요
