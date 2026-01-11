package store._0982.elasticsearch.application.dto;

public record ProductSearchInfo(
        String productId,
        String category,
        Long price,
        String originalUrl,
        String sellerId
) {
}
