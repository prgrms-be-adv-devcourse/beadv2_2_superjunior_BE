package store._0982.batch.infrastructure.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistory;
import store._0982.batch.domain.sellerbalance.SellerBalanceHistoryRepository;

import java.util.List;
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

    @Override
    public void saveAll(List<SellerBalanceHistory> sellerBalanceHistories) {
        sellerBalanceHistoryJpaRepository.saveAll(sellerBalanceHistories);
    }
}
