package store._0982.elasticsearch.application.dto;

public record ProductVectorInfo(
        String productId,
        String name,
        String description,
        Long price,
        float[] vector
) {
}
