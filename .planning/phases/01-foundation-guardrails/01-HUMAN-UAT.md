---
phase: 01-foundation-guardrails
plan: 07
kind: human-uat
status: partial
opened: 2026-08-06
closes_when: "아래 잔여 5항목에 operator 가 직접 확인 표시를 남길 때"
---

# Phase 1 사람 검수 (UAT) — 잔여 항목 대장

> **왜 이 파일이 있는가.** 01-07 Task 3 은 `checkpoint:human-verify` 였고, 승인은
> **오케스트레이터가 HTTP 대리 검증으로** 내렸다(브라우저 프로필이 타 세션 점유 상태였다).
> 대리 검증은 "서버가 옳은 것을 돌려준다"를 증명하지만 **"사람 눈에 그렇게 보인다"를 증명하지 않는다.**
> 그 간극을 승인 문구 속에 묻지 않고 `status: partial` 로 남겨 `/kha-status` 에 계속 뜨게 한다.
> 이것이 닫히기 전까지 Phase 1 의 사람 검수는 **부분 충족**이다.

## 승인 상태

| | |
|---|---|
| 판정 | **승인 (조건부)** — 01-07 은 이 조건으로 완료 처리됨 |
| 승인 주체 | 오케스트레이터 (auto-advance 설정 하의 기계 실측 근거) |
| 승인일 | 2026-08-06 |
| 미충족 잔여 | 아래 5항목 (operator 최종 검토 대상) |

## 기계로 닫힌 항목 (대리 검증 — 재확인 불요)

| 항목 | 실측 근거 | 판정 |
|---|---|---|
| 1. `:3000` 헬스 렌더 | HTML 에 "모아봄" + `__next` 존재. `/api/health` 2회 샘플의 `ts` 가 상이 (`06:20:48.641` → `06:20:51.005`) → **캐시된 옛 응답 아님** (T-1-35 완화 실효) | PASS |
| 1b. CORS 프리플라이트 부재 | `HealthProbe` 가 **상대 URL** 로 호출한다 → 프리플라이트가 구조적으로 발생 불가. D-02 단일 오리진 규약이 코드 수준에서 성립 | PASS |
| 2. Prometheus 타깃 | `/api/v1/targets` 실측 — `moabom-app-container` **up** / `moabom-app-host` down(설계상 정상) / `prometheus` up | PASS |
| 3. Grafana 접근 | `:3002` 에 `POST /login` `admin`/`admin` → **HTTP 200**. `/api/health` → `database ok`, v13.1.2 | PASS |

## 잔여 — operator 가 직접 확인할 것 (status: partial 의 실체)

- [x] **U-1. `:3000` 육안 확인.** ✅ 2026-08-06 operator 확인 — 렌더 정상, 새로고침 시 ts 갱신. (부기: "헬스체크 뷰뿐"이라는 관찰은 정확하며 D-05 의도 — 실 UI는 P2 재생 페이지·P5 행 홈(목업 선행).) 브라우저로 http://localhost:3000 을 열어 "모아봄" 제목과
      회색 박스의 JSON 이 **사람 눈에 정상으로 보이는지**. (HTML 바이트가 옳은 것과 렌더가 옳은 것은 다르다 —
      CSS 붕괴·폰트 누락·박스 밖 오버플로는 curl 로 안 보인다.)
- [x] **U-2. `:9090/targets` 육안 확인.** ✅ 2026-08-06 operator 확인 — container UP(초록)·host DOWN(설계 정상 인지 완료). `moabom-app-container` 가 초록인지. 빨간 `moabom-app-host` 는
      **설계상 정상**임을 알고 보는지 (모르면 다음 사람이 이걸 고장으로 오인한다).
- [x] **U-3. `:3002` 로그인 화면 육안 확인.** ✅ 2026-08-06 operator 확인 — 로그인 성공, admin 비밀번호를 기본값에서 변경(값은 로컬 `.env`의 `GRAFANA_PASSWORD`로만 영속화 — gitignored, repo 미기록. grafana는 무볼륨이라 env가 유일한 영속 경로).
- [x] **U-4. `01-COUNTERFACTUAL-LOG.md` 잔여 표기 동의.** ✅ 2026-08-06 operator 수용 — 3한계(--no-verify 우회·실 boundary=CI·미설정 클론 무훅)를 알려진 잔여로 인정, 후속 요청 없음. 특히 "`--no-verify` 로 우회 가능 /
      실 boundary 는 CI 히스토리 스캔 / `core.hooksPath` 미설정 시 훅 자체가 없음" 세 줄에 동의하는지.
      (오케스트레이터는 이것이 하네스 위협모델 정직 규율과 정합한다고 **잠정** 수용했다.)
- [ ] **U-5. `docs/adr/0001-runtime-versions.md` §9 설득력 판단.** EOL 스택 수용 근거가
      **포트폴리오 리뷰어에게** 설득력 있는지. 이것은 기술 판정이 아니라 **청중 판정**이라 기계가 대신할 수 없다.

## 검수 시점의 환경 사실 (재현하려면 알아야 하는 것)

- **Grafana 는 `:3002`.** `3001` 을 무관 프로젝트의 `exoplanet-grafana` 컨테이너가 점유해
  `docker compose --profile full up -d --wait` 가 바인드 실패로 **중단**됐다
  (`failed to bind host port for 127.0.0.1:3001:...:3000/tcp: address already in use`).
  외부 워크로드라 종료하지 않고, **저장소 밖 임시 오버레이**로 grafana 만 `3002` 로 재매핑해 검증했다.
  `compose.yaml` 은 수정하지 않았다 → **3001 점유가 해소되면 문서 그대로 `:3001` 이다.**
- **호스트 8080 이중 LISTEN.** `httpd.exe`(PID 5140)와 Docker 가 동시에 `0.0.0.0:8080` 에 LISTEN 중이고
  출처에 따라 응답자가 갈린다(`localhost` → Spring / 컨테이너 gateway → Apache).
  **결정 = (c) 현 상태 수용, Phase 2 이월** (2026-08-06). 근거: (a) 무관 서비스 정지는 operator 소관,
  (b) 앱 포트 이동은 설계 변경이라 뒤집을 근거가 없다. STATE 블로커로 이월됨.

## 이 파일을 닫는 법

U-1~U-5 를 확인한 뒤 체크박스를 채우고 frontmatter 의 `status: partial` 을 `status: complete` 로 바꾼다.
부분만 확인했다면 **채운 것만 체크하고 partial 을 유지한다** — 일괄 체크는 이 대장의 존재 이유를 없앤다.
