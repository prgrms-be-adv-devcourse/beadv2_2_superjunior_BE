package store._0982.point.application.point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import store._0982.common.exception.CustomException;
import store._0982.point.application.dto.point.PointBalanceInfo;
import store._0982.point.application.dto.point.PointTransactionInfo;
import store._0982.point.domain.constant.PointType;
import store._0982.point.domain.entity.PointBalance;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.repository.PointBalanceRepository;
import store._0982.point.domain.repository.PointTransactionRepository;
import store._0982.point.domain.vo.PointAmount;
import store._0982.point.exception.CustomErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PointPaymentServiceTest {

    @Mock
    private PointBalanceRepository pointBalanceRepository;

    @Mock
    private PointTransactionRepository pointTransactionRepository;

    @InjectMocks
    private PointPaymentService pointPaymentService;

    private UUID memberId;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("포인트 조회")
    class GetPoints {

        @Test
        @DisplayName("포인트를 조회한다")
        void getPoints_success() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            pointBalance.charge(10000);

            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.of(pointBalance));

            // when
            PointBalanceInfo result = pointPaymentService.getPoints(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.paidPoint()).isEqualTo(10000);
        }

        @Test
        @DisplayName("포인트가 없는 회원 조회 시 예외가 발생한다")
        void getPoints_fail_whenMemberPointNotFound() {
            // given
            when(pointBalanceRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> pointPaymentService.getPoints(memberId))
                    .isInstanceOf(CustomException.class)
                    .hasMessageContaining(CustomErrorCode.MEMBER_NOT_FOUND.getMessage());
        }
    }

    @Nested
    @DisplayName("포인트 내역 조회")
    class GetTransactions {

        @Test
        @DisplayName("전체 내역을 조회한다 (Type is null)")
        void getTransactions_all() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            PointTransaction transaction = PointTransaction.charged(
                    memberId, UUID.randomUUID(), PointAmount.of(1000, 500)
            );
            Page<PointTransaction> page = new PageImpl<>(List.of(transaction));

            when(pointTransactionRepository.findByAllByMemberIdAndType(memberId, null, pageable))
                    .thenReturn(page);

            // when
            Page<PointTransactionInfo> result = pointPaymentService.getTransactions(memberId, null, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).amount()).isEqualTo(1500); // 1000 + 500
            verify(pointTransactionRepository).findByAllByMemberIdAndType(memberId, null, pageable);
        }

        @Test
        @DisplayName("유상 포인트 내역을 조회한다 (Type is PAID)")
        void getTransactions_paid() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            PointTransaction transaction = PointTransaction.charged(
                    memberId, UUID.randomUUID(), PointAmount.of(1000, 0)
            );
            Page<PointTransaction> page = new PageImpl<>(List.of(transaction));

            when(pointTransactionRepository.findByAllByMemberIdAndType(memberId, PointType.PAID, pageable))
                    .thenReturn(page);

            // when
            Page<PointTransactionInfo> result = pointPaymentService.getTransactions(memberId, PointType.PAID, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).amount()).isEqualTo(1000);
            verify(pointTransactionRepository).findByAllByMemberIdAndType(memberId, PointType.PAID, pageable);
        }

        @Test
        @DisplayName("보너스 포인트 내역을 조회한다 (Type is BONUS)")
        void getTransactions_bonus() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            PointTransaction transaction = PointTransaction.bonusEarned(
                    memberId, null, UUID.randomUUID(), PointAmount.of(0, 500)
            );
            Page<PointTransaction> page = new PageImpl<>(List.of(transaction));

            when(pointTransactionRepository.findByAllByMemberIdAndType(memberId, PointType.BONUS, pageable))
                    .thenReturn(page);

            // when
            Page<PointTransactionInfo> result = pointPaymentService.getTransactions(memberId, PointType.BONUS, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).amount()).isEqualTo(500);
            verify(pointTransactionRepository).findByAllByMemberIdAndType(memberId, PointType.BONUS, pageable);
        }
    }

    @Nested
    @DisplayName("포인트 초기화")
    class InitializeBalance {

        @Test
        @DisplayName("포인트 잔액을 0으로 초기화한다")
        void initializeBalance_success() {
            // given
            PointBalance pointBalance = new PointBalance(memberId);
            when(pointBalanceRepository.save(any(PointBalance.class))).thenReturn(pointBalance);

            // when
            PointBalanceInfo result = pointPaymentService.initializeBalance(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.paidPoint()).isZero();
            assertThat(result.bonusPoint()).isZero();
            verify(pointBalanceRepository).save(any(PointBalance.class));
        }
    }
}
