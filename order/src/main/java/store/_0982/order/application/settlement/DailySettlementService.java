package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.order.domain.settlement.*;
import store._0982.order.infrastructure.client.ProductFeignClient;
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

    private final ProductFeignClient productFeignClient;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Transactional
    public void processDailySettlement() {

        try {
            List<GroupPurchaseInfo> unSettledGroupPurchase = productFeignClient.getUnSettledGroupPurchase();
            if (unSettledGroupPurchase.isEmpty()) {
                log.info("정산 대상이 없습니다.");
                return;
            }

            Map<UUID, Long> unsettledAmountBySeller = unSettledGroupPurchase.stream().collect(
                    Collectors.groupingBy(
                            GroupPurchaseInfo::sellerId,
                            Collectors.summingLong(GroupPurchaseInfo::getTotalAmount)
                    ));

            List<UUID> successGroupPurchaseIds = new ArrayList<>();
            unsettledAmountBySeller.forEach((sellerId, totalAmount) -> {
                try {
                    SellerBalance sellerBalance = sellerBalanceRepository.findByMemberId(sellerId)
                            .orElseGet(() -> new SellerBalance(sellerId));

                    sellerBalance.increaseBalance(totalAmount);
                    sellerBalanceRepository.save(sellerBalance);
                    sellerBalanceHistoryRepository.save(
                            new SellerBalanceHistory(sellerId, null, totalAmount, BalanceHistoryStatus.CREDIT)
                    );

                    unSettledGroupPurchase.stream()
                            .filter(gp -> gp.sellerId().equals(sellerId))
                            .map(GroupPurchaseInfo::groupPurchaseId)
                            .forEach(successGroupPurchaseIds::add);

                    log.info("판매자 {} 정산 완료 - 금액: {}", sellerId, totalAmount);
                } catch (Exception e) {
                    log.error("판매자 {} 정산 중 오류 발생", sellerId, e);
                }
            });

            successGroupPurchaseIds.forEach(gpId -> {
                try {
                    productFeignClient.markAsSettled(gpId);
                } catch (Exception e) {
                    log.error("공동구매 {} 정산 완료 표시 실패", gpId, e);
                }
            });

            log.info("데일리 정산 완료 - 총 {}건 처리", successGroupPurchaseIds.size());

        } catch (Exception e) {
            log.error("데일리 정산 중 오류 발생", e);
            throw e;
        }
    }
}
