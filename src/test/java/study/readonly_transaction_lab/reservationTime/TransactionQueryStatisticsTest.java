package study.readonly_transaction_lab.reservationTime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import study.readonly_transaction_lab.reservationTime.TransactionQueryStatisticsTest.ServiceConfig;
import study.readonly_transaction_lab.reservationTime.repository.TimesRepository;

@SpringBootTest
@Import(ServiceConfig.class)
public class TransactionQueryStatisticsTest {

    static int SINGLE_MEASUREMENT_COUNT = 1000;

    static final TxState SERVICE_READ_ONLY_TX = new TxState(true, true);
    static final TxState NO_SERVICE_TX = new TxState(false, false);

    @TestConfiguration
    static class ServiceConfig {

        @Bean
        TxRecordingTimesRepository jdbcRecordingRepo(@Qualifier("jdbcTemplateTimesRepo") TimesRepository repo) {
            return new TxRecordingTimesRepository(repo);
        }

        @Bean
        TxRecordingTimesRepository jpaRecordingRepo(@Qualifier("jpaTimesRepo") TimesRepository repo) {
            return new TxRecordingTimesRepository(repo);
        }

        @Bean
        ReservationTimeService jdbcReservationTimeService(
                @Qualifier("jdbcRecordingRepo") TxRecordingTimesRepository repo
        ) {
            return new ReservationTimeService(repo);
        }

        @Bean
        ReservationTimeService jpaReservationTimeService(
                @Qualifier("jpaRecordingRepo") TxRecordingTimesRepository repo
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

    @Autowired
    @Qualifier("jdbcRecordingRepo")
    TxRecordingTimesRepository jdbcRecordingRepo;

    @Autowired
    @Qualifier("jpaRecordingRepo")
    TxRecordingTimesRepository jpaRecordingRepo;

    @BeforeEach
    void setUp() {
        jdbcRecordingRepo.states.clear();
        jpaRecordingRepo.states.clear();
    }

    @DisplayName("보조 실험. readOnly 트랜잭션에서 DB에 전달되는 쿼리 통계를 확인한다.")
    @Test
    void readOnly_트랜잭션_단건_조회_쿼리_통계를_확인한다() {
        prepareTestData();
        startGeneralLog();

        for (int i = 0; i < SINGLE_MEASUREMENT_COUNT; i++) {
            jdbcReservationTimeService.findByIdWithReadOnly(1L);
        }

        stopGeneralLog();

        assertThat(jdbcRecordingRepo.states)
                .hasSize(SINGLE_MEASUREMENT_COUNT)
                .containsOnly(SERVICE_READ_ONLY_TX);

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

        assertThat(jdbcRecordingRepo.states)
                .hasSize(SINGLE_MEASUREMENT_COUNT)
                .containsOnly(NO_SERVICE_TX);

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

        assertThat(jpaRecordingRepo.states)
                .hasSize(SINGLE_MEASUREMENT_COUNT)
                .containsOnly(SERVICE_READ_ONLY_TX);

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

    record TxState(boolean active, boolean readOnly) {
    }

    static class TxRecordingTimesRepository implements TimesRepository {

        final List<TxState> states = new ArrayList<>();
        private final TimesRepository delegate;

        TxRecordingTimesRepository(TimesRepository delegate) {
            this.delegate = delegate;
        }

        @Override
        public TimeEntity saveTime(TimeEntity entity) {
            return delegate.saveTime(entity);
        }

        @Override
        public Optional<TimeEntity> findTimeEntityById(Long id) {
            states.add(new TxState(
                    TransactionSynchronizationManager.isActualTransactionActive(),
                    TransactionSynchronizationManager.isCurrentTransactionReadOnly()
            ));

            return delegate.findTimeEntityById(id);
        }

        @Override
        public List<TimeEntity> findAllTimes() {
            return delegate.findAllTimes();
        }
    }
}
