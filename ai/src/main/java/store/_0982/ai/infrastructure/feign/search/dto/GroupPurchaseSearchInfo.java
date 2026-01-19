package store._0982.ai.infrastructure.feign.search.dto;

import java.time.OffsetDateTime;

public record GroupPurchaseSearchInfo(
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
        Double score,
        ProductSearchInfo productSearchInfo
) {
}
