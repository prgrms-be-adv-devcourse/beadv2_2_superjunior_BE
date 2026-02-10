package store._0982.recommendation.application.dto;

import java.util.List;

public record AdvisorVectorSearchRequest(
        String keyword,
        String category,
        float[] vector,
        int topK,
        List<String> statuses
) {
}