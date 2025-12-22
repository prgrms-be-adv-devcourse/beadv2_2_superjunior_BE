package store._0982.product.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.product.infrastructure.client.product.ProductFeignClient;
import store._0982.product.infrastructure.client.product.dto.GroupPurchaseFeignInternalInfo;
import store._0982.product.infrastructure.settlement.SettlementLogFormat;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
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
        List<GroupPurchaseFeignInternalInfo> unsettledGroupPurchases = getUnsettledGroupPurchases();

        if (unsettledGroupPurchases.isEmpty()) {
            return;
        }

        Map<UUID, List<GroupPurchaseFeignInternalInfo>> groupPurchasesBySeller =
                unsettledGroupPurchases.stream()
                        .collect(Collectors.groupingBy(GroupPurchaseFeignInternalInfo::sellerId));

        processEachSellerSettlement(groupPurchasesBySeller);
    }

    @ServiceLog
    private List<GroupPurchaseFeignInternalInfo> getUnsettledGroupPurchases() {
        return productFeignClient.getUnSettledGroupPurchase();
    }

    @ServiceLog
    private void processEachSellerSettlement(
            Map<UUID, List<GroupPurchaseFeignInternalInfo>> groupPurchasesBySeller) {
        for (Map.Entry<UUID, List<GroupPurchaseFeignInternalInfo>> entry : groupPurchasesBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<GroupPurchaseFeignInternalInfo> sellerGroupPurchases = entry.getValue();

            try {
                log.info(SettlementLogFormat.DAILY_SETTLEMENT_START, sellerId, sellerGroupPurchases.size());
                sellerSettlementProcessor.processSellerSettlement(sellerId, sellerGroupPurchases);
                log.info(SettlementLogFormat.DAILY_SETTLEMENT_COMPLETE, sellerId);
            } catch (Exception e) {
                log.error(SettlementLogFormat.DAILY_SETTLEMENT_FAIL, sellerId, e.getMessage());
            }
        }
    }

}
