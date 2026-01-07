package store._0982.commerce.application.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.commerce.application.grouppurchase.GroupPurchaseService;
import store._0982.commerce.application.grouppurchase.dto.GroupPurchaseInternalInfo;
import store._0982.commerce.domain.sellerbalance.*;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SellerSettlementProcessor {

    private final GroupPurchaseService groupPurchaseService;
    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Transactional
    public void processGroupPurchaseSettlement(UUID sellerId, GroupPurchaseInternalInfo groupPurchase) {
        SellerBalance sellerBalance = sellerBalanceRepository.findByMemberId(sellerId)
                .orElseGet(() -> new SellerBalance(sellerId));

        sellerBalance.increaseBalance(groupPurchase.amount());
        sellerBalanceRepository.save(sellerBalance);

        sellerBalanceHistoryRepository.save(
                new SellerBalanceHistory(
                        sellerId,
                        null,
                        groupPurchase.groupPurchaseId(),
                        groupPurchase.amount(),
                        SellerBalanceHistoryStatus.CREDIT)
        );

        groupPurchaseService.markAsSettled(groupPurchase.groupPurchaseId());
    }
}
