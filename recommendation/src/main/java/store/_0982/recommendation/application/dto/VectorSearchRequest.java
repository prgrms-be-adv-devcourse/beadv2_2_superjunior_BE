package store._0982.recommendation.application.dto;

public record VectorSearchRequest(
        String keyword,
        String category,
        float[] vector,
        int topK
){
}
