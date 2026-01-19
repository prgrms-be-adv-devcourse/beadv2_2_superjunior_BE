package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.point.PointReturnCommand;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointReturnServiceTest {

    private static final String CANCEL_REASON = "테스트 환불";

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointTxManager pointTxManager;

    private PointReturnService pointReturnService;

    private UUID memberId;
    private UUID orderId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        pointReturnService = new PointReturnService(pointTxManager);

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Test
    @DisplayName("포인트를 반환한다")
    void returnPoints_success() {
        // given
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);
        PointBalance pointBalance = new PointBalance(memberId);
        
        PointTransaction usedTx = PointTransaction.used(memberId, orderId, UUID.randomUUID(), PointAmount.of(3000, 0));
        
        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
        when(pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                .thenReturn(Optional.of(usedTx));
        when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        pointReturnService.returnPoints(memberId, command);

        // then
        verify(pointTransactionRepository).existsByIdempotencyKey(idempotencyKey);
        verify(pointBalanceRepository).findByMemberId(memberId);
        verify(pointTransactionRepository).findByOrderIdAndStatus(orderId, PointTransactionStatus.USED);
        verify(pointTransactionRepository).saveAndFlush(any(PointTransaction.class));
    }

    @Test
    @DisplayName("존재하지 않는 회원의 포인트 반환 시 예외가 발생한다")
    void returnPoints_fail_whenMemberNotFound() {
        // given
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);

        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
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
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);
        PointBalance pointBalance = new PointBalance(memberId);

        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
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
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 5000);
        PointBalance pointBalance = new PointBalance(memberId);
        PointTransaction usedTx = PointTransaction.used(memberId, orderId, UUID.randomUUID(), PointAmount.of(3000, 0));

        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);
        when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
        when(pointTransactionRepository.findByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                .thenReturn(Optional.of(usedTx));

        // when & then
        assertThatThrownBy(() -> pointReturnService.returnPoints(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.INVALID_REFUND_AMOUNT.getMessage());
    }

    @Test
    @DisplayName("중복 반환 요청 시 예외가 발생한다")
    void returnPoints_fail_whenDuplicateRequest() {
        // given
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);

        when(pointTransactionRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> pointReturnService.returnPoints(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.IDEMPOTENT_REQUEST.getMessage());
    }
}
