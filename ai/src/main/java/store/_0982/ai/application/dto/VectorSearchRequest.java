package store._0982.ai.application.dto;

public record VectorSearchRequest(
        String keyword,
        String category,
        float[] vector,
        int topK
){
}
