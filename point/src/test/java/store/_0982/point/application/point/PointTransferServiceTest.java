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
import store._0982.point.application.dto.PointBalanceInfo;
import store._0982.point.application.dto.PointTransferCommand;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.exception.CustomErrorCode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PointTransferServiceTest {

    @Mock
    private PointTxManager pointTxManager;

    @InjectMocks
    private PointTransferService pointTransferService;

    private UUID memberId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        idempotencyKey = UUID.randomUUID();
    }

    @Nested
    @DisplayName("포인트 출금")
    class Transfer {

        @Test
        @DisplayName("포인트를 출금한다")
        void transfer_success() {
            // given
            PointTransferCommand command = new PointTransferCommand(50000, idempotencyKey);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(100000);
            pointBalance.transfer(50000);

            when(pointTxManager.transfer(memberId, idempotencyKey, 50000)).thenReturn(pointBalance);

            // when
            PointBalanceInfo result = pointTransferService.transfer(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(50000);
            verify(pointTxManager).transfer(memberId, idempotencyKey, 50000);
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 출금 시 예외가 발생한다")
        void transfer_fail_whenMemberNotFound() {
            // given
            PointTransferCommand command = new PointTransferCommand(50000, idempotencyKey);

            when(pointTxManager.transfer(memberId, idempotencyKey, 50000))
                    .thenThrow(new CustomException(CustomErrorCode.MEMBER_NOT_FOUND));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("출금 가능한 잔액이 부족한 경우 예외가 발생한다")
        void transfer_fail_whenInsufficientBalance() {
            // given
            PointTransferCommand command = new PointTransferCommand(100000, idempotencyKey);

            when(pointTxManager.transfer(memberId, idempotencyKey, 100000))
                    .thenThrow(new CustomException(CustomErrorCode.LACK_OF_POINT));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }

        @Test
        @DisplayName("중복 출금 요청 시 예외가 발생한다")
        void transfer_fail_whenDuplicateRequest() {
            // given
            PointTransferCommand command = new PointTransferCommand(50000, idempotencyKey);

            when(pointTxManager.transfer(memberId, idempotencyKey, 50000))
                    .thenThrow(new CustomException(CustomErrorCode.IDEMPOTENT_REQUEST));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.IDEMPOTENT_REQUEST.getMessage());
        }

        @Test
        @DisplayName("경계값: 정확히 전체 잔액을 출금할 수 있다")
        void transfer_success_exactBalance() {
            // given
            long exactAmount = 100000;
            PointTransferCommand command = new PointTransferCommand(exactAmount, idempotencyKey);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(exactAmount);
            pointBalance.transfer(exactAmount);

            when(pointTxManager.transfer(memberId, idempotencyKey, exactAmount)).thenReturn(pointBalance);

            // when
            PointBalanceInfo result = pointTransferService.transfer(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isZero();
            verify(pointTxManager).transfer(memberId, idempotencyKey, exactAmount);
        }

        @Test
        @DisplayName("경계값: 잔액보다 1원 더 많이 출금 시 예외가 발생한다")
        void transfer_fail_whenOneMoreThanBalance() {
            // given
            PointTransferCommand command = new PointTransferCommand(100001, idempotencyKey);

            when(pointTxManager.transfer(memberId, idempotencyKey, 100001))
                    .thenThrow(new CustomException(CustomErrorCode.LACK_OF_POINT));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }

        @Test
        @DisplayName("보너스 포인트만 있을 때 출금 시 예외가 발생한다")
        void transfer_fail_whenOnlyBonusPointsExist() {
            // given
            PointTransferCommand command = new PointTransferCommand(1000, idempotencyKey);

            when(pointTxManager.transfer(memberId, idempotencyKey, 1000))
                    .thenThrow(new CustomException(CustomErrorCode.LACK_OF_POINT));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }

        @Test
        @DisplayName("유료 포인트가 부족하지만 보너스 포인트를 포함하면 충분한 경우 출금 실패")
        void transfer_fail_whenPaidInsufficientButTotalSufficient() {
            // given
            PointTransferCommand command = new PointTransferCommand(15000, idempotencyKey);

            when(pointTxManager.transfer(memberId, idempotencyKey, 15000))
                    .thenThrow(new CustomException(CustomErrorCode.LACK_OF_POINT));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }
    }
}
