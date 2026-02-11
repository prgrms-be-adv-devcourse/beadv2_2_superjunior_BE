package store._0982.batch.batch.settlement.dto;

import java.util.UUID;

public record OrderSettlementDto(
        UUID orderSettlementId,
        UUID sellerId,
        Long settlementAmount
) {
}
