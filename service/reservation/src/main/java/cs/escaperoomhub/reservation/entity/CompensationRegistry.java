package cs.escaperoomhub.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Generated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Table(name = "compensation_registry")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompensationRegistry {
    @Id
    public Long id;

    @Column(nullable = false, unique = true)
    private Long reservationId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CompensationRegistryStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime completedAt;

    @Column
    private String errorMessage;

    public CompensationRegistry(Long id, Long reservationId) {
        this.id = id;
        this.reservationId = reservationId;
        this.status = CompensationRegistryStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = CompensationRegistryStatus.COMPLETE;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = CompensationRegistryStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public enum CompensationRegistryStatus {
        PENDING, COMPLETE, FAILED
    }
}
