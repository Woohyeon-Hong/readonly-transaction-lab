package study.readonly_transaction_lab.reservationTime;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<TimeInfo> findAllWithReadOnly() {
        return timesRepository.getTimes()
                .stream()
                .map(TimeInfo::from)
                .toList();
    }

    public  List<TimeInfo> findAllWithoutTransaction() {
        return timesRepository.getTimes()
                .stream()
                .map(TimeInfo::from)
                .toList();
    }
}
