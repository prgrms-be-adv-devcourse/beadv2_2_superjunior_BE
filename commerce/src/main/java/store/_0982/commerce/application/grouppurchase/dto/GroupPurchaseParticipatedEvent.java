package store._0982.commerce.application.grouppurchase.dto;

import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.product.Product;

public record GroupPurchaseParticipatedEvent (
        GroupPurchase groupPurchase,
        String sellerName,
        Product product
) {
}
