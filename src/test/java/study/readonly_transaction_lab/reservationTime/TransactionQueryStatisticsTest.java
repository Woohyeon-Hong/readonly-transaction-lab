package study.readonly_transaction_lab.reservationTime;

import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import study.readonly_transaction_lab.reservationTime.TransactionQueryStatisticsTest.ServiceConfig;
import study.readonly_transaction_lab.reservationTime.repository.TimesRepository;

@SpringBootTest
@Import(ServiceConfig.class)
public class TransactionQueryStatisticsTest {

    static int SINGLE_MEASUREMENT_COUNT = 1000;

    @TestConfiguration
    static class ServiceConfig {

        @Bean
        ReservationTimeService jdbcReservationTimeService(
                @Qualifier("jdbcTemplateTimesRepo") TimesRepository repo
        ) {
            return new ReservationTimeService(repo);
        }

        @Bean
        ReservationTimeService jpaReservationTimeService(
                @Qualifier("jpaTimesRepo") TimesRepository repo
        ) {
            return new ReservationTimeService(repo);
        }
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    @Qualifier("jdbcReservationTimeService")
    ReservationTimeService jdbcReservationTimeService;

    @Autowired
    @Qualifier("jpaReservationTimeService")
    ReservationTimeService jpaReservationTimeService;

    @DisplayName("보조 실험. readOnly 트랜잭션에서 DB에 전달되는 쿼리 통계를 확인한다.")
    @Test
    void readOnly_트랜잭션_단건_조회_쿼리_통계를_확인한다() {
        prepareTestData();
        startGeneralLog();

        for (int i = 0; i < SINGLE_MEASUREMENT_COUNT; i++) {
            jdbcReservationTimeService.findByIdWithReadOnly(1L);
        }

        stopGeneralLog();

        printQueryStatistics();
    }

    @DisplayName("보조 실험. 트랜잭션 없는 조회에서 DB에 전달되는 쿼리 통계를 확인한다.")
    @Test
    void 트랜잭션_없는_단건_조회_쿼리_통계를_확인한다() {
        prepareTestData();

        startGeneralLog();

        for (int i = 0; i < SINGLE_MEASUREMENT_COUNT; i++) {
            jdbcReservationTimeService.findByIdWithoutTransaction(1L);
        }

        stopGeneralLog();

        printQueryStatistics();
    }

    @DisplayName("보조 실험. JPA를 사용하는 readOnly 트랜잭션에서 DB에 전달되는 쿼리 통계를 확인한다.")
    @Test
    void jpa_readOnly_트랜잭션_단건_조회_쿼리_통계를_확인한다() {
        prepareTestData();

        startGeneralLog();

        for (int i = 0; i < SINGLE_MEASUREMENT_COUNT; i++) {
            jpaReservationTimeService.findByIdWithReadOnly(1L);
        }

        stopGeneralLog();

        printQueryStatistics();
    }

    private void prepareTestData() {
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("""
        INSERT INTO reservation_time (id, start_at)
        VALUES (?, ?)
        ON DUPLICATE KEY UPDATE start_at = VALUES(start_at)
        """, 1L, LocalTime.NOON);
    }

    private void startGeneralLog() {
        jdbcTemplate.execute("SET GLOBAL general_log = 'OFF'");
        jdbcTemplate.execute("SET GLOBAL log_output = 'TABLE'");
        jdbcTemplate.execute("TRUNCATE TABLE mysql.general_log");
        jdbcTemplate.execute("SET GLOBAL general_log = 'ON'");
    }

    private void stopGeneralLog() {
        jdbcTemplate.execute("SET GLOBAL general_log = 'OFF'");
    }

    private void printQueryStatistics() {
        List<QueryStatistic> results = jdbcTemplate.query("""
        SELECT
            argument,
            COUNT(*) AS count
        FROM mysql.general_log
        WHERE command_type = 'Query'
          AND argument NOT LIKE '%mysql.general_log%'
          AND argument NOT LIKE 'SET GLOBAL general_log%'
          AND argument NOT LIKE 'SET GLOBAL log_output%'
          AND argument NOT LIKE 'TRUNCATE TABLE mysql.general_log%'
        GROUP BY argument
        ORDER BY count DESC
        """, (rs, rowNum) -> new QueryStatistic(
                rs.getString("argument"),
                rs.getLong("count")
        ));

        System.out.println("| argument | count |");
        System.out.println("| --- | --- |");
        for (QueryStatistic result : results) {
            System.out.printf("| %s | %d |%n", result.argument(), result.count());
        }
    }

    private record QueryStatistic(String argument, long count) {
    }
}
