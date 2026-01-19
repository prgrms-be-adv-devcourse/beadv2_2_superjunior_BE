package store._0982.ai.application.dto;

import store._0982.ai.infrastructure.feign.search.dto.ProductSearchInfo;

import java.time.OffsetDateTime;

public record RecommandationInfo (
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
    public static RecommandationInfo from(RecommandationSearchResponse recommandationSearchResponse) {
        return new RecommandationInfo(
                recommandationSearchResponse.groupPurchaseId(),
                recommandationSearchResponse.minQuantity(),
                recommandationSearchResponse.maxQuantity(),
                recommandationSearchResponse.title(),
                recommandationSearchResponse.description(),
                recommandationSearchResponse.discountedPrice(),
                recommandationSearchResponse.status(),
                recommandationSearchResponse.startDate(),
                recommandationSearchResponse.endDate(),
                recommandationSearchResponse.createdAt(),
                recommandationSearchResponse.updatedAt(),
                recommandationSearchResponse.currentQuantity(),
                recommandationSearchResponse.discountRate(),
                recommandationSearchResponse.productSearchInfo()
        );
    }
}
