package com.moabom.profile.spi;

import java.util.List;

/** playback / recommendation 전용. 구현은 Phase 3(profile) 소관. */
public interface ProfilePort {
    boolean isKids(long profileId);
    List<Long> myListTitleIds(long profileId);
}
