package cs.escaperoomhub.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "compensation_registry")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompensationRegistry {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false)
    private Long reservationId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompensationRegistryStatus status;

    public CompensationRegistry(Long reservationId) {
        this.reservationId = reservationId;
        this.status = CompensationRegistryStatus.PENDING;
    }

    public enum CompensationRegistryStatus {
        PENDING, COMPLETE
    }
}
