package store._0982.ai.application.dto;

import store._0982.ai.infrastructure.feign.search.dto.ProductSearchInfo;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GroupPurchase(
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
    ProductSearchInfo productSearchInfo
) {
    public static GroupPurchase from(VectorSearchResponse vectorSearchResponse) {
        return new GroupPurchase(
                vectorSearchResponse.groupPurchaseId(),
                vectorSearchResponse.minQuantity(),
                vectorSearchResponse.maxQuantity(),
                vectorSearchResponse.title(),
                vectorSearchResponse.description(),
                vectorSearchResponse.imageUrl(),
                vectorSearchResponse.discountedPrice(),
                vectorSearchResponse.status(),
                vectorSearchResponse.startDate(),
                vectorSearchResponse.endDate(),
                vectorSearchResponse.createdAt(),
                vectorSearchResponse.updatedAt(),
                vectorSearchResponse.currentQuantity(),
                vectorSearchResponse.discountRate(),
                vectorSearchResponse.productSearchInfo()
        );
    }
}
