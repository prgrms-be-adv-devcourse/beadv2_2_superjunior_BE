package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import store._0982.order.infrastructure.client.ProductFeignClient;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInfo;

import java.util.List;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductSettlementClient {

    private final ProductFeignClient productFeignClient;

    /**
     * 정산되지 않은 공동구매 목록 조회
     */
    public List<GroupPurchaseInfo> getUnsettledGroupPurchases() {
        return productFeignClient.getUnSettledGroupPurchase();
    }

    /**
     * 공동구매 정산 완료 표시 (단건)
     */
    public void markAsSettled(UUID groupPurchaseId) {
        productFeignClient.markAsSettled(groupPurchaseId);
    }

}
