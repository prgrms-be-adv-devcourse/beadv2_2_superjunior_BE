package store._0982.recommendation.infrastructure.feign.search.dto;

import java.time.Instant;
import java.util.UUID;

public record GroupPurchaseSearchRow(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        String imageUrl,
        Long discountedPrice,
        String status,
        Instant startDate,
        Instant endDate,
        Instant createdAt,
        Instant updatedAt,
        int currentQuantity,
        UUID productId,
        String category,
        Long price,
        String originalUrl,
        UUID sellerId
) {
}
