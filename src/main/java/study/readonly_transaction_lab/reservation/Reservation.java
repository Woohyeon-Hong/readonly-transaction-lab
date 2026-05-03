package study.readonly_transaction_lab.reservation;

import java.time.LocalDate;
import study.readonly_transaction_lab.reservationTime.TimeEntity;
import study.readonly_transaction_lab.reservationTime.TimeInfo;

public record Reservation(
        Long id,
        String name,
        LocalDate date,
        TimeInfo timeInfo
) {
    public static Reservation from(
            ReservationEntity reservationEntity,
            TimeEntity timeEntity
    ) {
        return new Reservation(
                reservationEntity.id(),
                reservationEntity.name(),
                reservationEntity.date(),
                TimeInfo.from(timeEntity)
        );
    }
}
