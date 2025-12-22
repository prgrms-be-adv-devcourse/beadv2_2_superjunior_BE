package store._0982.product.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.product.domain.settlement.SellerBalanceHistory;
import store._0982.product.domain.settlement.SellerBalanceHistoryRepository;

import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class SellerBalanceHistoryRepositoryAdapter implements SellerBalanceHistoryRepository {

    private final SellerBalanceHistoryJpaRepository sellerBalanceHistoryJpaRepository;

    @Override
    public void save(SellerBalanceHistory sellerBalanceHistory) {
        sellerBalanceHistoryJpaRepository.save(sellerBalanceHistory);
    }

    @Override
    public Page<SellerBalanceHistory> findAllMemberId(UUID memberId, Pageable pageable) {
        return sellerBalanceHistoryJpaRepository.findAllByMemberId(memberId, pageable);
    }

}
