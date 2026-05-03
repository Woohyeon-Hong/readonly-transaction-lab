package study.readonly_transaction_lab.reservationTime;

import java.time.LocalTime;

public record TimeInfo(
        Long id,
        LocalTime startAt
) {
    public static TimeInfo from(TimeEntity entity) {
        return new TimeInfo(
                entity.id(),
                entity.startAt()
        );
    }
}
