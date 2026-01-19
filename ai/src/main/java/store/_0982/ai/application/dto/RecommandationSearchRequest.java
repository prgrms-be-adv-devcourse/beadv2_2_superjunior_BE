package store._0982.ai.application.dto;

public record RecommandationSearchRequest (
        String keyword,
        String category,
        float[] vector,
        int topK
){
}
