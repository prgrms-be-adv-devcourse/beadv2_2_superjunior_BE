package store._0982.order.domain.cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Optional<Cart> save(Cart cart);
    void deleteAllZeroQuantity();
    Optional<Cart> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);
}
