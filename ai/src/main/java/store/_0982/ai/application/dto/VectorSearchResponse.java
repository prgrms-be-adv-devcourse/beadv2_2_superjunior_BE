package store._0982.ai.application.dto;

import store._0982.ai.infrastructure.feign.search.dto.ProductSearchInfo;

import java.time.OffsetDateTime;

public record VectorSearchResponse(
        String groupPurchaseId,
        Integer minQuantity,
        Integer maxQuantity,
        String title,
        String description,
        Long discountedPrice,
        String status,
        String startDate,
        String endDate,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        Integer currentQuantity,
        Long discountRate,
        ProductSearchInfo productSearchInfo
) {
}
