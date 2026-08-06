-- V1__init_catalog.sql
-- 스키마의 단일 진실은 Flyway 다. docker-entrypoint-initdb.d 를 쓰지 않는다:
-- MySQL 이미지는 /var/lib/mysql 에 데이터가 있으면 init 을 통째로 건너뛴다 (PITFALLS Pitfall 14).
-- 리셋 = docker compose down -v.
--
-- D-11 범위: title(+장르 최소 관계) 만. 아래는 P1 이 만들지 않는다 —
--   account/profile (P3) · watch_progress (P4) · ingest_job (P8) · event_publication (P3, D-14)
--   에셋 경로/매니페스트 컬럼 (P2 배치 스크립트가 형식을 확정한 뒤 그 phase 가 추가)
--   발행 상태 컬럼 (INGST-12 = P3 소유)

CREATE TABLE genre (
  id       BIGINT       NOT NULL AUTO_INCREMENT,
  code     VARCHAR(40)  NOT NULL,
  name_ko  VARCHAR(80)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_genre_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE title (
  id            BIGINT        NOT NULL AUTO_INCREMENT,
  title_ko      VARCHAR(200)  NOT NULL,
  title_en      VARCHAR(200)  NULL,
  synopsis      TEXT          NULL,
  release_year  SMALLINT      NULL,
  runtime_sec   INT           NULL,
  -- PITFALLS Pitfall 17 / LGL-01: 처음부터 NOT NULL. 나중에 추가하면 모든 시드를 다시 만든다.
  attribution   VARCHAR(300)  NOT NULL,
  license       VARCHAR(60)   NOT NULL,
  created_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id)
  -- 주의: 전문 검색 인덱스를 여기에 만들지 않는다.
  --   SRCH-02 는 "ngram 과 LIKE 를 모두 구현해 측정한 뒤 채택"이 규율이고(PITFALLS Pitfall 10),
  --   인덱스 선점은 그 측정을 무의미하게 만든다. 대상 컬럼(title_ko/title_en/synopsis)만 미리 존재한다.
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE title_genre (
  title_id  BIGINT NOT NULL,
  genre_id  BIGINT NOT NULL,
  PRIMARY KEY (title_id, genre_id),
  KEY idx_title_genre_genre (genre_id),
  CONSTRAINT fk_title_genre_title FOREIGN KEY (title_id) REFERENCES title (id) ON DELETE CASCADE,
  CONSTRAINT fk_title_genre_genre FOREIGN KEY (genre_id) REFERENCES genre (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
