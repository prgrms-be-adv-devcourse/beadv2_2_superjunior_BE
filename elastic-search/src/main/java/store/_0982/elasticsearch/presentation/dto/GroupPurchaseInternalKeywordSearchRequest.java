package store._0982.elasticsearch.presentation.dto;


import java.util.List;

public record GroupPurchaseInternalKeywordSearchRequest(
        String keyword,
        String category,
        List<String> statuses,
        int size
) {
}
