package store._0982.order.application.settlement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.order.domain.settlement.BalanceHistoryStatus;
import store._0982.order.domain.settlement.SellerBalance;
import store._0982.order.domain.settlement.SellerBalanceHistory;
import store._0982.order.domain.settlement.SellerBalanceHistoryRepository;
import store._0982.order.domain.settlement.SellerBalanceRepository;

import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class SellerBalanceService {

    private final SellerBalanceRepository sellerBalanceRepository;
    private final SellerBalanceHistoryRepository sellerBalanceHistoryRepository;

    /**
     * 판매자 balance 증가 및 이력 저장
     */
    @Transactional
    public void increaseBalance(UUID sellerId, Long amount) {
        SellerBalance sellerBalance = sellerBalanceRepository.findByMemberId(sellerId)
                .orElseGet(() -> new SellerBalance(sellerId));

        sellerBalance.increaseBalance(amount);
        sellerBalanceRepository.save(sellerBalance);

        sellerBalanceHistoryRepository.save(
                new SellerBalanceHistory(sellerId, null, amount, BalanceHistoryStatus.CREDIT)
        );
    }
}
