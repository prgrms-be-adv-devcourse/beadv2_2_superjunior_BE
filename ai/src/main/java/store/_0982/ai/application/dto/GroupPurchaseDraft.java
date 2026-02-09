package store._0982.ai.application.dto;

import java.time.OffsetDateTime;

public record GroupPurchaseDraft(
        String title,
        String description,
        Long discountedPrice,
        int minQuantity,
        int maxQuantity,
        OffsetDateTime suggestedStartDate,
        OffsetDateTime suggestedEndDate,
        String category,
        Long originalPrice,
        double discountRate,
        String reason,
        double confidence
) {
}
