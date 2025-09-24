package cs.escaperoomhub.monolithic.store.service;

import cs.escaperoomhub.monolithic.exception.CommonErrors;
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
                .orElseThrow(() -> CommonErrors.notFound("타임슬롯이 존재하지 않습니다."));

        Long totalPrice = timeslot.calculatePrice(personCount);
        timeslot.reserve();

        timeslotRepository.save(timeslot);
        return totalPrice;
    }

    @Transactional
    public Long reserveWithPessimisticLock(Long timeslotId, Long personCount) {
        Timeslot timeslot = timeslotRepository.findByIdWithPessimisticLock(timeslotId)
                .orElseThrow(() -> CommonErrors.notFound("타임슬롯이 존재하지 않습니다."));

        Long totalPrice = timeslot.calculatePrice(personCount); // 락 획득 후 계산
        timeslot.reserve();

        return totalPrice;
    }
}
