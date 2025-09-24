package cs.escaperoomhub.point.entity;

import cs.escaperoomhub.point.exception.PointErrors;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "point")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Point {
    @Id
    private Long pointId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long amount;

    @Version
    private Long version;

    public Point(Long id, Long userId, Long amount) {
        this.pointId = id;
        this.userId = userId;
        this.amount = amount;
    }

    public void use(Long amount) {
        if (this.amount < amount) {
            throw PointErrors.insufficientBalance(amount, this.amount);
        }
        this.amount = this.amount - amount;
    }

    public void cancel(Long amount) {
        this.amount = this.amount + amount;
    }
}
