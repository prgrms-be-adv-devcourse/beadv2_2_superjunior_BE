package store._0982.elasticsearch.presentation.dto;

import store._0982.elasticsearch.application.dto.ProductDocumentCommand;

import java.time.Instant;

public record ProductDocumentRequset(
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
    public ProductDocumentCommand toCommand() {
        return new ProductDocumentCommand(
                productId,
                name,
                price,
                category,
                description,
                stock,
                originalUrl,
                sellerId,
                createdAt,
                updatedAt
        );
    }
}
