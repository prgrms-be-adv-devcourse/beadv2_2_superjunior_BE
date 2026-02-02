package store._0982.ai.infrastructure.feign.search.dto;

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
