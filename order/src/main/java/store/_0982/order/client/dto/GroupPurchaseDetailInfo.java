package store._0982.order.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseDetailInfo(
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
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {

}
