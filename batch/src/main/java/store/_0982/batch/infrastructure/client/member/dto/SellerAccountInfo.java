package store._0982.batch.infrastructure.client.member.dto;

import java.util.UUID;

public record SellerAccountInfo(
        UUID sellerId,
        String accountNumber,
        String bankCode,
        String accountHolder
) {
}
