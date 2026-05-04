package study.readonly_transaction_lab.reservation.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import study.readonly_transaction_lab.reservation.ReservationJpaEntity;

public interface SpringDataJpaReservationsRepository extends JpaRepository<ReservationJpaEntity, Long> {

    @Override
    @EntityGraph(attributePaths = "time")
    Optional<ReservationJpaEntity> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "time")
    List<ReservationJpaEntity> findAll();
}
