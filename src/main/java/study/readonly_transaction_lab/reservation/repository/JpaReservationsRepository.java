package study.readonly_transaction_lab.reservation.repository;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import study.readonly_transaction_lab.reservation.Reservation;
import study.readonly_transaction_lab.reservation.ReservationEntity;
import study.readonly_transaction_lab.reservation.ReservationJpaEntity;
import study.readonly_transaction_lab.reservationTime.TimeEntity;

@Repository("jpaReservationsRepo")
public class JpaReservationsRepository implements ReservationsRepository {

    private final SpringDataJpaReservationsRepository reservationsRepository;
    private final EntityManager entityManager;

    public JpaReservationsRepository(
            SpringDataJpaReservationsRepository reservationsRepository,
            EntityManager entityManager
    ) {
        this.reservationsRepository = reservationsRepository;
        this.entityManager = entityManager;
    }

    @Override
    public ReservationEntity saveReservation(ReservationEntity entity) {
        TimeEntity timeEntity = entityManager.getReference(TimeEntity.class, entity.timeId());
        ReservationJpaEntity saved = reservationsRepository.save(ReservationJpaEntity.from(entity, timeEntity));

        return entity.updateId(saved.id());
    }

    @Override
    public Optional<Reservation> findReservationWithTimeById(Long id) {
        return reservationsRepository.findById(id)
                .map(ReservationJpaEntity::toDomain);
    }

    @Override
    public List<Reservation> findAllReservationsWithTime() {
        return reservationsRepository.findAll()
                .stream()
                .map(ReservationJpaEntity::toDomain)
                .toList();
    }
}
