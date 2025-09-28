package cs.escaperoomhub.store.repository;

import cs.escaperoomhub.store.entity.Timeslot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Timeslot t WHERE t.timeslotId = :id")
    Optional<Timeslot> findByIdWithPessimisticLock(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Timeslot t set t.isAvailable = false where t.timeslotId = :id and t.isAvailable = true")
    int reserveIfAvailable(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Timeslot t set t.isAvailable = true where t.timeslotId = :id and t.isAvailable = false")
    int cancelIfReserved(@Param("id") Long id);
}
