package store._0982.commerce.application.grouppurchase.dto;

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
        UUID productId,
        String imageUrl
) {
}
