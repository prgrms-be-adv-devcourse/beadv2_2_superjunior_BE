package store._0982.elasticsearch.application.dto;

import store._0982.common.dto.event.ProductEvent;
import store._0982.elasticsearch.domain.ProductDocument;

import java.time.OffsetDateTime;

public record ProductDocumentCommand(
        String productId,
        String name,
        Integer price,
        String category,
        String description,
        Integer stock,
        String originalUrl,
        String sellerId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static ProductDocumentCommand from(ProductEvent event) {
        return new ProductDocumentCommand(
                event.productId().toString(),
                event.name(),
                event.price(),
                event.category(),
                event.description(),
                event.stock(),
                event.originalUrl(),
                event.sellerId().toString(),
                OffsetDateTime.parse(event.createdAt()),
                OffsetDateTime.parse(event.updatedAt())
        );
    }

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
