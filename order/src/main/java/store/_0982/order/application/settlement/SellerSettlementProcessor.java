package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInternalInfo;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SellerSettlementProcessor {

    private final SellerBalanceService sellerBalanceService;
    private final ProductSettlementClient productSettlementClient;

    @Transactional
    public void processSellerSettlement(UUID sellerId, List<GroupPurchaseInternalInfo> groupPurchases) {

        Long totalAmount = groupPurchases.stream()
                .mapToLong(GroupPurchaseInternalInfo::amount)
                .sum();

        sellerBalanceService.increaseBalance(sellerId, totalAmount);

        for (GroupPurchaseInternalInfo gp : groupPurchases) {
            productSettlementClient.markAsSettled(gp.groupPurchaseId());
        }
    }
}
