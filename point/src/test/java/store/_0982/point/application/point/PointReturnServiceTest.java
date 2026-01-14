package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.PointReturnCommand;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointReturnedEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointReturnServiceTest {

    private static final String CANCEL_REASON = "테스트 환불";

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private PointReturnService pointReturnService;

    private UUID memberId;
    private UUID orderId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }


    @Test
    @DisplayName("포인트를 반환한다")
    void returnPoints_success() {
        // given
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(5000);

        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);
        PointAmount usedAmount = PointAmount.of(3000, 0);
        PointTransaction usedHistory = PointTransaction.used(memberId, orderId, UUID.randomUUID(), usedAmount);
        PointAmount returnAmount = PointAmount.of(3000, 0);
        PointTransaction returnHistory = PointTransaction.returned(memberId, orderId, idempotencyKey, returnAmount, CANCEL_REASON);

        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                .thenReturn(Optional.of(usedHistory));
        when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class))).thenReturn(returnHistory);

        // when
        pointReturnService.returnPoints(memberId, command);

        // then
        verify(applicationEventPublisher).publishEvent(any(PointReturnedEvent.class));
    }

    @Test
    @DisplayName("이미 처리된 반환 요청은 멱등성 키로 중복 처리하지 않는다")
    void returnPoints_idempotent() {
        // given
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(5000);

        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);

        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

        // when
        pointReturnService.returnPoints(memberId, command);

        // then
        verify(pointTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("존재하지 않는 회원의 포인트 반환 시 예외가 발생한다")
    void returnPoints_fail_whenMemberNotFound() {
        // given
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, UUID.randomUUID(), "테스트 환불", 3000);

        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointReturnService.returnPoints(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("사용 내역이 없는 주문에 대한 반환 시 예외가 발생한다")
    void returnPoints_fail_whenNoUsageHistory() {
        // given
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(5000);

        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);

        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> pointReturnService.returnPoints(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("반환 금액이 사용 금액보다 큰 경우 예외가 발생한다")
    void returnPoints_fail_whenExcessiveAmount() {
        // given
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(5000);

        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 5000);
        PointAmount usedAmount = PointAmount.of(3000, 0);
        PointTransaction usedHistory = PointTransaction.used(memberId, orderId, UUID.randomUUID(), usedAmount);

        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                .thenReturn(Optional.of(usedHistory));

        // when & then
        assertThatThrownBy(() -> pointReturnService.returnPoints(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.INVALID_REFUND_AMOUNT.getMessage());
    }

    @Test
    @DisplayName("부분 반환이 정상적으로 처리된다")
    void returnPoints_partial_success() {
        // given
        PointBalance pointBalance = new PointBalance(memberId);
        pointBalance.charge(2000);

        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 2000);
        PointAmount usedAmount = PointAmount.of(5000, 0);
        PointTransaction usedHistory = PointTransaction.used(memberId, orderId, UUID.randomUUID(), usedAmount);
        PointAmount returnAmount = PointAmount.of(2000, 0);
        PointTransaction returnHistory = PointTransaction.returned(memberId, orderId, idempotencyKey, returnAmount, CANCEL_REASON);

        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                .thenReturn(Optional.of(usedHistory));
        when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class))).thenReturn(returnHistory);

        // when
        pointReturnService.returnPoints(memberId, command);

        // then
        verify(applicationEventPublisher).publishEvent(any(PointReturnedEvent.class));
        verify(pointTransactionRepository).saveAndFlush(any(PointTransaction.class));
    }
}
