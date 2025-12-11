package store._0982.order.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.order.domain.settlement.SellerBalanceHistory;
import store._0982.order.domain.settlement.SellerBalanceHistoryRepository;

@RequiredArgsConstructor
@Repository
public class SellerBalanceHistoryRepositoryAdapter implements SellerBalanceHistoryRepository {

    private final SellerBalanceHistoryJpaRepository sellerBalanceHistoryJpaRepository;

    @Override
    public void save(SellerBalanceHistory sellerBalanceHistory) {
        sellerBalanceHistoryJpaRepository.save(sellerBalanceHistory);
    }
}
