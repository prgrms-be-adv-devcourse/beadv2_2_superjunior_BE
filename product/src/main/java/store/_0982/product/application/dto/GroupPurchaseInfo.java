package store._0982.product.application.dto;

import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        Long discountedPrice,
        String title,
        String description,
        GroupPurchaseStatus status,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID sellerId,
        UUID productId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static GroupPurchaseInfo from(GroupPurchase groupPurchase) {
        return new GroupPurchaseInfo(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getMinQuantity(),
                groupPurchase.getMaxQuantity(),
                groupPurchase.getDiscountedPrice(),
                groupPurchase.getTitle(),
                groupPurchase.getDescription(),
                groupPurchase.getStatus(),
                groupPurchase.getStartDate(),
                groupPurchase.getEndDate(),
                groupPurchase.getSellerId(),
                groupPurchase.getProductId(),
                groupPurchase.getCreatedAt(),
                groupPurchase.getUpdatedAt()
        );
    }
}
