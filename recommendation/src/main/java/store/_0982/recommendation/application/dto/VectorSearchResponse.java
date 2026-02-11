package store._0982.recommendation.application.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VectorSearchResponse(
        UUID groupPurchaseId,
        Integer minQuantity,
        Integer maxQuantity,
        String title,
        String description,
        String imageUrl,
        Long discountedPrice,
        String status,
        String startDate,
        String endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Integer currentQuantity,
        Long discountRate,
        ProductSearchInfo productSearchInfo,
        ProductVectorInfo productVectorInfo
) {
}
