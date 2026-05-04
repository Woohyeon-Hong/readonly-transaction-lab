package study.readonly_transaction_lab.reservationTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;

@Entity
@Table(name = "reservation_time")
public class TimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_at", nullable = false)
    private LocalTime startAt;

    protected TimeEntity() {
    }

    public TimeEntity(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static TimeEntity of(LocalTime startAt) {
        return new TimeEntity(null, startAt);
    }

    public TimeEntity updateId(long id) {
        return new TimeEntity(id, this.startAt);
    }

    public Long id() {
        return id;
    }

    public LocalTime startAt() {
        return startAt;
    }
}
