package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInfo;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SellerSettlementProcessor {

    private final SellerBalanceService sellerBalanceService;
    private final ProductSettlementClient productSettlementClient;

    @Transactional
    public void processSellerSettlement(UUID sellerId, List<GroupPurchaseInfo> groupPurchases) {

        Long totalAmount = groupPurchases.stream()
                .mapToLong(GroupPurchaseInfo::getTotalAmount)
                .sum();

        sellerBalanceService.increaseBalance(sellerId, totalAmount);

        for (GroupPurchaseInfo gp : groupPurchases) {
            productSettlementClient.markAsSettled(gp.groupPurchaseId());
        }
    }
}
