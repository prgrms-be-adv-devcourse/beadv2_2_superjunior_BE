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
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointChargeCommand;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointChargeServiceTest {

    @Mock
    private PointTxManager pointTxManager;

    @InjectMocks
    private PointChargeService pointChargeService;

    private UUID memberId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Nested
    @DisplayName("포인트 충전")
    class ChargePoints {

        @Test
        @DisplayName("포인트를 충전한다")
        void chargePoints_success() {
            // given
            PointChargeCommand command = new PointChargeCommand(10000, idempotencyKey);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            when(pointTxManager.chargePoints(memberId, idempotencyKey, 10000)).thenReturn(pointBalance);

            // when
            PointBalanceInfo result = pointChargeService.chargePoints(command, memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(10000);
            verify(pointTxManager).chargePoints(memberId, idempotencyKey, 10000);
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 충전 시 예외가 발생한다")
        void chargePoints_fail_whenMemberNotFound() {
            // given
            PointChargeCommand command = new PointChargeCommand(10000, idempotencyKey);

            when(pointTxManager.chargePoints(memberId, idempotencyKey, 10000))
                    .thenThrow(new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> pointChargeService.chargePoints(command, memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }
}
