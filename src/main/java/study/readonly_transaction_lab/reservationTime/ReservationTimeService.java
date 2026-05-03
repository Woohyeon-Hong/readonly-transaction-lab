package study.readonly_transaction_lab.reservationTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import study.readonly_transaction_lab.reservationTime.repository.JdbcTemplateTimesRepository;

@Service
public class ReservationTimeService {

    private final JdbcTemplateTimesRepository timesRepository;

    public ReservationTimeService(JdbcTemplateTimesRepository timesRepository) {
        this.timesRepository = timesRepository;
    }

    @Transactional(readOnly = true)
    public TimeInfo findByIdWithReadOnly(Long id) {
        TimeEntity time = timesRepository.getTimeEntityById(id);
        return TimeInfo.from(time);
    }

    public TimeInfo findByIdWithoutTransaction(Long id) {
        TimeEntity time = timesRepository.getTimeEntityById(id);
        return TimeInfo.from(time);
    }
}
