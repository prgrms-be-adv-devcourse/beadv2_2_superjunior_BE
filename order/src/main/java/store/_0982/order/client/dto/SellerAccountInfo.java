package store._0982.order.client.dto;

import java.util.UUID;

public record SellerAccountInfo(
        UUID sellerId,
        String accountNumber,
        String bankCode,
        String accountHolder
) {
}
