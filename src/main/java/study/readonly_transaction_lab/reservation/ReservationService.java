package study.readonly_transaction_lab.reservation;

import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import study.readonly_transaction_lab.reservation.repository.ReservationsRepository;

public class ReservationService {

    private final ReservationsRepository reservationsRepository;

    public ReservationService(ReservationsRepository reservationsRepository) {
        this.reservationsRepository = reservationsRepository;
    }

    @Transactional(readOnly = true)
    public Reservation findByIdWithReadOnly(Long id) {
        return reservationsRepository.findReservationWithTimeById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
    }

    public Reservation findByIdWithoutTransaction(Long id) {
        return reservationsRepository.findReservationWithTimeById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));
    }

    @Transactional(readOnly = true)
    public List<Reservation> findAllWithReadOnly() {
        return reservationsRepository.findAllReservationsWithTime();
    }

    public List<Reservation> findAllWithoutTransaction() {
        return reservationsRepository.findAllReservationsWithTime();
    }
}
