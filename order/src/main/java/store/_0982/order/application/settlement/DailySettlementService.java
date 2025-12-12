package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.common.log.ServiceLog;
import store._0982.order.client.ProductFeignClient;
import store._0982.order.client.dto.GroupPurchaseInternalInfo;

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

    private static final String SETTLEMENT_START = "[SETTLEMENT] [Seller:%s] started - 공구 수: %d";
    private static final String SETTLEMENT_COMPLETE = "[SETTLEMENT] [Seller:%s] completed";
    private static final String SETTLEMENT_FAIL = "[SETTLEMENT] [Seller:%s] failed - %s";

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

    @ServiceLog
    private void processEachSellerSettlement(
            Map<UUID, List<GroupPurchaseInternalInfo>> groupPurchasesBySeller) {
        for (Map.Entry<UUID, List<GroupPurchaseInternalInfo>> entry : groupPurchasesBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<GroupPurchaseInternalInfo> sellerGroupPurchases = entry.getValue();

            try {
                log.info(String.format(SETTLEMENT_START, sellerId, sellerGroupPurchases.size()));
                processSellerDailySettlement(sellerId, sellerGroupPurchases);
                log.info(String.format(SETTLEMENT_COMPLETE, sellerId));
            } catch (Exception e) {
                log.error(String.format(SETTLEMENT_FAIL, sellerId, e.getMessage()), e);
            }
        }
    }

    private void processSellerDailySettlement(UUID sellerId, List<GroupPurchaseInternalInfo> groupPurchases) {
        sellerSettlementProcessor.processSellerSettlement(sellerId, groupPurchases);
    }
}
