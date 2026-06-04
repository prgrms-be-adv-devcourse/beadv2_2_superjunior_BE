package store._0982.batch.batch.settlement.writer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;
import store._0982.batch.batch.settlement.dto.OrderSettlementDto;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.batch.domain.sellerbalance.SellerBalanceRepository;
import store._0982.batch.domain.settlement.OrderSettlementRepository;
import store._0982.common.domain.sellerbalance.SellerBalance;
import store._0982.common.domain.sellerbalance.SellerBalanceHistory;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SettlementWriter 단위 테스트")
class SettlementWriterTest {

    @Mock
    private SellerBalanceRepository sellerBalanceRepository;

    @Mock
    private SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Mock
    private OrderSettlementRepository orderSettlementRepository;

    @Captor
    private ArgumentCaptor<List<SellerBalance>> balancesCaptor;

    @Captor
    private ArgumentCaptor<List<SellerBalanceHistory>> historiesCaptor;

    @Captor
    private ArgumentCaptor<List<UUID>> settlementIdsCaptor;

    private SettlementWriter writer;

    @BeforeEach
    void setUp() {
        writer = new SettlementWriter(sellerBalanceRepository, sellerBalanceHistoryRepository, orderSettlementRepository);
    }

    @Nested
    @DisplayName("정상 처리")
    class NormalCase {

        @Test
        @DisplayName("단일 판매자의 정산 항목이 잔액에 합산된다")
        void write_singleSeller_shouldIncreaseBalance() {
            // given
            UUID sellerId = UUID.randomUUID();
            UUID settlementId1 = UUID.randomUUID();
            UUID settlementId2 = UUID.randomUUID();

            SellerBalance existingBalance = new SellerBalance(sellerId);

            when(sellerBalanceRepository.findAllByMemberIdIn(any()))
                    .thenReturn(List.of(existingBalance));

            List<OrderSettlementDto> items = List.of(
                    new OrderSettlementDto(settlementId1, sellerId, 10_000L),
                    new OrderSettlementDto(settlementId2, sellerId, 5_000L)
            );

            // when
            writer.write(new Chunk<>(items));

            // then
            verify(sellerBalanceRepository).saveAll(balancesCaptor.capture());
            SellerBalance saved = balancesCaptor.getValue().get(0);
            assertThat(saved.getSettlementBalance()).isEqualTo(15_000L);
        }

        @Test
        @DisplayName("여러 판매자의 정산 항목이 각 판매자 잔액에 별도로 합산된다")
        void write_multipleSellerIds_shouldIncreaseBalanceSeparately() {
            // given
            UUID sellerId1 = UUID.randomUUID();
            UUID sellerId2 = UUID.randomUUID();

            SellerBalance balance1 = new SellerBalance(sellerId1);
            SellerBalance balance2 = new SellerBalance(sellerId2);

            when(sellerBalanceRepository.findAllByMemberIdIn(any()))
                    .thenReturn(List.of(balance1, balance2));

            List<OrderSettlementDto> items = List.of(
                    new OrderSettlementDto(UUID.randomUUID(), sellerId1, 10_000L),
                    new OrderSettlementDto(UUID.randomUUID(), sellerId2, 20_000L)
            );

            // when
            writer.write(new Chunk<>(items));

            // then
            verify(sellerBalanceRepository).saveAll(balancesCaptor.capture());
            List<SellerBalance> savedBalances = balancesCaptor.getValue();
            assertThat(savedBalances).hasSize(2);

            SellerBalance saved1 = savedBalances.stream()
                    .filter(b -> b.getMemberId().equals(sellerId1)).findFirst().orElseThrow();
            SellerBalance saved2 = savedBalances.stream()
                    .filter(b -> b.getMemberId().equals(sellerId2)).findFirst().orElseThrow();

            assertThat(saved1.getSettlementBalance()).isEqualTo(10_000L);
            assertThat(saved2.getSettlementBalance()).isEqualTo(20_000L);
        }

        @Test
        @DisplayName("정산 항목마다 SellerBalanceHistory(CREDIT)가 생성된다")
        void write_shouldCreateCreditHistoryPerSettlement() {
            // given
            UUID sellerId = UUID.randomUUID();
            UUID settlementId1 = UUID.randomUUID();
            UUID settlementId2 = UUID.randomUUID();

            when(sellerBalanceRepository.findAllByMemberIdIn(any()))
                    .thenReturn(List.of(new SellerBalance(sellerId)));

            List<OrderSettlementDto> items = List.of(
                    new OrderSettlementDto(settlementId1, sellerId, 10_000L),
                    new OrderSettlementDto(settlementId2, sellerId, 5_000L)
            );

            // when
            writer.write(new Chunk<>(items));

            // then
            verify(sellerBalanceHistoryRepository).saveAll(historiesCaptor.capture());
            List<SellerBalanceHistory> histories = historiesCaptor.getValue();
            assertThat(histories).hasSize(2);
            assertThat(histories).allMatch(h -> h.getOrderSettlementId() != null);
        }

        @Test
        @DisplayName("처리된 정산 항목 ID 목록으로 markSettled가 호출된다")
        void write_shouldMarkSettledWithAllIds() {
            // given
            UUID sellerId = UUID.randomUUID();
            UUID settlementId1 = UUID.randomUUID();
            UUID settlementId2 = UUID.randomUUID();

            when(sellerBalanceRepository.findAllByMemberIdIn(any()))
                    .thenReturn(List.of(new SellerBalance(sellerId)));

            List<OrderSettlementDto> items = List.of(
                    new OrderSettlementDto(settlementId1, sellerId, 10_000L),
                    new OrderSettlementDto(settlementId2, sellerId, 5_000L)
            );

            // when
            writer.write(new Chunk<>(items));

            // then
            verify(orderSettlementRepository).markSettled(settlementIdsCaptor.capture());
            assertThat(settlementIdsCaptor.getValue()).containsExactlyInAnyOrder(settlementId1, settlementId2);
        }
    }

    @Nested
    @DisplayName("판매자 잔액 없음")
    class SellerBalanceNotFound {

        @Test
        @DisplayName("잔액이 없는 판매자는 새로운 SellerBalance(0원)를 생성하고 정산액을 합산한다")
        void write_whenBalanceNotFound_shouldCreateNewBalance() {
            // given
            UUID sellerId = UUID.randomUUID();
            UUID settlementId = UUID.randomUUID();

            when(sellerBalanceRepository.findAllByMemberIdIn(any())).thenReturn(List.of());

            List<OrderSettlementDto> items = List.of(
                    new OrderSettlementDto(settlementId, sellerId, 10_000L)
            );

            // when
            writer.write(new Chunk<>(items));

            // then
            verify(sellerBalanceRepository).saveAll(balancesCaptor.capture());
            SellerBalance newBalance = balancesCaptor.getValue().get(0);
            assertThat(newBalance.getMemberId()).isEqualTo(sellerId);
            assertThat(newBalance.getSettlementBalance()).isEqualTo(10_000L);
        }
    }

    @Nested
    @DisplayName("빈 청크")
    class EmptyChunk {

        @Test
        @DisplayName("빈 청크 입력 시 아무 저장도 호출되지 않는다")
        void write_withEmptyChunk_shouldNotCallAnyRepository() {
            // when
            writer.write(new Chunk<>(List.of()));

            // then
            verify(sellerBalanceRepository, never()).saveAll(any());
            verify(sellerBalanceHistoryRepository, never()).saveAll(any());
            verify(orderSettlementRepository, never()).markSettled(any());
        }
    }
}
