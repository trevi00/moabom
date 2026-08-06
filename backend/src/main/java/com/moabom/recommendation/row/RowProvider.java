package com.moabom.recommendation.row;

/**
 * ★ OCP 확장점: 새 행 유형 = 이 인터페이스 구현 파일 1개 추가 + 등록 1줄. 기존 파일 수정 0.
 * 구현체(ContinueWatching/TopTen/Genre/BecauseYouWatched/MyList)는 Phase 5~7 소관.
 */
public interface RowProvider {
    String rowKey();
    int order();
}
