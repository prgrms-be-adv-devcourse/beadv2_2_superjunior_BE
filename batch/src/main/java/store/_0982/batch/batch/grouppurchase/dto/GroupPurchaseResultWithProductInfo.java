package store._0982.batch.batch.grouppurchase.dto;

import store._0982.batch.domain.grouppurchase.GroupPurchase;
import store._0982.batch.domain.product.Product;

public record GroupPurchaseResultWithProductInfo(
        GroupPurchase groupPurchase,
        Product product,
        boolean success
) {
}
