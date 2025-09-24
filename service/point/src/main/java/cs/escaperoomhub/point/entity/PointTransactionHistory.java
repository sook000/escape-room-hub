package cs.escaperoomhub.point.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "point_transaction_history")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransactionHistory {
    @Id
    private Long id;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private Long pointId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    public PointTransactionHistory(Long id, String requestId, Long pointId,
                                   Long amount, TransactionType transactionType) {
        this.id = id;
        this.requestId = requestId;
        this.pointId = pointId;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public enum TransactionType {
        USE, CANCEL
    }

}
