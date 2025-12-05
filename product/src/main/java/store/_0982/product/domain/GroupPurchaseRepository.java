package store._0982.product.domain;

import java.util.Optional;
import java.util.UUID;

public interface GroupPurchaseRepository {

    Optional<GroupPurchase> findById(UUID purchaseId);

}
