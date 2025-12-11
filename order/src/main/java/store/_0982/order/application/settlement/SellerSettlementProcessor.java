package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.order.client.ProductFeignClient;
import store._0982.order.client.dto.GroupPurchaseInternalInfo;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SellerSettlementProcessor {

    private final SellerBalanceService sellerBalanceService;
    private final ProductFeignClient productFeignClient;

    @Transactional
    public void processSellerSettlement(UUID sellerId, List<GroupPurchaseInternalInfo> groupPurchases) {

        Long totalAmount = groupPurchases.stream()
                .mapToLong(gp -> gp.totalAmount() != null ? gp.totalAmount() : 0L)
                .sum();

        sellerBalanceService.increaseBalance(sellerId, totalAmount);

        for (GroupPurchaseInternalInfo gp : groupPurchases) {
            productFeignClient.markAsSettled(gp.groupPurchaseId());
        }
    }
}
