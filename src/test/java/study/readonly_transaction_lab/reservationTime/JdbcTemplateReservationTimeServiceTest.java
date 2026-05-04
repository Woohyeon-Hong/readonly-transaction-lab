package study.readonly_transaction_lab.reservationTime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
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
import study.readonly_transaction_lab.reservationTime.JdbcTemplateReservationTimeServiceTest.JdbcConfig;
import study.readonly_transaction_lab.reservationTime.repository.JdbcTemplateTimesRepository;
import study.readonly_transaction_lab.reservationTime.repository.TimesRepository;

@SpringBootTest
@Import(JdbcConfig.class)
class JdbcTemplateReservationTimeServiceTest {

    static int SINGLE_WARM_UP_COUNT = 100;
    static int SINGLE_MEASUREMENT_COUNT = 1000;

    static int MASS_DATA_COUNT = 50000;
    static int MASS_WARM_UP_COUNT = 10;
    static int MASS_MEASUREMENT_COUNT = 200;

    @TestConfiguration
    static class JdbcConfig {
        @Bean
        ReservationTimeService reservationTimeService(
                @Qualifier("jdbcTemplateTimesRepo") TimesRepository repo
        ) {
            return new ReservationTimeService(repo);
        }
    }

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    ReservationTimeService reservationTimeService;

    @Autowired
    JdbcTemplateTimesRepository jdbcTemplateTimesRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");

        jdbcTemplate.update("ALTER TABLE reservation AUTO_INCREMENT = 1");
        jdbcTemplate.update("ALTER TABLE reservation_time AUTO_INCREMENT = 1");
    }

    @DisplayName("시나리오 1-1. JdbcTemplate을 사용해서 예약 시간 정보 단건 조회를 할 때, 두 경우를 비교한다.")
    @Test
    void 예약_시간_단건_조회() {
        TimeEntity savedTime = jdbcTemplateTimesRepository.saveTime(TimeEntity.of(LocalTime.NOON));
        Long id = savedTime.id();

        //실제 측정 전에 워밍업 실행을 수행한다.
        for (int i = 0; i < SINGLE_WARM_UP_COUNT; i++) {
            reservationTimeService.findByIdWithReadOnly(id);
            reservationTimeService.findByIdWithoutTransaction(id);
        }

        List<Long> readOnlyTransactionResults = new ArrayList<>();
        List<Long> noTransactionResults = new ArrayList<>();

        //두 메서드를 교차 실행한다.
        for (int i = 0; i < SINGLE_MEASUREMENT_COUNT; i++) {
            if (i % 2 == 0) {
                readOnlyTransactionResults.add(
                        calculateElapsedTime(() -> reservationTimeService.findByIdWithReadOnly(id), id)
                );
                noTransactionResults.add(calculateElapsedTime(
                        () -> reservationTimeService.findByIdWithoutTransaction(id), id)
                );
            } else {
                noTransactionResults.add(calculateElapsedTime(
                        () -> reservationTimeService.findByIdWithoutTransaction(id), id)
                );
                readOnlyTransactionResults.add(calculateElapsedTime(
                        () -> reservationTimeService.findByIdWithReadOnly(id), id)
                );
            }
        }

        System.out.println("=== readOnly ===");
        printResult(readOnlyTransactionResults);

        System.out.println("=== no transaction ===");
        printResult(noTransactionResults);
    }

    private long calculateElapsedTime(Supplier<TimeInfo> supplier, Long expectedId) {
        long start = System.nanoTime();
        TimeInfo result = supplier.get();
        long end = System.nanoTime();
        assertThat(result.id()).isEqualTo(expectedId);

        return end - start;
    }

    @DisplayName("시나리오 1-2. JdbcTemplate을 사용해서 예약 시간 정보 대량 조회를 할 때, 두 경우를 비교한다.")
    @Test
    void 예약_시간_대량_조회() {
        for (int i = 0; i < MASS_DATA_COUNT; i++) {
            jdbcTemplateTimesRepository.saveTime(TimeEntity.of(LocalTime.NOON));
        }

        //실제 측정 전에 워밍업 실행을 수행한다.
        for (int i = 0; i < MASS_WARM_UP_COUNT; i++) {
            reservationTimeService.findAllWithReadOnly();
            reservationTimeService.findAllWithoutTransaction();
        }

        List<Long> readOnlyTransactionResults = new ArrayList<>();
        List<Long> noTransactionResults = new ArrayList<>();

        //두 메서드를 교차 실행한다.
        for (int i = 0; i < MASS_MEASUREMENT_COUNT; i++) {
            if (i % 2 == 0) {
                readOnlyTransactionResults.add(
                        calculateElapsedTime(() -> reservationTimeService.findAllWithReadOnly(), MASS_DATA_COUNT)
                );
                noTransactionResults.add(calculateElapsedTime(
                        () -> reservationTimeService.findAllWithoutTransaction(), MASS_DATA_COUNT)
                );
            } else {
                noTransactionResults.add(calculateElapsedTime(
                        () -> reservationTimeService.findAllWithoutTransaction(), MASS_DATA_COUNT)
                );
                readOnlyTransactionResults.add(
                        calculateElapsedTime(() -> reservationTimeService.findAllWithReadOnly(), MASS_DATA_COUNT)
                );
            }
        }

        System.out.println("=== readOnly ===");
        printResult(readOnlyTransactionResults);

        System.out.println("=== no transaction ===");
        printResult(noTransactionResults);
    }

    private long calculateElapsedTime(Supplier<List<TimeInfo>> supplier, int expectedSize) {
        long start = System.nanoTime();
        List<TimeInfo> results = supplier.get();
        long end = System.nanoTime();
        assertThat(results).hasSize(expectedSize);

        return end - start;
    }

    private void printResult(List<Long> elapsedTimes) {
        List<Long> sorted = elapsedTimes.stream()
                .sorted()
                .toList();

        long min = sorted.get(0);
        long max = sorted.get(sorted.size() - 1);
        long median = sorted.get(sorted.size() / 2);
        int p95Index = Math.max(0, (int) Math.ceil(sorted.size() * 0.95) - 1);
        long p95 = sorted.get(p95Index);

        double average = elapsedTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);

        System.out.println("avg(ms) = " + average / 1_000_000.0);
        System.out.println("median(ms) = " + median / 1_000_000.0);
        System.out.println("min(ms) = " + min / 1_000_000.0);
        System.out.println("max(ms) = " + max / 1_000_000.0);
        System.out.println("p95(ms) = " + p95 / 1_000_000.0 + "\n");
    }
}
