package store._0982.batch.batch.sellerpayout.dto;

import java.util.UUID;

public record SellerAccountDto(
        UUID sellerId,
        String bankCode,
        String accountNumber,
        String accountHolder
) {
}
