package store._0982.batch.infrastructure.sellerpayout;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.sellerpayout.SellerPayoutRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class SellerPayoutRepositoryAdapter implements SellerPayoutRepository {

    private final SellerPayoutJpaRepository settlementJpaRepository;

    @Override
    public SellerPayout save(SellerPayout settlement) {
        return settlementJpaRepository.save(settlement);
    }

    @Override
    public void saveAll(List<SellerPayout> settlements) {
        settlementJpaRepository.saveAll(settlements);
    }

    @Override
    public Optional<SellerPayout> findById(UUID settlementId) {
        return settlementJpaRepository.findById(settlementId);
    }
}
