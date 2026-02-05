package store._0982.batch.infrastructure.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryRepository;
import store._0982.common.domain.sellerbalance.SellerBalanceHistory;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class SellerBalanceHistoryRepositoryAdapter implements SellerBalanceHistoryRepository {

    private final SellerBalanceHistoryJpaRepository sellerBalanceHistoryJpaRepository;

    @Override
    public void save(SellerBalanceHistory sellerBalanceHistory) {
        sellerBalanceHistoryJpaRepository.save(sellerBalanceHistory);
    }

    @Override
    public void saveAll(List<SellerBalanceHistory> sellerBalanceHistories) {
        sellerBalanceHistoryJpaRepository.saveAll(sellerBalanceHistories);
    }
}
