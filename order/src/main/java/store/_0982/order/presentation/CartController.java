package store._0982.order.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import store._0982.order.application.cart.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;
}
