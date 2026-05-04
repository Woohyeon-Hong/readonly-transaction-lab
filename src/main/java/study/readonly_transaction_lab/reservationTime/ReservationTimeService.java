package study.readonly_transaction_lab.reservationTime;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import study.readonly_transaction_lab.reservationTime.repository.TimesRepository;

public class ReservationTimeService {

    private final TimesRepository timesRepository;


    public ReservationTimeService(TimesRepository timesRepository) {
        this.timesRepository = timesRepository;
    }

    @Transactional(readOnly = true)
    public TimeInfo findByIdWithReadOnly(Long id) {
        TimeEntity time = timesRepository.findTimeEntityById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 시간이 존재하지 않습니다."));
        return TimeInfo.from(time);
    }

    public TimeInfo findByIdWithoutTransaction(Long id) {
        TimeEntity time = timesRepository.findTimeEntityById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약 시간이 존재하지 않습니다."));
        return TimeInfo.from(time);
    }

    @Transactional(readOnly = true)
    public List<TimeInfo> findAllWithReadOnly() {
        return timesRepository.findAllTimes()
                .stream()
                .map(TimeInfo::from)
                .toList();
    }

    public  List<TimeInfo> findAllWithoutTransaction() {
        return timesRepository.findAllTimes()
                .stream()
                .map(TimeInfo::from)
                .toList();
    }
}
