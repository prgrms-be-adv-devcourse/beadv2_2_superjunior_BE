package store._0982.batch.batch.sellerbalance.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import store._0982.batch.domain.sellerbalance.SellerBalance;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistory;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryStatus;
import store._0982.batch.domain.sellerbalance.SellerBalanceRepository;
import store._0982.batch.domain.settlement.OrderSettlement;
import store._0982.batch.domain.settlement.OrderSettlementRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerBalanceWriter implements ItemWriter<OrderSettlement> {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;
    private final OrderSettlementRepository orderSettlementRepository;

    @Override
    public void write(Chunk<? extends OrderSettlement> chunk) {
        List<OrderSettlement> orderSettlements = chunk.getItems().stream()
                .map(OrderSettlement.class::cast)
                .toList();

        if (orderSettlements.isEmpty()) {
            return;
        }

        Map<UUID, List<OrderSettlement>> settlementsBySeller = orderSettlements.stream()
                .collect(Collectors.groupingBy(OrderSettlement::getSellerId));

        List<UUID> sellerIds = new ArrayList<>();
        Map<UUID, SellerBalance> sellerBalanceMap = sellerBalanceRepository.findAllByMemberIdIn(sellerIds)
                .stream()
                .collect(Collectors.toMap(SellerBalance::getMemberId, Function.identity()));

        List<SellerBalanceHistory> histories = new ArrayList<>(orderSettlements.size());
        List<UUID> settlementIds = new ArrayList<>(orderSettlements.size());

        for (Map.Entry<UUID, List<OrderSettlement>> entry : settlementsBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<OrderSettlement> settlements = entry.getValue();

            SellerBalance sellerBalance = sellerBalanceMap.computeIfAbsent(sellerId, SellerBalance::new);

            long totalAmount = 0L;
            for (OrderSettlement settlement : settlements) {
                totalAmount += settlement.getTotalAmount();
                histories.add(new SellerBalanceHistory(
                        settlement.getSellerId(),
                        settlement.getSettlementId(),
                        settlement.getGroupPurchaseId(),
                        settlement.getTotalAmount(),
                        SellerBalanceHistoryStatus.CREDIT
                ));
                settlementIds.add(settlement.getOrderSettlementId());
            }
            sellerBalance.increaseBalance(totalAmount);
        }

        if (!sellerBalanceMap.isEmpty()) {
            sellerBalanceRepository.saveAll(new ArrayList<>(sellerBalanceMap.values()));
        }

        if (!histories.isEmpty()) {
            sellerBalanceHistoryRepository.saveAll(histories);
        }

        orderSettlementRepository.markSettled(settlementIds);
    }
}
