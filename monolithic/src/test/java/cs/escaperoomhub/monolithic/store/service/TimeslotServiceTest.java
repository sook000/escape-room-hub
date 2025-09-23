package cs.escaperoomhub.monolithic.store.service;

import cs.escaperoomhub.monolithic.exception.ClientErrorException;
import cs.escaperoomhub.monolithic.store.entity.Timeslot;
import cs.escaperoomhub.monolithic.store.repository.TimeslotRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeslotServiceTest {
    @Mock
    TimeslotRepository timeslotRepository;

    @InjectMocks
    TimeslotService timeslotService;

    private Timeslot makeSlot(Long id, boolean available, long pricePerPerson) {
        LocalDateTime past = LocalDateTime.now().minusHours(1);
        return new Timeslot(
                id,1L, 2L, past, past, pricePerPerson, available);
    }

    private Timeslot makeSlotWithOpenAt(Long id, boolean available, long pricePerPerson, LocalDateTime openAt) {
        return new Timeslot(id, 1L, 2L, openAt, openAt, pricePerPerson, available);
    }

    @Test
    @DisplayName("정상 예약되어 Timeslot의 is_available이 false로 바뀜")
    void reserveSuccess() {
        // given
        Long timeslotId = 1L;
        Long personCount = 3L;
        Timeslot slot = makeSlot(timeslotId, true, 10_000L); // 예약 가능 상태, 1인당 만원

        given(timeslotRepository.findById(timeslotId)).willReturn(Optional.of(slot));

        // when
        Long price = timeslotService.reserve(timeslotId, personCount);

        // then
        assertThat(price).isEqualTo(30_000L);
        assertThat(slot.isAvailable()).isFalse();
        verify(timeslotRepository).save(slot);
    }

    @Test
    @DisplayName("타임슬롯 미존재 시 예외")
    void reserveNotFound() {
        // given
        Long timeslotId = 5L;
        given(timeslotRepository.findById(timeslotId)).willReturn(Optional.empty());

        // when // then
        assertThatThrownBy(() -> timeslotService.reserve(timeslotId, 1L))
                .isInstanceOf(ClientErrorException.class)
                .hasMessageContaining("타임슬롯이 존재하지 않습니다.");

        verify(timeslotRepository).findById(timeslotId);
        verify(timeslotRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 예약된 타임슬롯이면 예외 전파 및 save 호출 없음")
    void reserveAlreadyReserved() {
        // given
        Long timeslotId = 10L;
        Long personCount = 2L;
        Timeslot slot = makeSlot(timeslotId, false, 15_000L); // 이미 예약됨

        given(timeslotRepository.findById(timeslotId)).willReturn(Optional.of(slot));

        // when // then
        assertThatThrownBy(() -> timeslotService.reserve(timeslotId, personCount))
                .isInstanceOf(ClientErrorException.class);

        verify(timeslotRepository).findById(timeslotId);
        verify(timeslotRepository, never()).save(any());
    }

    @Test
    @DisplayName("오픈 시간 이전이면 예약 불가하고 save 호출 없음")
    void reserveFailsBeforeOpenAt() {
        // given
        Long timeslotId = 100L;
        Long personCount = 2L;
        LocalDateTime future = LocalDateTime.now().plusMinutes(5);
        Timeslot slot = makeSlotWithOpenAt(timeslotId, true, 10_000L, future);

        given(timeslotRepository.findById(timeslotId)).willReturn(Optional.of(slot));

        // when // then
        assertThatThrownBy(() -> timeslotService.reserve(timeslotId, personCount))
                .isInstanceOf(ClientErrorException.class)
                .hasMessageContaining("오픈");

        verify(timeslotRepository).findById(timeslotId);
        verify(timeslotRepository, never()).save(any());
    }


    @Test
    @DisplayName("reserve 호출하면 isAvailable false로 변경, isAvailable이 false면 reserve 불가")
    void timeSlotChangeStatus() {
        Timeslot available = makeSlot(1L, true, 1000L);
        available.reserve();
        assertThat(available.isAvailable()).isFalse();

        Timeslot reserved = makeSlot(2L, false, 1000L);
        assertThatThrownBy(reserved::reserve)
                .isInstanceOf(ClientErrorException.class);
    }
}