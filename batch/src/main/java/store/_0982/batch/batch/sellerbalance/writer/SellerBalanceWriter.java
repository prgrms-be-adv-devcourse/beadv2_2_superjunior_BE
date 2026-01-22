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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerBalanceWriter implements ItemWriter<OrderSettlement> {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Override
    public void write(Chunk<? extends OrderSettlement> chunk) {
        List<OrderSettlement> orderSettlements = chunk.getItems().stream()
                .map(OrderSettlement.class::cast)
                .toList();

        if (orderSettlements.isEmpty()) {
            return;
        }

        Map<UUID, Long> amountBySeller = orderSettlements.stream()
                .collect(Collectors.groupingBy(
                        OrderSettlement::getSellerId,
                        Collectors.summingLong(OrderSettlement::getTotalAmount)
                ));

        List<UUID> sellerIds = new ArrayList<>(amountBySeller.keySet());
        Map<UUID, SellerBalance> sellerBalanceMap = sellerBalanceRepository.findAllByMemberIdIn(sellerIds)
                .stream()
                .collect(Collectors.toMap(SellerBalance::getMemberId, Function.identity()));

        for (UUID sellerId : sellerIds) {
            SellerBalance sellerBalance = sellerBalanceMap.computeIfAbsent(sellerId, SellerBalance::new);
            Long amount = amountBySeller.getOrDefault(sellerId, 0L);
            sellerBalance.increaseBalance(amount);
        }

        if (!sellerBalanceMap.isEmpty()) {
            sellerBalanceRepository.saveAll(new ArrayList<>(sellerBalanceMap.values()));
        }

        List<SellerBalanceHistory> histories = orderSettlements.stream()
                .map(orderSettlement -> new SellerBalanceHistory(
                        orderSettlement.getSellerId(),
                        orderSettlement.getSettlementId(),
                        orderSettlement.getGroupPurchaseId(),
                        orderSettlement.getTotalAmount(),
                        SellerBalanceHistoryStatus.CREDIT
                ))
                .toList();

        if (!histories.isEmpty()) {
            sellerBalanceHistoryRepository.saveAll(histories);
        }

        orderSettlements.forEach(OrderSettlement::markSettled);
    }
}
