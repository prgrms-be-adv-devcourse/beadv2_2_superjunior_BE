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
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointReturnServiceTest {

    private static final String CANCEL_REASON = "테스트 환불";

    @Mock
    private PointTxManager pointTxManager;

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
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);

        doNothing().when(pointTxManager).returnPoints(memberId, orderId, idempotencyKey, 3000, CANCEL_REASON);

        // when
        pointReturnService.returnPoints(memberId, command);

        // then
        verify(pointTxManager).returnPoints(memberId, orderId, idempotencyKey, 3000, CANCEL_REASON);
    }

    @Test
    @DisplayName("존재하지 않는 회원의 포인트 반환 시 예외가 발생한다")
    void returnPoints_fail_whenMemberNotFound() {
        // given
        PointReturnCommand command = new PointReturnCommand(idempotencyKey, orderId, CANCEL_REASON, 3000);

        doThrow(new CustomException(CustomErrorCode.MEMBER_NOT_FOUND))
                .when(pointTxManager).returnPoints(memberId, orderId, idempotencyKey, 3000, CANCEL_REASON);

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

        doThrow(new CustomException(CustomErrorCode.ORDER_NOT_FOUND))
                .when(pointTxManager).returnPoints(memberId, orderId, idempotencyKey, 3000, CANCEL_REASON);

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

        doThrow(new CustomException(CustomErrorCode.INVALID_REFUND_AMOUNT))
                .when(pointTxManager).returnPoints(memberId, orderId, idempotencyKey, 5000, CANCEL_REASON);

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

        doThrow(new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST))
                .when(pointTxManager).returnPoints(memberId, orderId, idempotencyKey, 3000, CANCEL_REASON);

        // when & then
        assertThatThrownBy(() -> pointReturnService.returnPoints(memberId, command))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining(CustomErrorCode.IDEMPOTENT_REQUEST.getMessage());
    }
}
