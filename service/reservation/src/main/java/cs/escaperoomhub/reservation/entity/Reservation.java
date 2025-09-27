package cs.escaperoomhub.reservation.entity;

import cs.escaperoomhub.reservation.exception.ReservationErrors;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "reservation")
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

    public void request() {
        if (status != ReservationStatus.CREATED) {
            throw ReservationErrors.invalidReservationStatus(ReservationStatus.CREATED, status);
        }

        status = ReservationStatus.REQUESTED;
    }

    public void complete() {
        this.status = ReservationStatus.COMPLETED;
    }

    public void fail() {
        if (status != ReservationStatus.CREATED) {
            throw ReservationErrors.invalidReservationStatus(ReservationStatus.CREATED, status);
        }

        status = ReservationStatus.FAILED;
    }

    public enum ReservationStatus {
        CREATED, REQUESTED, COMPLETED, FAILED
    }
}