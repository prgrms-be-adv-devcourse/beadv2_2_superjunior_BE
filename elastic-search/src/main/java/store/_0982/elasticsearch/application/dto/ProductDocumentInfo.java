package store._0982.elasticsearch.application.dto;

import store._0982.elasticsearch.domain.ProductDocument;

import java.time.Instant;

public record ProductDocumentInfo(
        String productId,
        String name,
        Integer price,
        String category,
        String description,
        Integer stock,
        String originalUrl,
        String sellerId,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProductDocumentInfo from(ProductDocument doc) {
        return new ProductDocumentInfo(
                doc.getProductId(),
                doc.getName(),
                doc.getPrice(),
                doc.getCategory(),
                doc.getDescription(),
                doc.getStock(),
                doc.getOriginalUrl(),
                doc.getSellerId(),
                doc.getCreatedAt(),
                doc.getUpdatedAt()
        );
    }
}
