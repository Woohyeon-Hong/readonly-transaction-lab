package study.readonly_transaction_lab.reservationTime.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import study.readonly_transaction_lab.reservationTime.TimeEntity;

public interface SpringDataJpaTimesRepository extends JpaRepository<TimeEntity, Long> {
}
