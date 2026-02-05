package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import store._0982.common.exception.CustomException;
import store._0982.point.application.OrderQueryService;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointDeductCommand;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.event.PointDeductedTxEvent;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointDeductServiceTest {

    private static final String SAMPLE_PURCHASE_NAME = "테스트 공구";

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private OrderQueryService orderQueryService;

    @InjectMocks
    private PointDeductService pointDeductService;

    private PointDeductFacade pointDeductFacade;

    private UUID memberId;
    private UUID orderId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        pointDeductFacade = new PointDeductFacade(pointDeductService, orderQueryService);

        memberId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Nested
    @DisplayName("포인트 차감")
    class DeductPoints {

        @Test
        @DisplayName("포인트를 차감한다")
        void deduct_success() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000, SAMPLE_PURCHASE_NAME);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);
            
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                    .thenReturn(false);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            doNothing().when(orderQueryService).validateOrderPayable(memberId, orderId, 5000);
            doNothing().when(applicationEventPublisher).publishEvent(any(PointDeductedTxEvent.class));

            // when
            PointBalanceInfo result = pointDeductFacade.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(5000);
            
            verify(orderQueryService).validateOrderPayable(memberId, orderId, 5000);
            verify(pointBalanceRepository).findByMemberId(memberId);
            verify(pointTransactionRepository).saveAndFlush(any(PointTransaction.class));
            verify(applicationEventPublisher).publishEvent(any(PointDeductedTxEvent.class));
        }

        @Test
        @DisplayName("주문 검증 실패 시 차감이 실패한다")
        void deduct_fail_whenOrderValidationFails() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000, SAMPLE_PURCHASE_NAME);

            doThrow(new CustomException(CustomErrorCode.INVALID_PAYMENT_REQUEST))
                    .when(orderQueryService).validateOrderPayable(memberId, orderId, 5000);

            // when & then
            assertThatThrownBy(() -> pointDeductFacade.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_REQUEST.getMessage());

            verify(orderQueryService).validateOrderPayable(memberId, orderId, 5000);
            verify(pointTransactionRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 차감 시 예외가 발생한다")
        void deduct_fail_whenMemberNotFound() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000, SAMPLE_PURCHASE_NAME);

            doNothing().when(orderQueryService).validateOrderPayable(memberId, orderId, 5000);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                    .thenReturn(false);
            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointDeductFacade.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("잔액 부족 시 차감이 실패한다")
        void deduct_fail_whenInsufficientBalance() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000, SAMPLE_PURCHASE_NAME);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(1000);

            doNothing().when(orderQueryService).validateOrderPayable(memberId, orderId, 5000);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                    .thenReturn(false);
            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));

            // when & then
            assertThatThrownBy(() -> pointDeductFacade.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }

        @Test
        @DisplayName("중복 차감 요청 시 예외가 발생한다")
        void deduct_fail_whenDuplicateRequest() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000, SAMPLE_PURCHASE_NAME);

            doNothing().when(orderQueryService).validateOrderPayable(memberId, orderId, 5000);
            when(pointTransactionRepository.existsByOrderIdAndStatus(orderId, PointTransactionStatus.USED))
                    .thenReturn(true);

            // when & then
            assertThatThrownBy(() -> pointDeductFacade.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.IDEMPOTENT_REQUEST.getMessage());
        }
    }
}
