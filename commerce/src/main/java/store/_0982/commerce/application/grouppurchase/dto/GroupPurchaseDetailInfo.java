package store._0982.commerce.application.grouppurchase.dto;

import store._0982.commerce.domain.grouppurchase.GroupPurchase;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseStatus;
import store._0982.commerce.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseDetailInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        Long price,
        Long discountedPrice,
        int currentQuantity,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID sellerId,
        UUID productId,
        String originalUrl,
        String imageUrl,
        ProductCategory category,
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {
    public static GroupPurchaseDetailInfo from(GroupPurchase groupPurchase, String originalUrl, Long price, ProductCategory category) {
        return new GroupPurchaseDetailInfo(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getMinQuantity(),
                groupPurchase.getMaxQuantity(),
                groupPurchase.getTitle(),
                groupPurchase.getDescription(),
                price,
                groupPurchase.getDiscountedPrice(),
                groupPurchase.getCurrentQuantity(),
                groupPurchase.getStartDate(),
                groupPurchase.getEndDate(),
                groupPurchase.getSellerId(),
                groupPurchase.getProductId(),
                originalUrl,
                groupPurchase.getImageUrl(),
                category,
                groupPurchase.getStatus(),
                groupPurchase.getCreatedAt()
        );
    }
}
