package cs.escaperoomhub.reservation.repository;

import cs.escaperoomhub.reservation.entity.CompensationRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompensationRegistryRepository extends JpaRepository<CompensationRegistry, Long> {
}
