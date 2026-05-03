package study.readonly_transaction_lab.reservationTime.repository;

import java.util.List;
import java.util.Optional;
import study.readonly_transaction_lab.reservationTime.TimeEntity;

public interface TimesRepository {
    TimeEntity saveTime(TimeEntity entity);
    Optional<TimeEntity> findTimeEntityById(Long id);
    List<TimeEntity> findAllTimes();
}
