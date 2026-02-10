package store._0982.elasticsearch.infrastructure.client.commerce.dto;

import java.util.List;
import java.util.UUID;

public record GroupPurchaseIdsRequest(
        List<UUID> ids
) {
}
