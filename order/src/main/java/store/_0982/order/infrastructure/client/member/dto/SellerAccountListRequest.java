package store._0982.order.infrastructure.client.member.dto;

import java.util.List;
import java.util.UUID;

public record SellerAccountListRequest(
        List<UUID> sellerIds
) {
}
