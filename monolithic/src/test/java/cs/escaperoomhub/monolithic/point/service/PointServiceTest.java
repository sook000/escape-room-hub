package cs.escaperoomhub.monolithic.point.service;

import cs.escaperoomhub.monolithic.exception.ClientErrorException;
import cs.escaperoomhub.monolithic.point.entity.Point;
import cs.escaperoomhub.monolithic.point.repository.PointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    PointRepository pointRepository;

    @InjectMocks
    PointService pointService;

    private Point makePoint(Long pointId, Long userId, Long amount) {
        return new Point(pointId, userId, amount);
    }

    @Test
    @DisplayName("정상 사용 시 금액이 차감되고 save 호출")
    void useSuccess() {
        // given
        Long userId = 100L;
        Point point = makePoint(1L, userId, 10_000L);
        given(pointRepository.findByUserId(userId)).willReturn(Optional.of(point));

        // when
        pointService.use(userId, 3_000L);

        // then
        assertThat(point.getAmount()).isEqualTo(7_000L);
        ArgumentCaptor<Point> captor = ArgumentCaptor.forClass(Point.class);
        verify(pointRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(7000L);    }

    @Test
    @DisplayName("포인트 미존재 시 예외")
    void useNotFound() {
        // given
        Long userId = 101L;
        given(pointRepository.findByUserId(userId)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> pointService.use(userId, 1_000L))
                .isInstanceOf(ClientErrorException.class)
                .hasMessageContaining("포인트가 존재하지 않습니다.");

        verify(pointRepository).findByUserId(userId);
        verify(pointRepository, never()).save(any());
    }

    @Test
    @DisplayName("use: 잔액 부족 시 예외 및 save 호출 없음")
    void useInsufficientBalance() {
        // given
        Long userId = 102L;
        Point point = makePoint(2L, userId, 2_000L);
        given(pointRepository.findByUserId(userId)).willReturn(Optional.of(point));

        // when // then
        assertThatThrownBy(() -> pointService.use(userId, 3_000L))
                .isInstanceOf(ClientErrorException.class);

        verify(pointRepository).findByUserId(userId);
        verify(pointRepository, never()).save(any());
    }

    @Test
    @DisplayName("Point 금액 차감 성공/실패(경계값 포함) 동작 검증")
    void DomainCheck() {
        // 충분한 잔액 -> 차감 후 금액 확인
        Point a = makePoint(10L, 1L, 5_000L);
        a.use(1_500L);
        assertThat(a.getAmount()).isEqualTo(3_500L);

        // 정확히 동일 금액 -> 0원
        Point b = makePoint(11L, 1L, 3_000L);
        b.use(3_000L);
        assertThat(b.getAmount()).isEqualTo(0L);

        // 부족 -> 예외
        Point c = makePoint(12L, 1L, 1_000L);
        assertThatThrownBy(() -> c.use(1_001L))
                .isInstanceOf(ClientErrorException.class);
    }
}