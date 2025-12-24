package store._0982.batch.application.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store._0982.batch.domain.sellerbalance.*;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class SellerBalanceCommandService {

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
                new SellerBalanceHistory(sellerId, null, amount, SellerBalanceHistoryStatus.CREDIT)
        );
    }
}
