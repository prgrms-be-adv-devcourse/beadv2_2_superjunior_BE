package store._0982.commerce.application.grouppurchase.dto;

import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseThumbnailInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        Long discountedPrice,
        Long originalPrice,
        int currentQuantity,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        ProductCategory category,
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {
    public static GroupPurchaseThumbnailInfo from(GroupPurchase groupPurchase, Long originalPrice, ProductCategory category) {
        return new GroupPurchaseThumbnailInfo(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getMinQuantity(),
                groupPurchase.getMaxQuantity(),
                groupPurchase.getTitle(),
                groupPurchase.getDiscountedPrice(),
                originalPrice,
                groupPurchase.getCurrentQuantity(),
                groupPurchase.getStartDate(),
                groupPurchase.getEndDate(),
                category,
                groupPurchase.getStatus(),
                groupPurchase.getCreatedAt()
        );
    }
}
