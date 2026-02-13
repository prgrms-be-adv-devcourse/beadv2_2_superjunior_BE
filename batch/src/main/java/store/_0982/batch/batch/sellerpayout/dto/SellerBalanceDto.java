package store._0982.batch.batch.sellerpayout.dto;

import java.util.UUID;

public record SellerBalanceDto(
        UUID memberId,
        Long settlementBalance
) {
}
