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
    public Cart save(Cart cart) {
        return Optional.of(cartJpaRepository.save(cart));
    }

    @Override
    public void deleteAllZeroQuantity() {
        cartJpaRepository.deleteAllZeroQuantity();
    }

    @Override
    public Optional<Cart> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId) {
        return cartJpaRepository.findByMemberIdAndGroupPurchaseId(memberId, groupPurchaseId);
    }
}
