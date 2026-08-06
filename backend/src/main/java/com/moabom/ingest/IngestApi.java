package com.moabom.ingest;

/**
 * 관리자 인제스트 공개 API 계약 자리. 다른 도메인 모듈은 ingest 를 직접 부르지 않고,
 * catalog 가 ingest :: event 를 구독한다 (ARCHITECTURE §2 — 유일한 비동기 경계).
 * 메서드는 소유 phase 가 채운다 (ingest = Phase 8).
 */
public interface IngestApi { }
