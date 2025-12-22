package store._0982.commerce.infrastructure.client.product.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseFeignDetailInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        Long discountedPrice,
        int participantCount,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID sellerId,
        UUID productId,
        String originalUrl,
        Long price,
        GroupPurchaseFeignStatus status,
        OffsetDateTime createdAt
) {

}
