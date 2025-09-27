package cs.escaperoomhub.reservation.repository;

import cs.escaperoomhub.reservation.entity.CompensationRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompensationRegistryRepository extends JpaRepository<CompensationRegistry, Long> {
    Optional<CompensationRegistry> findByReservationId(Long reservationId);
}
