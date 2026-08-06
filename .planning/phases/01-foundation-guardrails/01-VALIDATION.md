---
phase: 1
slug: foundation-guardrails
status: draft
nyquist_compliant: false
wave_0_complete: false
created: 2026-08-06
---

# Phase 1 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.
> Source: `01-RESEARCH.md` §Validation Architecture (V-SC1a~V-CP5, 26 items).

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 5 (Gradle) — backend / (FE 스텁은 P1 테스트 대상 아님) |
| **Config file** | none — Wave 0 installs (backend/build.gradle 신규) |
| **Quick run command** | `cd backend && ./gradlew test --tests "*ModularityTests*"` |
| **Full suite command** | `cd backend && ./gradlew build` |
| **Estimated runtime** | ~120 seconds (Testcontainers mysql 포함) |

---

## Sampling Rate

- **After every task commit:** Run quick run command
- **After every plan wave:** Run full suite command
- **Before `/kha-verify-uat`:** Full suite must be green
- **Max feedback latency:** 180 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| (planner가 PLAN.md 태스크 확정 후 채움 — RESEARCH §Validation Architecture V-매핑 26종 참조) | | | ARCH-01, ARCH-05 | | | | | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] backend/ Gradle 프로젝트 + wrapper 8.14.5 (테스트 인프라 전무 — planning 커밋 5개뿐)
- [ ] `ModularityTests` — ApplicationModules.verify() (ARCH-01)
- [ ] Testcontainers mysql 배선 (`SHOW VARIABLES` 파라미터 검증 — CP-10)
- [ ] `.gitattributes` + gradlew +x (CRLF/실행권한 — CI 사망 방지)

*RESEARCH §Wave 0 gaps: 11종 — 상세는 01-RESEARCH.md 참조.*

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| full 프로필 실기동 (V-SC1b/c, V-D02) | ARCH-05 | bootBuildImage 수 분 소요 — D-09 최소 코어 게이트와 충돌. CI는 `docker compose --profile full config -q` 프록시만 | phase 게이트에서 1회: 선행 빌드 → `docker compose --profile full up` → 헬스/FE 스텁 확인 |
| 반사실 프로브 2건 (V-SC2d, V-SC4d) | ARCH-01, SC4 | "위반이 실패한다"는 반사실 요구 — 게이트 존재≠작동 | 경계 위반 커밋/미디어 파일 커밋을 일부러 만들어 붉어짐 확인 후 revert, 로그 기록 |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 180s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
