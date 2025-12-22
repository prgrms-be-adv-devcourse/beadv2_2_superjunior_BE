package store._0982.product.domain.cart;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartRepository {
    Cart save(Cart cart);

    void deleteAllZeroQuantity();

    Optional<Cart> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);

    Optional<Cart> findById(UUID cartId);

    Page<Cart> findAllByMemberId(UUID memberId, Pageable pageable);

    void flushCart(UUID memberId);

    List<Cart> findAllByCartIdIn(List<UUID> cartIds);

    void deleteAll(List<Cart> carts);
}
