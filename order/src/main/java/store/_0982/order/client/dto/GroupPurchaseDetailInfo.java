package store._0982.order.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseDetailInfo(
        UUID groupPurchaseId,
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        int discountedPrice,
        int participantCount,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID sellerId,
        UUID productId,
        String originalUrl,
        int price,
        GroupPurchaseStatus status,
        OffsetDateTime createdAt
) {

}
