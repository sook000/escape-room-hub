package cs.escaperoomhub.store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Table(name = "timeslot_transaction_history")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeslotTransactionHistory {

    @Id
    private Long timeslotHistoryId;

    @Column(nullable = false)
    private String requestId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long timeslotId;

    @Column(nullable = false)
    private Long personCount;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    public TimeslotTransactionHistory(Long timeslotHistoryId, String requestId, Long userId,
                                      Long timeslotId, Long personCount, Long price, TransactionType transactionType) {
        this.timeslotHistoryId = timeslotHistoryId;
        this.requestId = requestId;
        this.userId = userId;
        this.timeslotId = timeslotId;
        this.personCount = personCount;
        this.price = price;
        this.transactionType = transactionType;
    }

    public enum TransactionType {
        BOOKING, CANCEL
    }
}
