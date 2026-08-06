package com.moabom;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * SC3 + G3 의 게이트.
 * 컨테이너 기동 파라미터를 compose.yaml 과 동일하게 맞추고(RESEARCH CP-10), 그 값이 실제로 먹었는지를
 * 테스트가 스스로 단언한다. 어긋나면 P1 에서는 조용하다가 P7 검색 phase 에서
 * "테스트는 통과하는데 실 DB 에서 안 나온다" 로 터진다.
 */
@SpringBootTest
@Testcontainers
class SchemaMigrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
        .withCommand("--ngram_token_size=2",
                     "--character-set-server=utf8mb4",
                     "--collation-server=utf8mb4_0900_ai_ci");

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void flywayAppliedBothMigrations() {
        Integer applied = jdbc.queryForObject(
            "SELECT count(*) FROM flyway_schema_history WHERE success = 1 AND version IN ('1','2')",
            Integer.class);
        assertThat(applied).isEqualTo(2);
    }

    @Test
    void attributionAndLicenseAreNotNull() {
        assertThat(nullability("attribution")).isEqualTo("NO");
        assertThat(nullability("license")).isEqualTo("NO");
    }

    @Test
    void koreanTitlesRoundTrip() {
        Integer korean = jdbc.queryForObject(
            "SELECT count(*) FROM title WHERE title_ko REGEXP '[가-힣]'", Integer.class);
        assertThat(korean).isGreaterThanOrEqualTo(5);

        Integer exact = jdbc.queryForObject(
            "SELECT count(*) FROM title WHERE title_ko = ?", Integer.class, "인터스텔라");
        assertThat(exact).isEqualTo(1);
    }

    @Test
    void ccByRowsHaveAttribution() {
        Integer ccby = jdbc.queryForObject(
            "SELECT count(*) FROM title WHERE license LIKE 'CC-BY%' AND attribution <> ''",
            Integer.class);
        assertThat(ccby).isGreaterThanOrEqualTo(1);
    }

    @Test
    void mysqlStartupParametersActuallyApplied() {
        Integer tokenSize = jdbc.queryForObject("SELECT @@ngram_token_size", Integer.class);
        assertThat(tokenSize)
            .as("ngram_token_size 는 read-only 라 기동 인자로만 설정된다 (PITFALLS Pitfall 10)")
            .isEqualTo(2);

        String collation = jdbc.queryForObject("SELECT @@collation_server", String.class);
        assertThat(collation).isEqualTo("utf8mb4_0900_ai_ci");
    }

    @Test
    void noFulltextIndexYet() {
        Integer fulltext = jdbc.queryForObject(
            "SELECT count(*) FROM information_schema.statistics "
                + "WHERE table_schema = DATABASE() AND index_type = 'FULLTEXT'",
            Integer.class);
        assertThat(fulltext)
            .as("SRCH-02 는 ngram vs LIKE 실측 후 채택이다 — P1 이 인덱스를 선점하면 그 측정이 무의미해진다")
            .isZero();
    }

    private String nullability(String column) {
        return jdbc.queryForObject(
            "SELECT is_nullable FROM information_schema.columns "
                + "WHERE table_schema = DATABASE() AND table_name = 'title' AND column_name = ?",
            String.class, column);
    }
}
