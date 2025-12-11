package store._0982.elasticsearch.application.dto;

import store._0982.common.kafka.dto.ProductEvent;
import store._0982.elasticsearch.domain.ProductDocument;

import java.time.OffsetDateTime;

public record ProductDocumentCommand(
        String productId,
        String name,
        Long price,
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
                event.getId().toString(),
                event.getName(),
                event.getPrice(),
                event.getCategory(),
                event.getDescription(),
                event.getStock(),
                event.getOriginalUrl(),
                event.getSellerId().toString(),
                OffsetDateTime.parse(event.getCreatedAt()),
                OffsetDateTime.parse(event.getUpdatedAt())
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
