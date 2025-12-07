package store._0982.product.application.dto;

import store._0982.product.domain.GroupPurchase;
import store._0982.product.domain.GroupPurchaseStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        int discountedPrice,
        int participantCount,
        LocalDateTime startDate,
        LocalDate endDate,
        UUID sellerId,
        UUID productId,
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {
    public static GroupPurchaseInfo from(GroupPurchase groupPurchase, int participantCount) {
        return new GroupPurchaseInfo(
                groupPurchase.getGroupPurchaseId(),
                groupPurchase.getMinQuantity(),
                groupPurchase.getMaxQuantity(),
                groupPurchase.getTitle(),
                groupPurchase.getDescription(),
                groupPurchase.getDiscountedPrice(),
                participantCount,
                groupPurchase.getStartDate(),
                groupPurchase.getEndDate(),
                groupPurchase.getSellerId(),
                groupPurchase.getProductId(),
                groupPurchase.getStatus(),
                groupPurchase.getCreatedAt()
        );
    }
}
