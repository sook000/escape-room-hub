package cs.escaperoomhub.monolithic.store.entity;

import cs.escaperoomhub.monolithic.exception.Errors;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "timeslot")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Timeslot {

    @Id
    private Long timeslotId;

    @Column(nullable = false)
    private Long storeId;

    @Column(nullable = false)
    private Long themeId;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime openAt;

    @Column(nullable = false)
    private Long pricePerPerson;

    @Column(nullable = false)
    private boolean isAvailable;

    public Timeslot(Long timeslotId,
                    Long storeId,
                    Long themeId,
                    LocalDateTime startAt,
                    LocalDateTime openAt,
                    Long pricePerPerson) {
        this(timeslotId, storeId, themeId, startAt, openAt, pricePerPerson, true);
    }

    public Timeslot(Long timeslotId,
                    Long storeId,
                    Long themeId,
                    LocalDateTime startAt,
                    LocalDateTime openAt,
                    Long pricePerPerson,
                    boolean isAvailable) {
        this.timeslotId = timeslotId;
        this.storeId = storeId;
        this.themeId = themeId;
        this.startAt = startAt;
        this.openAt = openAt;
        this.pricePerPerson = pricePerPerson;
        this.isAvailable = isAvailable;
    }

    public Long calculatePrice(Long personCount) {
        return pricePerPerson * personCount;
    }

    public void reserve() {
        if (LocalDateTime.now().isBefore(this.openAt)) {
            throw Errors.timeslotNotOpenYet();
        }

        if (!this.isAvailable) {
            throw Errors.timeslotAlreadyReserved();
        }

        isAvailable = false;
    }
}
