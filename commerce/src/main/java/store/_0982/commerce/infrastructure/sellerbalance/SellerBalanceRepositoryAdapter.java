package store._0982.commerce.infrastructure.sellerbalance;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.sellerbalance.SellerBalance;
import store._0982.commerce.domain.sellerbalance.SellerBalanceRepository;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class SellerBalanceRepositoryAdapter implements SellerBalanceRepository {

    private final SellerBalanceJpaRepository sellerBalanceJpaRepository;

    @Override
    public Optional<SellerBalance> findByMemberId(UUID sellerId) {
        return sellerBalanceJpaRepository.findByMemberId(sellerId);
    }

    @Override
    public void save(SellerBalance sellerBalance) {
        sellerBalanceJpaRepository.save(sellerBalance);
    }

}
