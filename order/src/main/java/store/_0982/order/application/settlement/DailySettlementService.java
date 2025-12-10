package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInternalInfo;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DailySettlementService {

    private final ProductSettlementClient productSettlementClient;
    private final SellerSettlementProcessor sellerSettlementProcessor;

    /**
     * 데일리 정산 메인 프로세스
     */
    public void processDailySettlement() {

        try {
            List<GroupPurchaseInternalInfo> unsettledGroupPurchases = productSettlementClient.getUnsettledGroupPurchases();
            if (unsettledGroupPurchases.isEmpty()) {
                log.info("정산 대상이 없습니다.");
                return;
            }

            Map<UUID, List<GroupPurchaseInternalInfo>> groupPurchasesBySeller = unsettledGroupPurchases.stream()
                    .collect(Collectors.groupingBy(GroupPurchaseInternalInfo::sellerId));

            for (Map.Entry<UUID, List<GroupPurchaseInternalInfo>> entry : groupPurchasesBySeller.entrySet()) {
                UUID sellerId = entry.getKey();
                List<GroupPurchaseInternalInfo> sellerGroupPurchases = entry.getValue();

                try {
                    sellerSettlementProcessor.processSellerSettlement(sellerId, sellerGroupPurchases);
                    log.info("판매자 {} 정산 성공", sellerId);
                } catch (Exception e) {
                    log.error("판매자 {} 정산 실패", sellerId, e);
                }
            }

        } catch (Exception e) {
            log.error("데일리 정산 중 오류 발생", e);
            throw e;
        }
    }
}
