package study.readonly_transaction_lab.reservationTime.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import study.readonly_transaction_lab.reservationTime.TimeEntity;

@Repository("jpaTimesRepo")
public class JpaTimesRepository implements TimesRepository {

    private final SpringDataJpaTimesRepository jpaRepository;

    public JpaTimesRepository(SpringDataJpaTimesRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public TimeEntity saveTime(TimeEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<TimeEntity> findTimeEntityById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<TimeEntity> findAllTimes() {
        return jpaRepository.findAll();
    }
}
