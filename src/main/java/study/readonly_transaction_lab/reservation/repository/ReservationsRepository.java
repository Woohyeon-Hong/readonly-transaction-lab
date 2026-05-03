package study.readonly_transaction_lab.reservation.repository;

import java.util.List;
import java.util.Optional;
import study.readonly_transaction_lab.reservation.Reservation;
import study.readonly_transaction_lab.reservation.ReservationEntity;

public interface ReservationsRepository {
    ReservationEntity saveReservation(ReservationEntity entity);
    Optional<Reservation> findReservationWithTimeById(Long id);
    List<Reservation> findAllReservationsWithTime();
}
