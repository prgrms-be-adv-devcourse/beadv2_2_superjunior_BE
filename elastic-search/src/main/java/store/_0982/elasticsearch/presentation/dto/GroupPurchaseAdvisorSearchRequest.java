package store._0982.elasticsearch.presentation.dto;

import java.util.List;

public record GroupPurchaseAdvisorSearchRequest(
        String keyword,
        String category,
        float[] vector,
        int topK,
        List<String> statuses
) {
}
