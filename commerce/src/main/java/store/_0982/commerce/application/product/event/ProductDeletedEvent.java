package store._0982.commerce.application.product.event;

import store._0982.commerce.domain.product.Product;

public record ProductDeletedEvent(
        Product product
) {
}
