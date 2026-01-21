package store._0982.batch.batch.sellerbalance.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import store._0982.batch.application.sellerbalance.event.SellerBalanceCompleted;
import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.grouppurchase.GroupPurchaseRepository;
import store._0982.batch.domain.order.Order;
import store._0982.batch.domain.order.OrderRepository;
import store._0982.batch.domain.order.OrderStatus;
import store._0982.batch.domain.sellerbalance.*;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SellerBalanceWriter implements ItemWriter<GroupPurchase> {

    private final OrderRepository orderRepository;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final GroupPurchaseRepository groupPurchaseRepository;

    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void write(Chunk<? extends GroupPurchase> chunk) {
        List<GroupPurchase> groupPurchases = chunk.getItems().stream()
                .map(gp -> (GroupPurchase) gp)
                .toList();

        List<UUID> groupPurchaseIds = groupPurchases.stream()
                .map(GroupPurchase::getGroupPurchaseId)
                .toList();

        List<Order> orders = orderRepository.findByGroupPurchaseIdInAndStatus(
                groupPurchaseIds,
                OrderStatus.PURCHASE_CONFIRMED
        );

        Map<UUID, Long> groupPurchaseAmount = orders.stream()
                .collect(Collectors.groupingBy(
                        Order::getGroupPurchaseId,
                        Collectors.summingLong(Order::getTotalAmount)
                ));

        Map<UUID, GroupPurchase> gpMap = groupPurchases.stream()
                .collect(Collectors.toMap(GroupPurchase::getGroupPurchaseId, gp -> gp));

        List<SellerBalance> sellerBalances = new ArrayList<>();
        List<SellerBalanceHistory> sellerBalanceHistories = new ArrayList<>();
        List<GroupPurchase> groupPurchases1 = new ArrayList<>();

        for (Map.Entry<UUID, Long> entry : groupPurchaseAmount.entrySet()) {
            UUID groupPurchaseId = entry.getKey();
            Long amount = entry.getValue();

            try {
                GroupPurchase findGroupPurchase = gpMap.get(groupPurchaseId);
                UUID sellerId = findGroupPurchase.getSellerId();

                SellerBalance sellerBalance = sellerBalanceRepository.findByMemberId(sellerId)
                        .orElseThrow(() -> new CustomException(CustomErrorCode.SELLER_NOT_FOUND));

                sellerBalance.increaseBalance(amount);
                sellerBalances.add(sellerBalance);

                sellerBalanceHistories.add(
                        new SellerBalanceHistory(
                                sellerId,
                                null,
                                groupPurchaseId,
                                amount,
                                SellerBalanceHistoryStatus.CREDIT
                        )
                );

                findGroupPurchase.markAsSettled();
                groupPurchases1.add(findGroupPurchase);

                eventPublisher.publishEvent(
                        new SellerBalanceCompleted(sellerBalance, amount)
                );
            } catch (CustomException e) {
                log.error("[ERROR] [SELLER_BALANCE] {} failed", groupPurchaseId, e);
            }
        }

        sellerBalanceRepository.saveAll(sellerBalances);
        sellerBalanceHistoryRepository.saveAll(sellerBalanceHistories);
        groupPurchaseRepository.saveAll(groupPurchases1);
    }
}
