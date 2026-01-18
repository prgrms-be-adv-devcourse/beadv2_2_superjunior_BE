package store._0982.batch.domain.elasticsearch;

import store._0982.batch.infrastructure.elasticsearch.GroupPurchaseReindexProjection;

import java.time.Instant;
import java.util.UUID;

public record GroupPurchaseReindexRow(
        UUID groupPurchaseId,
        String title,
        String description,
        String status,
        Instant endDate,
        long discountedPrice,
        Integer currentQuantity,
        Instant updatedAt,
        UUID productId,
        String category,
        Long price,
        UUID sellerId
) {
    public static GroupPurchaseReindexRow from(GroupPurchaseReindexProjection projection) {
        return new GroupPurchaseReindexRow(
                projection.getGroupPurchaseId(),
                projection.getTitle(),
                projection.getDescription(),
                projection.getStatus(),
                projection.getEndDate(),
                projection.getDiscountedPrice(),
                projection.getCurrentQuantity(),
                projection.getUpdatedAt(),
                projection.getProductId(),
                projection.getCategory(),
                projection.getPrice(),
                projection.getSellerId()
        );
    }
}
