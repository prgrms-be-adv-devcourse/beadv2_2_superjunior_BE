package store._0982.batch.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.settlement.SellerPayoutFailureRepository;
import store._0982.common.domain.settlement.SellerPayoutFailure;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class SellerPayoutFailureRepositoryAdapter implements SellerPayoutFailureRepository {

    private final SellerPayoutFailureJpaRepository sellerPayoutFailureJpaRepository;

    @Override
    public SellerPayoutFailure save(SellerPayoutFailure sellerPayoutFailure) {
        return sellerPayoutFailureJpaRepository.save(sellerPayoutFailure);
    }

    @Override
    public void saveAll(List<SellerPayoutFailure> failures) {
        sellerPayoutFailureJpaRepository.saveAll(failures);
    }

    @Override
    public void incrementRetryCount(UUID sellerPayoutId) {
        sellerPayoutFailureJpaRepository.incrementRetryCount(sellerPayoutId);
    }

    @Override
    public void deleteBySellerPayoutId(UUID sellerPayoutId) {
        sellerPayoutFailureJpaRepository.deleteBySellerPayoutId(sellerPayoutId);
    }
}
