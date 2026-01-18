package store._0982.commerce.application.order.event;

import store._0982.commerce.domain.cart.Cart;

import java.util.List;

public record OrderCartCompletedEvent(
        List<Cart> carts
) {
}
