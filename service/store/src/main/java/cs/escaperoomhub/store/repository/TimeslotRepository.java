package cs.escaperoomhub.store.repository;

import cs.escaperoomhub.store.entity.Timeslot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Timeslot t WHERE t.timeslotId = :id")
    Optional<Timeslot> findByIdWithPessimisticLock(@Param("id") Long id);
}
