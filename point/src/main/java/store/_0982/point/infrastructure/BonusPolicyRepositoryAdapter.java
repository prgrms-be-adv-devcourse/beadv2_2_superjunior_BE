package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.BonusPolicyType;
import store._0982.point.domain.entity.BonusPolicy;
import store._0982.point.domain.repository.BonusPolicyRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BonusPolicyRepositoryAdapter implements BonusPolicyRepository {

    private final BonusPolicyJpaRepository bonusPolicyJpaRepository;

    @Override
    public Optional<BonusPolicy> findBestPolicy(BonusPolicyType type, Long purchaseAmount, UUID groupPurchaseId, String category) {
        return bonusPolicyJpaRepository.findBestPolicy(type, purchaseAmount, groupPurchaseId, category);
    }
}