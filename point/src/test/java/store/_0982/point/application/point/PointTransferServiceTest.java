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
import store._0982.point.application.dto.point.PointTransferCommand;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.exception.CustomErrorCode;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointTransferServiceTest {

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointTxManager pointTxManager;

    private PointTransferService pointTransferService;

    private UUID memberId;
    private UUID idempotencyKey;

    @BeforeEach
    void setUp() {
        pointTransferService = new PointTransferService(pointTxManager);

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

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            PointBalanceInfo result = pointTransferService.transfer(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isEqualTo(50000);
            verify(pointBalanceRepository).findByMemberId(memberId);
            verify(pointTransactionRepository).saveAndFlush(any(PointTransaction.class));
        }

        @Test
        @DisplayName("존재하지 않는 회원의 포인트 출금 시 예외가 발생한다")
        void transfer_fail_whenMemberNotFound() {
            // given
            PointTransferCommand command = new PointTransferCommand(50000, idempotencyKey);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

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
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(50000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }

        @Test
        @DisplayName("경계값: 정확히 전체 잔액을 출금할 수 있다")
        void transfer_success_exactBalance() {
            // given
            long exactAmount = 100000;
            PointTransferCommand command = new PointTransferCommand(exactAmount, idempotencyKey);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(exactAmount);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));
            when(pointTransactionRepository.saveAndFlush(any(PointTransaction.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            // when
            PointBalanceInfo result = pointTransferService.transfer(memberId, command);

            // then
            assertThat(result).isNotNull();
            assertThat(result.paidPoint()).isZero();
            verify(pointBalanceRepository).findByMemberId(memberId);
        }

        @Test
        @DisplayName("경계값: 잔액보다 1원 더 많이 출금 시 예외가 발생한다")
        void transfer_fail_whenOneMoreThanBalance() {
            // given
            PointTransferCommand command = new PointTransferCommand(100001, idempotencyKey);
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(100000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));

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
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.earnBonus(1000); // Only Bonus

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));

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
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);
            pointBalance.earnBonus(10000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));

            // when & then
            assertThatThrownBy(() -> pointTransferService.transfer(memberId, command))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.LACK_OF_POINT.getMessage());
        }
    }
}
