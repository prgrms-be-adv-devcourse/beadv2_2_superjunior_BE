package store._0982.order.infrastructure.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.order.domain.cart.Cart;
import store._0982.order.domain.cart.CartRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Optional<Cart> save(Cart cart) {
        return Optional.empty();
    }

    @Override
    public void deleteAllZeroQuantity() {

    }

    @Override
    public Optional<Cart> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId) {
        return Optional.empty();
    }
}
