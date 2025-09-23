package cs.escaperoomhub.monolithic.point.service;

import cs.escaperoomhub.monolithic.exception.BusinessException;
import cs.escaperoomhub.monolithic.exception.Errors;
import cs.escaperoomhub.monolithic.point.entity.Point;
import cs.escaperoomhub.monolithic.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService{

    private final PointRepository pointRepository;

    @Transactional
    public void use(Long userId, Long amount) {
        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> Errors.notFound("포인트가 존재하지 않습니다."));

//         오류 상황 가정
//        if (true) {
//            throw new RuntimeException();
//        }

        point.use(amount);
        pointRepository.save(point);
    }

}
