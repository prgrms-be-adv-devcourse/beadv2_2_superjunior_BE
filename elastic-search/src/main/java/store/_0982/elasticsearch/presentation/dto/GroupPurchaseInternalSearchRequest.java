package store._0982.elasticsearch.presentation.dto;


public record GroupPurchaseInternalSearchRequest(
        String keyword,
        String category,
        float[] vector,
        int topK
) {
}
