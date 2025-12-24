package store._0982.batch.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.settlement.SellerBalance;
import store._0982.batch.domain.settlement.SellerBalanceRepository;

import java.util.List;
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
    public List<SellerBalance> findAllByMemberIdIn(List<UUID> memberIds) {
        return sellerBalanceJpaRepository.findAllByMemberIdIn(memberIds);
    }

    @Override
    public void save(SellerBalance sellerBalance) {
        sellerBalanceJpaRepository.save(sellerBalance);
    }

}
