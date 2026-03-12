package store._0982.recommendation.infrastructure.feign.search.dto;

import java.util.List;

public record GroupPurchaseKeywordSearchRequest(
        String keyword,
        String category,
        List<String> statuses,
        int size
) {
}