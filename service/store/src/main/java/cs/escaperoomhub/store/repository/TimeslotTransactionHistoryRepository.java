package cs.escaperoomhub.store.repository;

import cs.escaperoomhub.store.entity.TimeslotTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimeslotTransactionHistoryRepository extends JpaRepository<TimeslotTransactionHistory, Long> {
    TimeslotTransactionHistory findByRequestIdAndTransactionType(String requestId,
                                               TimeslotTransactionHistory.TransactionType transactionType);
}
