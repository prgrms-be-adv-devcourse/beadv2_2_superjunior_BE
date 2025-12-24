package store._0982.batch.infrastructure.client.member.dto;

import java.util.List;
import java.util.UUID;

public record SellerAccountListRequest(
        List<UUID> sellerIds
) {
}
