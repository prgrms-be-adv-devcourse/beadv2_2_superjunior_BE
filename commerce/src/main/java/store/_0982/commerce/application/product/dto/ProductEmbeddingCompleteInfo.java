package store._0982.commerce.application.product.dto;

import store._0982.commerce.domain.product.ProductVector;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ProductEmbeddingCompleteInfo(
        UUID productId,
        int vectorSize,
        String modelVersion,
        OffsetDateTime updatedAt
) {
    public static ProductEmbeddingCompleteInfo from(ProductVector vector) {
        int size = vector.getVector() == null ? 0 : vector.getVector().length;
        return new ProductEmbeddingCompleteInfo(
                vector.getProductId(),
                size,
                vector.getModelVersion(),
                vector.getUpdatedAt()
        );
    }
}
