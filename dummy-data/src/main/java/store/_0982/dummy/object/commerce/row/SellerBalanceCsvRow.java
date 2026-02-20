package store._0982.dummy.object.commerce.row;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "balance_id",
        "member_id",
        "settlement_balance",
        "created_at",
        "updated_at"
})
public record SellerBalanceCsvRow(
        UUID balanceId,
        UUID memberId,
        long settlementBalance,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
