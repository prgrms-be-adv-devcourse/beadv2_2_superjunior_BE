package store._0982.ai.application.dto;

import java.util.List;

public record AdvisorVectorSearchRequest(
        String keyword,
        String category,
        float[] vector,
        int topK,
        List<String> statuses
) {
}
