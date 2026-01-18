package store._0982.batch.application.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.sellerbalance.*;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.exception.CustomException;

@RequiredArgsConstructor
@Service
public class SellerBalanceService {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Transactional
    public void clearBalance(Settlement settlement) {
        SellerBalance sellerBalance = sellerBalanceRepository.findByMemberId(settlement.getSellerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.SELLER_NOT_FOUND));

        long transferAmount = settlement.getSettlementAmount().longValue();
        sellerBalance.decreaseBalance(transferAmount);
        sellerBalanceRepository.save(sellerBalance);

        saveSellerBalanceHistory(settlement, transferAmount);
    }

    private void saveSellerBalanceHistory(Settlement settlement, long transferAmount) {
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
