package store._0982.batch.application.sellerbalance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.batch.domain.sellerbalance.SellerBalanceRepository;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.domain.sellerbalance.SellerBalance;
import store._0982.common.domain.sellerbalance.SellerBalanceHistoryStatus;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.exception.CustomException;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerBalanceService 단위 테스트")
class SellerBalanceServiceTest {

    @Mock
    private SellerBalanceRepository sellerBalanceRepository;

    @Mock
    private SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    private SellerBalanceService service;

    @BeforeEach
    void setUp() {
        service = new SellerBalanceService(sellerBalanceRepository, sellerBalanceHistoryRepository);
    }

    private SellerPayout createPayout(UUID sellerId, long amount) {
        return SellerPayout.createSellerPayout(
                sellerId,
                OffsetDateTime.now().minusMonths(1),
                OffsetDateTime.now(),
                amount,
                null,
                null
        );
    }

    @Nested
    @DisplayName("정상 처리")
    class NormalCase {

        @Test
        @DisplayName("판매자 잔액이 송금액만큼 차감된다")
        void clearBalance_shouldDecreaseBalanceByTransferAmount() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerBalance balance = new SellerBalance(sellerId);
            balance.increaseBalance(50_000L);

            SellerPayout payout = createPayout(sellerId, 50_000L);

            when(sellerBalanceRepository.findByMemberId(sellerId)).thenReturn(Optional.of(balance));

            // when
            service.clearBalance(payout);

            // then
            verify(sellerBalanceRepository).save(balance);
            assertThat(balance.getSettlementBalance()).isEqualTo(0L);
        }

        @Test
        @DisplayName("일부 금액만 차감되면 나머지 잔액이 남는다")
        void clearBalance_shouldLeaveRemainingBalance() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerBalance balance = new SellerBalance(sellerId);
            balance.increaseBalance(80_000L);

            SellerPayout payout = createPayout(sellerId, 50_000L);

            when(sellerBalanceRepository.findByMemberId(sellerId)).thenReturn(Optional.of(balance));

            // when
            service.clearBalance(payout);

            // then
            verify(sellerBalanceRepository).save(balance);
            assertThat(balance.getSettlementBalance()).isEqualTo(30_000L);
        }

        @Test
        @DisplayName("차감 후 SellerBalance가 저장된다")
        void clearBalance_shouldSaveUpdatedBalance() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerBalance balance = new SellerBalance(sellerId);
            balance.increaseBalance(50_000L);

            SellerPayout payout = createPayout(sellerId, 50_000L);

            when(sellerBalanceRepository.findByMemberId(sellerId)).thenReturn(Optional.of(balance));

            // when
            service.clearBalance(payout);

            // then
            verify(sellerBalanceRepository, times(1)).save(any(SellerBalance.class));
        }

        @Test
        @DisplayName("DEBIT 상태의 SellerBalanceHistory가 저장된다")
        void clearBalance_shouldSaveDebitHistory() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerBalance balance = new SellerBalance(sellerId);
            balance.increaseBalance(50_000L);

            SellerPayout payout = createPayout(sellerId, 50_000L);

            when(sellerBalanceRepository.findByMemberId(sellerId)).thenReturn(Optional.of(balance));

            // when
            service.clearBalance(payout);

            // then
            verify(sellerBalanceHistoryRepository).save(argThat(history ->
                    history.getMemberId().equals(sellerId)
                    && history.getSellerPayoutId().equals(payout.getSellerPayoutId())
                    && history.getAmount().equals(50_000L)
                    && history.getStatus() == SellerBalanceHistoryStatus.DEBIT
            ));
        }

        @Test
        @DisplayName("히스토리의 orderSettlementId는 null이다")
        void clearBalance_shouldSaveHistoryWithNullOrderSettlementId() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerBalance balance = new SellerBalance(sellerId);
            balance.increaseBalance(50_000L);

            SellerPayout payout = createPayout(sellerId, 50_000L);

            when(sellerBalanceRepository.findByMemberId(sellerId)).thenReturn(Optional.of(balance));

            // when
            service.clearBalance(payout);

            // then
            verify(sellerBalanceHistoryRepository).save(argThat(history ->
                    history.getOrderSettlementId() == null
            ));
        }
    }

    @Nested
    @DisplayName("판매자 잔액 없음")
    class SellerNotFound {

        @Test
        @DisplayName("판매자 잔액이 없으면 SELLER_NOT_FOUND 예외가 발생한다")
        void clearBalance_shouldThrowWhenSellerBalanceNotFound() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);

            when(sellerBalanceRepository.findByMemberId(sellerId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> service.clearBalance(payout))
                    .isInstanceOf(CustomException.class)
                    .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                            .isEqualTo(CustomErrorCode.SELLER_NOT_FOUND));
        }

        @Test
        @DisplayName("SELLER_NOT_FOUND 예외 발생 시 잔액 저장이 호출되지 않는다")
        void clearBalance_shouldNotSaveWhenSellerNotFound() {
            // given
            UUID sellerId = UUID.randomUUID();
            SellerPayout payout = createPayout(sellerId, 50_000L);

            when(sellerBalanceRepository.findByMemberId(sellerId)).thenReturn(Optional.empty());

            // when
            try { service.clearBalance(payout); } catch (CustomException ignored) {}

            // then
            verify(sellerBalanceRepository, never()).save(any());
            verify(sellerBalanceHistoryRepository, never()).save(any());
        }
    }
}