package cs.escaperoomhub.point.entity;

import cs.escaperoomhub.point.exception.PointErrors;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
}
