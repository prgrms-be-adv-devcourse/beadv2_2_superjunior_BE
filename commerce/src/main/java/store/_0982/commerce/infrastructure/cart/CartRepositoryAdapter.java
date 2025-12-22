package store._0982.commerce.infrastructure.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.cart.Cart;
import store._0982.commerce.domain.cart.CartRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartRepository {

    private final CartJpaRepository cartJpaRepository;

    @Override
    public Cart save(Cart cart) {
        return cartJpaRepository.save(cart);
    }

    @Override
    public void deleteAllZeroQuantity() {
        cartJpaRepository.deleteAllZeroQuantity();
    }

    public Optional<Cart> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId) {
        return cartJpaRepository.findByMemberIdAndGroupPurchaseId(memberId, groupPurchaseId);
    }

    @Override
    public Page<Cart> findAllByMemberId(UUID memberId, Pageable pageable) {
        return cartJpaRepository.findAllByMemberId(memberId, pageable);
    }

    @Override
    public Optional<Cart> findById(UUID cartId) {
        return cartJpaRepository.findById(cartId);
    }

    @Override
    public void flushCart(UUID memberId) {
        cartJpaRepository.flushCart(memberId);
    }

    @Override
    public List<Cart> findAllByCartIdIn(List<UUID> cartIds) {
        return cartJpaRepository.findAllByCartIdIn(cartIds);
    }

    @Override
    public void deleteAll(List<Cart> carts) {
        cartJpaRepository.deleteAll(carts);
    }

}
