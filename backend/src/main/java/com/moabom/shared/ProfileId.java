package com.moabom.shared;

public record ProfileId(long value) {
    public ProfileId {
        if (value <= 0) throw new IllegalArgumentException("profileId must be positive: " + value);
    }
}
