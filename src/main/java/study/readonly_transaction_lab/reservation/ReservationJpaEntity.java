package study.readonly_transaction_lab.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import study.readonly_transaction_lab.reservationTime.TimeEntity;
import study.readonly_transaction_lab.reservationTime.TimeInfo;

@Entity
@Table(name = "reservation")
public class ReservationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_id", nullable = false)
    private TimeEntity time;

    protected ReservationJpaEntity() {
    }

    public ReservationJpaEntity(Long id, String name, LocalDate date, TimeEntity time) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.time = time;
    }

    public static ReservationJpaEntity from(ReservationEntity reservationEntity, TimeEntity timeEntity) {
        return new ReservationJpaEntity(
                reservationEntity.id(),
                reservationEntity.name(),
                reservationEntity.date(),
                timeEntity
        );
    }

    public Reservation toDomain() {
        return new Reservation(
                id,
                name,
                date,
                new TimeInfo(time.id(), time.startAt())
        );
    }

    public Long id() {
        return id;
    }
}
