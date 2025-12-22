package store._0982.product.infrastructure.client.member.dto;

import java.util.UUID;

public record SellerAccountInfo(
        UUID sellerId,
        String accountNumber,
        String bankCode,
        String accountHolder
) {
}
