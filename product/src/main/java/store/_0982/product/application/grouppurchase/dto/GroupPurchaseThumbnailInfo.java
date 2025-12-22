package store._0982.product.application.grouppurchase.dto;

import store._0982.product.domain.grouppurchase.GroupPurchase;
import store._0982.product.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.product.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseThumbnailInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        Long discountedPrice,
        int currentQuantity,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        ProductCategory category,
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {
    public static GroupPurchaseThumbnailInfo from(GroupPurchase groupPurchase, ProductCategory category) {
        return new GroupPurchaseThumbnailInfo(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getMinQuantity(),
                groupPurchase.getMaxQuantity(),
                groupPurchase.getTitle(),
                groupPurchase.getDiscountedPrice(),
                groupPurchase.getCurrentQuantity(),
                groupPurchase.getStartDate(),
                groupPurchase.getEndDate(),
                category,
                groupPurchase.getStatus(),
                groupPurchase.getCreatedAt()
        );
    }
}
