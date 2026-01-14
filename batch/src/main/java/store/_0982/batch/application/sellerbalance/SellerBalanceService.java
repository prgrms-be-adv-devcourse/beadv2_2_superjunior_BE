package store._0982.batch.application.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.sellerbalance.*;
import store._0982.batch.domain.settlement.Settlement;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SellerBalanceService {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    /**
     * 판매자 balance 증가 및 이력 저장
     */
    @Transactional
    public void increaseBalance(UUID sellerId, Long amount, UUID groupPurchaseId) {
        SellerBalance sellerBalance = sellerBalanceRepository.findByMemberId(sellerId)
                .orElseGet(() -> new SellerBalance(sellerId));

        sellerBalance.increaseBalance(amount);
        sellerBalanceRepository.save(sellerBalance);

        sellerBalanceHistoryRepository.save(
                new SellerBalanceHistory(sellerId, null, groupPurchaseId, amount, SellerBalanceHistoryStatus.CREDIT)
        );
    }

    @Transactional
    public void saveSellerBalanceHistory(Settlement settlement, long transferAmount) {
        SellerBalanceHistory history = new SellerBalanceHistory(
                settlement.getSellerId(),
                settlement.getSettlementId(),
                null,
                transferAmount,
                SellerBalanceHistoryStatus.DEBIT
        );
        sellerBalanceHistoryRepository.save(history);
    }
}
