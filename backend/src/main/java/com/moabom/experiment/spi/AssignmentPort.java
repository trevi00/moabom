package com.moabom.experiment.spi;

/** recommendation 전용. 배정은 결정론(읽을 때마다 랜덤 금지 — ARCHITECTURE AP5). 구현은 v2. */
public interface AssignmentPort {
    String cellFor(long profileId, String testKey);
}
