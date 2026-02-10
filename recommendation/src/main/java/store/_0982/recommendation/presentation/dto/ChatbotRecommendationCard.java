package store._0982.recommendation.presentation.dto;

import store._0982.common.domain.product.ProductCategory;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ChatbotRecommendationCard(
        UUID groupPurchaseId,
        UUID productId,
        String productName,
        ProductCategory category,
        Long originalPrice,
        Long discountedPrice,
        double discountRate,
        OffsetDateTime endDate,
        long remainingMinutes
) {
}
