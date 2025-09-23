package cs.escaperoomhub.monolithic.reservation.repository;

import cs.escaperoomhub.monolithic.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
