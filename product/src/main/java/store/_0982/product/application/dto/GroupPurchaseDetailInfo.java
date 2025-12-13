package store._0982.product.application.dto;

import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseStatus;

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
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {
    public static GroupPurchaseDetailInfo from(GroupPurchase groupPurchase, String originalUrl, Long price) {
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
                groupPurchase.getStatus(),
                groupPurchase.getCreatedAt()
        );
    }
}
