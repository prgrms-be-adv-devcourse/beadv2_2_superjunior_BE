package store._0982.product.infrastructure.client.member.dto;

import java.util.List;
import java.util.UUID;

public record SellerAccountListRequest(
        List<UUID> sellerIds
) {
}
