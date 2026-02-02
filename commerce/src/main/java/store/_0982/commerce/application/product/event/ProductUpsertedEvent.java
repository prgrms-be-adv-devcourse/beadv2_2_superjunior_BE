package store._0982.commerce.application.product.event;

import store._0982.common.domain.product.Product;

public record ProductUpsertedEvent(
        Product product
)
{ }
