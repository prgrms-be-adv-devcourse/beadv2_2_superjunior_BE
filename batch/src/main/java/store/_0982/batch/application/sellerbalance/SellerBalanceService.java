package store._0982.batch.application.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.batch.domain.sellerbalance.SellerBalanceRepository;
import store._0982.batch.exception.CustomErrorCode;
import store._0982.common.domain.sellerbalance.SellerBalance;
import store._0982.common.domain.sellerbalance.SellerBalanceHistory;
import store._0982.common.domain.sellerpayout.SellerPayout;
import store._0982.common.exception.CustomException;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SellerBalanceService {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    @Transactional
    public void clearBalance(SellerPayout sellerPayout) {
        SellerBalance sellerBalance = sellerBalanceRepository.findByMemberId(sellerPayout.getSellerId())
                .orElseThrow(() -> new CustomException(CustomErrorCode.SELLER_NOT_FOUND));

        long transferAmount = sellerPayout.getTotalAmount();
        sellerBalance.decreaseBalance(transferAmount);
        sellerBalanceRepository.save(sellerBalance);

        saveSellerBalanceHistory(sellerPayout, transferAmount);
    }

    private void saveSellerBalanceHistory(SellerPayout settlement, long transferAmount) {
        SellerBalanceHistory history = SellerBalanceHistory.createDebitHistory(
                settlement.getSellerId(),
                settlement.getSellerPayoutId(),
                transferAmount
        );
        sellerBalanceHistoryRepository.save(history);
    }
}
