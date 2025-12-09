package store._0982.elasticsearch.application.dto;

import store._0982.elasticsearch.domain.ProductDocument;

import java.time.Instant;

public record ProductDocumentCommand(
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
    public ProductDocument toDocument() {
        return ProductDocument.builder()
                .productId(productId)
                .name(name)
                .price(price)
                .category(category)
                .description(description)
                .stock(stock)
                .originalUrl(originalUrl)
                .sellerId(sellerId)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();
    }
}
