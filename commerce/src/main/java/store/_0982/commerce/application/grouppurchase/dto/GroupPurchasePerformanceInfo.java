package store._0982.commerce.application.grouppurchase.dto;

import store._0982.common.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.common.domain.product.ProductCategory;

import java.util.UUID;

public record GroupPurchasePerformanceInfo(
        UUID groupPurchaseId,
        String title,
        ProductCategory category,
        Long originalPrice,
        Long discountedPrice,
        Double discountRate,
        int minQuantity,
        int maxQuantity,
        int currentQuantity,
        GroupPurchaseStatus status,
        Double participationRate,
        int duration
) {
}
