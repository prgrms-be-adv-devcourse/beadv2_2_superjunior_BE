package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.order.infrastructure.client.dto.GroupPurchaseInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class DailySettlementService {

    private final ProductSettlementClient productSettlementClient;
    private final SellerBalanceService sellerBalanceService;

    @Transactional
    public void processDailySettlement() {

        try {
            List<GroupPurchaseInfo> unsettledGroupPurchases = productSettlementClient.getUnsettledGroupPurchases();
            if (unsettledGroupPurchases.isEmpty()) {
                log.info("정산 대상이 없습니다.");
                return;
            }

            Map<UUID, Long> unsettledAmountBySeller = groupBySeller(unsettledGroupPurchases);

            List<UUID> successGroupPurchaseIds = processSellerBalances(
                    unsettledGroupPurchases,
                    unsettledAmountBySeller
            );

            productSettlementClient.markAsSettledBatch(successGroupPurchaseIds);

            log.info("데일리 정산 완료 - 총 {}건 처리", successGroupPurchaseIds.size());

        } catch (Exception e) {
            log.error("데일리 정산 중 오류 발생", e);
            throw e;
        }
    }

    /**
     * 판매자별 총액 계산
     */
    private Map<UUID, Long> groupBySeller(List<GroupPurchaseInfo> groupPurchases) {
        return groupPurchases.stream()
                .collect(Collectors.groupingBy(
                        GroupPurchaseInfo::sellerId,
                        Collectors.summingLong(GroupPurchaseInfo::getTotalAmount)
                ));
    }

    /**
     * 판매자별 balance 업데이트 처리
     */
    private List<UUID> processSellerBalances(
            List<GroupPurchaseInfo> groupPurchases,
            Map<UUID, Long> amountBySeller
    ) {
        List<UUID> successGroupPurchaseIds = new ArrayList<>();

        amountBySeller.forEach((sellerId, totalAmount) -> {
            try {
                sellerBalanceService.increaseBalance(sellerId, totalAmount);
                groupPurchases.stream()
                        .filter(gp -> gp.sellerId().equals(sellerId))
                        .map(GroupPurchaseInfo::groupPurchaseId)
                        .forEach(successGroupPurchaseIds::add);

            } catch (Exception e) {
                log.error("판매자 {} 정산 중 오류 발생", sellerId, e);
            }
        });

        return successGroupPurchaseIds;
    }
}
