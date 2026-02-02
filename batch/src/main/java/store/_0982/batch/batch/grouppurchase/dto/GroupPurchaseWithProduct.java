package store._0982.batch.batch.grouppurchase.dto;

import store._0982.common.domain.grouppurchase.GroupPurchase;
import store._0982.common.domain.product.Product;

public record GroupPurchaseWithProduct(
        GroupPurchase groupPurchase,
        Product product
) {
}
