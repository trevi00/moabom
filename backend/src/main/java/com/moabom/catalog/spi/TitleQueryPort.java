package com.moabom.catalog.spi;

import java.util.List;

/**
 * 다른 도메인 모듈(recommendation / playback) 전용 좁은 포트.
 * bff 는 이 포트가 아니라 모듈 루트 API 를 쓴다 (ARCHITECTURE §2.1).
 * P1 은 계약 자리만 잡는다 — 구현은 Phase 3(catalog) 소관.
 */
public interface TitleQueryPort {
    List<Long> findIdsByGenre(String genreCode, int limit);
    List<Long> findTopN(int limit);
    boolean isPlayable(long titleId);
}
