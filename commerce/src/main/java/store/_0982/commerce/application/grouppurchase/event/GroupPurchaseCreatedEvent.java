package store._0982.commerce.application.grouppurchase.event;

import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.product.Product;

public record GroupPurchaseCreatedEvent(
        GroupPurchase groupPurchase,
        String sellerName,
        Product product
) {
}
