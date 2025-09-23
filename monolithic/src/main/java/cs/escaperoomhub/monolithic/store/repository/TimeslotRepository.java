package cs.escaperoomhub.monolithic.store.repository;

import cs.escaperoomhub.monolithic.store.entity.Timeslot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {
}
