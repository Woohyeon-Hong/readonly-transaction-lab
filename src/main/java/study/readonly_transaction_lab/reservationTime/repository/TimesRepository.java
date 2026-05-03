package study.readonly_transaction_lab.reservationTime.repository;

import java.util.List;
import study.readonly_transaction_lab.reservationTime.TimeEntity;

public interface TimesRepository {
    TimeEntity saveTime(TimeEntity entity);

    List<TimeEntity> getTimes();

    void deleteTimeById(Long id);

    TimeEntity getTimeEntityById(Long id);
}
