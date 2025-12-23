package store._0982.batch.settlement.monthly;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.infrastructure.client.product.ProductFeignClient;
import store._0982.commerce.infrastructure.client.product.dto.GroupPurchaseFeignInternalInfo;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SellerSettlementProcessor {

    private final SellerBalanceCommandService sellerBalanceCommandService;
    private final ProductFeignClient productFeignClient;

    @Transactional
    public void processSellerSettlement(UUID sellerId, List<GroupPurchaseFeignInternalInfo> groupPurchases) {

        Long totalAmount = groupPurchases.stream()
                .mapToLong(gp -> gp.totalAmount() != null ? gp.totalAmount() : 0L)
                .sum();

        sellerBalanceCommandService.increaseBalance(sellerId, totalAmount);

        for (GroupPurchaseFeignInternalInfo gp : groupPurchases) {
            productFeignClient.markAsSettled(gp.groupPurchaseId());
        }
    }
}
