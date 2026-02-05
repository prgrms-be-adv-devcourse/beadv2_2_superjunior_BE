package store._0982.ai.infrastructure.feign.search.dto;

public record ProductSearchInfo(
        String productId,
        String category,
        Long price,
        String originalUrl,
        String sellerId
) {
}
