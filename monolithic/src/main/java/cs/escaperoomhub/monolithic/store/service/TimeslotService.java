package cs.escaperoomhub.monolithic.store.service;

import cs.escaperoomhub.monolithic.exception.Errors;
import cs.escaperoomhub.monolithic.store.entity.Timeslot;
import cs.escaperoomhub.monolithic.store.repository.TimeslotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TimeslotService {
    private final TimeslotRepository timeslotRepository;

    @Transactional
    public Long reserve(Long timeslotId, Long personCount) {

        Timeslot timeslot = timeslotRepository.findById(timeslotId)
                .orElseThrow(() -> Errors.notFound("타임슬롯이 존재하지 않습니다."));

        Long totalPrice = timeslot.calculatePrice(personCount);
        timeslot.reserve();

        timeslotRepository.save(timeslot);
        return totalPrice;
    }
}
