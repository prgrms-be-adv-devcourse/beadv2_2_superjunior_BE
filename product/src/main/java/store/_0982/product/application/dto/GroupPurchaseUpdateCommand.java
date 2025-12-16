package store._0982.product.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseUpdateCommand(
        int minQuantity,
        int maxQuantity,
        String title,
        String description,
        Long discountedPrice,
        OffsetDateTime startDate,
        OffsetDateTime endDate,
        UUID productId
) {
}
