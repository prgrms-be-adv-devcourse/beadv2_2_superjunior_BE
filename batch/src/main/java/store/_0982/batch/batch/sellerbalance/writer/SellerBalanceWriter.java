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

        if (groupPurchases.isEmpty()) {
            return;
        }

        List<UUID> groupPurchaseIds = groupPurchases.stream()
                .map(GroupPurchase::getGroupPurchaseId)
                .distinct()
                .toList();

        Map<UUID, GroupPurchase> gpMap = groupPurchases.stream()
                .collect(Collectors.toMap(GroupPurchase::getGroupPurchaseId, gp -> gp));

        List<GroupPurchaseAmountRow> amountRows =
                orderRepository.sumTotalAmountByGroupPurchaseIdsAndStatus(
                        groupPurchaseIds,
                        OrderStatus.PURCHASE_CONFIRMED
                );

        List<SellerBalance> sellerBalances = new ArrayList<>();
        List<SellerBalanceHistory> sellerBalanceHistories = new ArrayList<>();
        List<UUID> uuids = new ArrayList<>();

        for (GroupPurchaseAmountRow row : amountRows) {
            UUID groupPurchaseId = row.groupPurchaseId();
            Long amount = row.totalAmount() == null ? 0L : row.totalAmount();

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

                uuids.add(groupPurchaseId);

                eventPublisher.publishEvent(
                        new SellerBalanceCompleted(sellerBalance, amount)
                );
            } catch (CustomException e) {
                log.error("[ERROR] [SELLER_BALANCE] {} failed", groupPurchaseId, e);
            }
        }

        sellerBalanceRepository.saveAll(sellerBalances);
        sellerBalanceHistoryRepository.saveAll(sellerBalanceHistories);

        if (!uuids.isEmpty()) groupPurchaseRepository.markAsSettled(uuids);
    }
}
