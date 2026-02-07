package store._0982.batch.batch.grouppurchase.dto;


import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseProjection(
        UUID groupPurchaseId,
        GroupPurchaseStatus status,
        int currentQuantity,
        int minQuantity,
        UUID sellerId,
        UUID productId,
        String title,
        String description,
        Long discountedPrice,
        OffsetDateTime endDate,
        OffsetDateTime updatedAt,
        Long originalPrice,
        ProductCategory productCategory
) {
}
