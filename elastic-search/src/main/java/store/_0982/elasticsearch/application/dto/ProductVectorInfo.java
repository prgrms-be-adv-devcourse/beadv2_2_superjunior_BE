package store._0982.elasticsearch.application.dto;

public record ProductVectorInfo(
        String productId,
        String name,
        String category,
        String description,
        Long price,
        Double score,
        float[] vector
) {
}
