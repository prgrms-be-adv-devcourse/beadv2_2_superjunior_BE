package store._0982.point.infrastructure;

import store._0982.point.domain.constant.BonusPolicyType;
import store._0982.point.domain.entity.BonusPolicy;

import java.util.Optional;
import java.util.UUID;

public interface BonusPolicyRepositoryCustom {
    Optional<BonusPolicy> findBestPolicy(
            BonusPolicyType type,
            Long purchaseAmount,
            UUID groupPurchaseId,
            String category
    );
}
