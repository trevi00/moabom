package com.moabom.shared;

/** 모듈 간 참조는 엔티티가 아니라 타입드 ID 로 한다 (ARCHITECTURE AP: JPA 양방향 연관 → 사이클 회피). */
public record TitleId(long value) {
    public TitleId {
        if (value <= 0) throw new IllegalArgumentException("titleId must be positive: " + value);
    }
}
