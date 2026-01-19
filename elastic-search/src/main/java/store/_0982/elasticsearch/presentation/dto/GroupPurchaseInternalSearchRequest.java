package store._0982.elasticsearch.presentation.dto;

import java.util.UUID;

public record GroupPurchaseInternalSearchRequest(
        String keyword,
        String status,
        UUID sellerId,
        String category,
        float[] vector,
        int topK
) {
}
