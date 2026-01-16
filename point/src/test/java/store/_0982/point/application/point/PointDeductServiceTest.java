package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.common.exception.CustomException;
import store._0982.point.application.OrderValidator;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointDeductCommand;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointDeductServiceTest {

    @Mock
    private PointTxManager pointTxManager;

    @Mock
    private OrderValidator orderValidator;

    @InjectMocks
    private PointDeductService pointDeductService;

    private UUID memberId;
    private UUID orderId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
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
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);
            pointBalance.use(5000);

            doNothing().when(orderValidator).validateOrderPayable(memberId, orderId, 5000);
            when(pointTxManager.deductPoints(memberId, orderId, idempotencyKey, 5000)).thenReturn(pointBalance);

            // when
            PointBalanceInfo result = pointDeductService.deductPoints(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(5000);
            verify(orderValidator).validateOrderPayable(memberId, orderId, 5000);
            verify(pointTxManager).deductPoints(memberId, orderId, idempotencyKey, 5000);
        }

        @Test
        @DisplayName("주문 검증 실패 시 차감이 실패한다")
        void deduct_fail_whenOrderValidationFails() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            doThrow(new CustomException(CustomErrorCode.INVALID_PAYMENT_REQUEST))
                    .when(orderValidator).validateOrderPayable(memberId, orderId, 5000);

            // when & then
            assertThatThrownBy(() -> pointDeductService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.INVALID_PAYMENT_REQUEST.getMessage());

            verify(orderValidator).validateOrderPayable(memberId, orderId, 5000);
            verify(pointTxManager, never()).deductPoints(any(), any(), any(), anyLong());
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 차감 시 예외가 발생한다")
        void deduct_fail_whenMemberNotFound() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            doNothing().when(orderValidator).validateOrderPayable(memberId, orderId, 5000);
            when(pointTxManager.deductPoints(memberId, orderId, idempotencyKey, 5000))
                    .thenThrow(new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> pointDeductService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("잔액 부족 시 차감이 실패한다")
        void deduct_fail_whenInsufficientBalance() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            doNothing().when(orderValidator).validateOrderPayable(memberId, orderId, 5000);
            when(pointTxManager.deductPoints(memberId, orderId, idempotencyKey, 5000))
                    .thenThrow(new CustomException(CustomErrorCode.LACK_OF_POINT));

            // when & then
            assertThatThrownBy(() -> pointDeductService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }

        @Test
        @DisplayName("중복 차감 요청 시 예외가 발생한다")
        void deduct_fail_whenDuplicateRequest() {
            // given
            PointDeductCommand command = new PointDeductCommand(idempotencyKey, orderId, 5000);

            doNothing().when(orderValidator).validateOrderPayable(memberId, orderId, 5000);
            when(pointTxManager.deductPoints(memberId, orderId, idempotencyKey, 5000))
                    .thenThrow(new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST));

            // when & then
            assertThatThrownBy(() -> pointDeductService.deductPoints(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.IDEMPOTENT_REQUEST.getMessage());
        }
    }
}
