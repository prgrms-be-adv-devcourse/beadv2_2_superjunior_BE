package store._0982.recommendation.infrastructure.feign.search.dto;

public record ProductSearchInfo(
        String productId,
        String category,
        Long price,
        String originalUrl,
        String sellerId
) {
}
