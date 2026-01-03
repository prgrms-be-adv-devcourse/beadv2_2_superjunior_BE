package store._0982.commerce.application.sellerbalance;

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
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceHistoryInfo;
import store._0982.commerce.application.sellerbalance.dto.SellerBalanceInfo;
import store._0982.commerce.domain.sellerbalance.SellerBalance;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistory;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.commerce.domain.sellerbalance.SellerBalanceHistoryStatus;
import store._0982.commerce.domain.sellerbalance.SellerBalanceRepository;
import store._0982.common.dto.PageResponse;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SellerBalanceServiceTest {

    @Mock
    private SellerBalanceRepository sellerBalanceRepository;

    @Mock
    private SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @InjectMocks
    private SellerBalanceService sellerBalanceService;

    @Nested
    @DisplayName("balance 조회 Service")
    class GetBalanceTest {

        @Test
        @DisplayName("존재하는 판매자의 balance를 조회한다")
        void getBalance_existingBalance() {
            // given
            UUID memberId = UUID.randomUUID();
            SellerBalance sellerBalance = new SellerBalance(memberId);

            when(sellerBalanceRepository.findByMemberId(memberId))
                    .thenReturn(Optional.of(sellerBalance));

            // when
            SellerBalanceInfo result = sellerBalanceService.getBalance(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.balance()).isZero();
            verify(sellerBalanceRepository).findByMemberId(memberId);
        }

        @Test
        @DisplayName("존재하지 않는 판매자는 새로운 balance를 생성하여 반환한다")
        void getBalance_nonExistingBalance() {
            // given
            UUID memberId = UUID.randomUUID();

            when(sellerBalanceRepository.findByMemberId(memberId))
                    .thenReturn(Optional.empty());

            // when
            SellerBalanceInfo result = sellerBalanceService.getBalance(memberId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.balance()).isZero();
            assertThat(result.sellerBalanceId()).isNotNull();
            verify(sellerBalanceRepository).findByMemberId(memberId);
        }
    }

    @Nested
    @DisplayName("balance 변동 내역 조회 API")
    class GetBalanceHistoryTest {

        @Test
        @DisplayName("판매자의 balance 변동 내역을 페이징하여 조회한다")
        void getBalanceHistory_success() {
            // given
            UUID memberId = UUID.randomUUID();
            UUID historyId1 = UUID.randomUUID();
            UUID historyId2 = UUID.randomUUID();
            UUID settlementId1 = UUID.randomUUID();
            UUID settlementId2 = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);

            SellerBalanceHistory history1 = createHistory(
                    historyId1, memberId, settlementId1, 50000L, SellerBalanceHistoryStatus.CREDIT
            );
            SellerBalanceHistory history2 = createHistory(
                    historyId2, memberId, settlementId2, 20000L, SellerBalanceHistoryStatus.DEBIT
            );

            Page<SellerBalanceHistory> page = new PageImpl<>(
                    List.of(history1, history2),
                    pageable,
                    2
            );

            when(sellerBalanceHistoryRepository.findAllMemberId(memberId, pageable))
                    .thenReturn(page);

            // when
            PageResponse<SellerBalanceHistoryInfo> result =
                    sellerBalanceService.getBalanceHistory(memberId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(2);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);

            assertThat(result.content().get(0).memberId()).isEqualTo(memberId);
            assertThat(result.content().get(0).amount()).isEqualTo(50000L);
            assertThat(result.content().get(0).status()).isEqualTo(SellerBalanceHistoryStatus.CREDIT);

            assertThat(result.content().get(1).memberId()).isEqualTo(memberId);
            assertThat(result.content().get(1).amount()).isEqualTo(20000L);
            assertThat(result.content().get(1).status()).isEqualTo(SellerBalanceHistoryStatus.DEBIT);

            verify(sellerBalanceHistoryRepository).findAllMemberId(memberId, pageable);
        }

        @Test
        @DisplayName("변동 내역이 없는 경우 빈 페이지를 반환한다")
        void getBalanceHistory_emptyResult() {
            // given
            UUID memberId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 20);
            Page<SellerBalanceHistory> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(sellerBalanceHistoryRepository.findAllMemberId(memberId, pageable))
                    .thenReturn(emptyPage);

            // when
            PageResponse<SellerBalanceHistoryInfo> result =
                    sellerBalanceService.getBalanceHistory(memberId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
            verify(sellerBalanceHistoryRepository).findAllMemberId(memberId, pageable);
        }

        @Test
        @DisplayName("두 번째 페이지를 조회한다")
        void getBalanceHistory_secondPage() {
            // given
            UUID memberId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(1, 10);

            SellerBalanceHistory history = createHistory(
                    UUID.randomUUID(), memberId, UUID.randomUUID(),
                    30000L, SellerBalanceHistoryStatus.CREDIT
            );

            Page<SellerBalanceHistory> page = new PageImpl<>(
                    List.of(history),
                    pageable,
                    15
            );

            when(sellerBalanceHistoryRepository.findAllMemberId(memberId, pageable))
                    .thenReturn(page);

            // when
            PageResponse<SellerBalanceHistoryInfo> result =
                    sellerBalanceService.getBalanceHistory(memberId, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalPages()).isEqualTo(2);
            assertThat(result.numberOfElements()).isEqualTo(1);
            verify(sellerBalanceHistoryRepository).findAllMemberId(memberId, pageable);
        }
    }

    private SellerBalanceHistory createHistory(
            UUID historyId,
            UUID memberId,
            UUID settlementId,
            Long amount,
            SellerBalanceHistoryStatus status
    ) {
        // 실제 엔티티를 생성할 수 없기 때문에 mock
        SellerBalanceHistory history = mock(SellerBalanceHistory.class);
        when(history.getHistoryId()).thenReturn(historyId);
        when(history.getMemberId()).thenReturn(memberId);
        when(history.getSettlementId()).thenReturn(settlementId);
        when(history.getAmount()).thenReturn(amount);
        when(history.getStatus()).thenReturn(status);
        when(history.getCreatedAt()).thenReturn(OffsetDateTime.now());
        return history;
    }
}
