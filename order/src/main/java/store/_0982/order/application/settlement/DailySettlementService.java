package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.order.infrastructure.client.ProductFeignClient;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInternalInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class DailySettlementService {

    private final ProductFeignClient productFeignClient;
    private final SellerSettlementProcessor sellerSettlementProcessor;

    /**
     * 데일리 정산 메인 프로세스
     */
    @ServiceLog
    public void processDailySettlement() {
        List<GroupPurchaseInternalInfo> unsettledGroupPurchases = getUnsettledGroupPurchases();

        if (unsettledGroupPurchases.isEmpty()) {
            return;
        }

        Map<UUID, List<GroupPurchaseInternalInfo>> groupPurchasesBySeller =
                unsettledGroupPurchases.stream()
                        .collect(Collectors.groupingBy(GroupPurchaseInternalInfo::sellerId));

        processEachSellerSettlement(groupPurchasesBySeller);
    }

    @ServiceLog
    private List<GroupPurchaseInternalInfo> getUnsettledGroupPurchases() {
        return productFeignClient.getUnSettledGroupPurchase();
    }

    private void processEachSellerSettlement(
            Map<UUID, List<GroupPurchaseInternalInfo>> groupPurchasesBySeller) {
        for (Map.Entry<UUID, List<GroupPurchaseInternalInfo>> entry : groupPurchasesBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<GroupPurchaseInternalInfo> sellerGroupPurchases = entry.getValue();

            try {
                processSellerDailySettlement(sellerId, sellerGroupPurchases);
            } catch (Exception e) {
                // @ServiceLog가 이미 failed 로그를 남겼으므로 계속 진행
            }
        }
    }

    @ServiceLog
    private void processSellerDailySettlement(UUID sellerId, List<GroupPurchaseInternalInfo> groupPurchases) {
        sellerSettlementProcessor.processSellerSettlement(sellerId, groupPurchases);
    }
}
