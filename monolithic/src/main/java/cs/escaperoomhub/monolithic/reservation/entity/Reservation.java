package cs.escaperoomhub.monolithic.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "Reservation")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
    @Id
    private Long reservationId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long timeslotId;

    @Column(nullable = false)
    private Long personCount;

    public Reservation(Long reservationId, Long userId, Long timeslotId, Long personCount) {
        this.reservationId = reservationId;
        this.status = ReservationStatus.CREATED;
        this.userId = userId;
        this.timeslotId = timeslotId;
        this.personCount = personCount;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public enum ReservationStatus {
        CREATED, COMPLETED
    }
}