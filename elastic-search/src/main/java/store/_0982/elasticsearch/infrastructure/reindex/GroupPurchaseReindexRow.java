package store._0982.elasticsearch.infrastructure.reindex;

import java.time.Instant;
import java.util.UUID;

public record GroupPurchaseReindexRow(
        UUID groupPurchaseId,
        String title,
        String description,
        String status,
        Instant startDate,
        Instant endDate,
        int minQuantity,
        int maxQuantity,
        long discountedPrice,
        int currentQuantity,
        Instant createdAt,
        Instant updatedAt,
        UUID productId,
        String category,
        Long price,
        String originalUrl,
        UUID sellerId
) {
    public static GroupPurchaseReindexRow from(GroupPurchaseReindexProjection projection) {
        return new GroupPurchaseReindexRow(
                projection.getGroupPurchaseId(),
                projection.getTitle(),
                projection.getDescription(),
                projection.getStatus(),
                projection.getStartDate(),
                projection.getEndDate(),
                projection.getMinQuantity(),
                projection.getMaxQuantity(),
                projection.getDiscountedPrice(),
                projection.getCurrentQuantity(),
                projection.getCreatedAt(),
                projection.getUpdatedAt(),
                projection.getProductId(),
                projection.getCategory(),
                projection.getPrice(),
                projection.getOriginalUrl(),
                projection.getSellerId()
        );
    }
}
