package store._0982.batch.batch.settlement.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.batch.domain.sellerbalance.SellerBalanceRepository;
import store._0982.batch.domain.settlement.OrderSettlementRepository;
import store._0982.common.domain.sellerbalance.SellerBalance;
import store._0982.common.domain.sellerbalance.SellerBalanceHistory;
import store._0982.common.domain.settlement.OrderSettlement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementWriter implements ItemWriter<OrderSettlement> {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;
    private final OrderSettlementRepository orderSettlementRepository;

    @Override
    public void write(Chunk<? extends OrderSettlement> chunk) {
        List<OrderSettlement> orderSettlements = new ArrayList<>(chunk.getItems());

        if (orderSettlements.isEmpty()) {
            return;
        }

        Map<UUID, List<OrderSettlement>> settlementsBySeller = orderSettlements.stream()
                .collect(Collectors.groupingBy(OrderSettlement::getSellerId));

        List<UUID> sellerIds = new ArrayList<>(settlementsBySeller.keySet());
        Map<UUID, SellerBalance> sellerBalanceMap = sellerBalanceRepository.findAllByMemberIdIn(sellerIds)
                .stream()
                .collect(Collectors.toMap(SellerBalance::getMemberId, Function.identity()));

        List<SellerBalanceHistory> histories = new ArrayList<>(orderSettlements.size());
        List<UUID> settlementIds = new ArrayList<>(orderSettlements.size());
        List<SellerBalance> changedBalances = new ArrayList<>();

        for (Map.Entry<UUID, List<OrderSettlement>> entry : settlementsBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<OrderSettlement> settlements = entry.getValue();

            SellerBalance sellerBalance = sellerBalanceMap.get(sellerId);
            if (sellerBalance == null) {
                // TODO : 모니터링 필요
                log.warn("[WARN] [settlementJob] seller balance not found. create new balance. sellerId={}", sellerId);
                sellerBalance = new SellerBalance(sellerId);
                sellerBalanceMap.put(sellerId, sellerBalance);
            }

            long totalAmount = 0L;
            for (OrderSettlement orderSettlement : settlements) {
                totalAmount += orderSettlement.getSettlementAmount();
                histories.add(SellerBalanceHistory.createCreditHistory(
                        orderSettlement.getSellerId(),
                        orderSettlement.getOrderSettlementId(),
                        orderSettlement.getSettlementAmount()
                ));
                settlementIds.add(orderSettlement.getOrderSettlementId());
            }
            sellerBalance.increaseBalance(totalAmount);
            changedBalances.add(sellerBalance);
        }

        if (!changedBalances.isEmpty()) {
            sellerBalanceRepository.saveAll(changedBalances);
        }

        if (!histories.isEmpty()) {
            sellerBalanceHistoryRepository.saveAll(histories);
        }

        if (!settlementIds.isEmpty()) {
            orderSettlementRepository.markSettled(settlementIds);
        }
    }
}
