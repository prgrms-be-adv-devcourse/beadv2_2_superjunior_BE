package store._0982.product.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchaseRegisterCommand(
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
