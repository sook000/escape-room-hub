package cs.escaperoomhub.point.repository;

import cs.escaperoomhub.point.entity.PointTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointTransactionHistoryRepository extends JpaRepository<PointTransactionHistory, Long> {
    PointTransactionHistory findByRequestIdAndTransactionType(
            String requestId, PointTransactionHistory.TransactionType transactionType);
}
