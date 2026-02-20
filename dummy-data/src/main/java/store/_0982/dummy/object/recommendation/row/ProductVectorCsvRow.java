package store._0982.dummy.object.recommendation.row;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.vector.ProductVector;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "productId",
        "vector",
        "modelVersion",
        "dimensionSize",
        "updatedAt"
})
public record ProductVectorCsvRow(
        UUID productId,
        String vector,
        String modelVersion,
        int dimensionSize,
        OffsetDateTime updatedAt
) {
    public static ProductVectorCsvRow from(ProductVector vector, String vectorLiteral) {
        return new ProductVectorCsvRow(
                vector.getProductId(),
                vectorLiteral,
                vector.getModelVersion(),
                vector.getDimensionSize(),
                vector.getUpdatedAt()
        );
    }
}
