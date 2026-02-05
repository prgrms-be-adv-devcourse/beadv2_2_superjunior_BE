package store._0982.commerce.application.product.event;

import store._0982.commerce.application.product.dto.CartVectorInfo;
import store._0982.commerce.application.product.dto.OrderVectorInfo;

import java.util.List;

public record VectorCollectedEvent(
        List<OrderVectorInfo> orderVectors,
        List<CartVectorInfo> cartVectors
) {
}
