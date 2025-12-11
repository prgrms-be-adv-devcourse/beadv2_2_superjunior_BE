package store._0982.order.application.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.order.application.cart.dto.CartAddCommand;
import store._0982.order.application.cart.dto.CartInfo;
import store._0982.order.domain.cart.Cart;
import store._0982.order.domain.cart.CartRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;

    public CartInfo addIntoCart(CartAddCommand command) {
        Cart cart = cartRepository.findByMemberIdAndGroupPurchaseId(
                command.memberId(),
                command.groupPurchaseId()
        ).orElse(Cart.create(command.memberId(),command.groupPurchaseId()));
        cart.add(command.quantity());
        return CartInfo.from(cartRepository.save(cart));
    }
}
