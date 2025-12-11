package store._0982.product.application.dto;

import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseThumbnailInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        int discountedPrice,
        int currentQuantity,
        LocalDateTime startDate,
        LocalDate endDate,
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {
    public static GroupPurchaseThumbnailInfo from(GroupPurchase groupPurchase) {
        return new GroupPurchaseThumbnailInfo(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getMinQuantity(),
                groupPurchase.getMaxQuantity(),
                groupPurchase.getTitle(),
                groupPurchase.getDiscountedPrice(),
                groupPurchase.getCurrentQuantity(),
                groupPurchase.getStartDate(),
                groupPurchase.getEndDate(),
                groupPurchase.getStatus(),
                groupPurchase.getCreatedAt()
        );
    }
}
