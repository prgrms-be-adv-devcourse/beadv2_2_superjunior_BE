package store._0982.order.application.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store._0982.order.domain.cart.CartRepository;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
}
