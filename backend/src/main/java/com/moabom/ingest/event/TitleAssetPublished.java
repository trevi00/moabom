package com.moabom.ingest.event;

import java.time.Instant;

/**
 * 유일한 비동기 경계의 계약. catalog 가 P3+ 에서 @ApplicationModuleListener 로 구독한다.
 * ⚠️ 구독을 붙이는 phase 가 spring-modulith-starter-jpa + event_publication V 마이그레이션을
 *    같은 커밋에 추가해야 한다 (D-14 / RESEARCH CP-4).
 */
public record TitleAssetPublished(long titleId, String masterPlaylistPath, Instant publishedAt) { }
