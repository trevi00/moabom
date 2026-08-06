package com.moabom.playback;

/**
 * bff 전용 공개 API 계약 자리. 다른 도메인 모듈은 이것이 아니라 spi 포트를 쓴다 (ARCHITECTURE §2.1).
 * 메서드는 소유 phase 가 채운다 (playback = Phase 4).
 */
public interface PlaybackApi { }
