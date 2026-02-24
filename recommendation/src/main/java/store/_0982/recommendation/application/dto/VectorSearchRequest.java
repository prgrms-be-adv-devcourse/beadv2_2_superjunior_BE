package store._0982.recommendation.application.dto;

public record VectorSearchRequest(
        float[] vector,
        int topK
){
}
