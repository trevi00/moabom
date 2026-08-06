package com.moabom.playback.spi;

import java.util.List;

/** recommendation 전용. 구현은 Phase 4(playback) 소관. */
public interface WatchHistoryPort {
    List<Long> recentWatchedTitleIds(long profileId, int limit);
    List<Long> inProgressTitleIds(long profileId, int limit);
}
