package store._0982.point.domain.repository;

import store._0982.point.domain.constant.BonusPolicyType;
import store._0982.point.domain.entity.BonusPolicy;

import java.util.Optional;
import java.util.UUID;

public interface BonusPolicyRepository {
    Optional<BonusPolicy> findBestPolicy(
            BonusPolicyType type,
            Long purchaseAmount,
            UUID groupPurchaseId,
            String category
    );
}