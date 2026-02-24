package store._0982.recommendation.infrastructure.feign.commerce.dto;

import java.util.List;
import java.util.UUID;

public record GroupPurchaseIdsRequest(
        List<UUID> ids
) {
}
