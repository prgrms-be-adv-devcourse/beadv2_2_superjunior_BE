package store._0982.commerce.application.sellerbalance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseInternalInfo;
import store._0982.commerce.domain.sellerbalance.SellerBalanceLogFormat;
import store._0982.common.log.ServiceLog;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DailySellerBalanceService {

    private final SellerSettlementProcessor sellerSettlementProcessor;
    private final GroupPurchaseService groupPurchaseService;

    @ServiceLog
    public void processDailySettlement() {
        List<GroupPurchaseInternalInfo> unsettledGroupPurchases = groupPurchaseService.getUnsettledGroupPurchases();

        if (unsettledGroupPurchases.isEmpty()) {
            return;
        }

        Map<UUID, List<GroupPurchaseInternalInfo>> groupPurchasesBySeller =
                unsettledGroupPurchases.stream()
                        .collect(Collectors.groupingBy(GroupPurchaseInternalInfo::sellerId));

        processEachSellerSettlement(groupPurchasesBySeller);
    }

    @ServiceLog
    private void processEachSellerSettlement(
            Map<UUID, List<GroupPurchaseInternalInfo>> groupPurchasesBySeller) {

        for (Map.Entry<UUID, List<GroupPurchaseInternalInfo>> entry : groupPurchasesBySeller.entrySet()) {
            UUID sellerId = entry.getKey();
            List<GroupPurchaseInternalInfo> sellerGroupPurchases = entry.getValue();

            log.info(SellerBalanceLogFormat.DAILY_SETTLEMENT_START, sellerId, sellerGroupPurchases.size());

            int successCount = 0;
            int failCount = 0;

            for (GroupPurchaseInternalInfo groupPurchaseInternalInfo : sellerGroupPurchases) {
                try {
                    sellerSettlementProcessor.processGroupPurchaseSettlement(sellerId, groupPurchaseInternalInfo);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    log.error(SellerBalanceLogFormat.DAILY_SETTLEMENT_FAIL, sellerId, groupPurchaseInternalInfo.groupPurchaseId(), e.getMessage());
                }
            }
            log.info(SellerBalanceLogFormat.DAILY_SETTLEMENT_COMPLETE, sellerId, successCount, failCount);
        }
    }

}
