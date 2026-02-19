package store_0982.dummy_data.generate_dummy_obj.commerce.row;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.sellerbalance.SellerBalanceHistoryStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "historyId",
        "memberId",
        "sellerPayoutId",
        "orderSettlementId",
        "amount",
        "status",
        "createdAt"
})
public record SellerBalanceHistoryCsvRow(
        UUID historyId,
        UUID memberId,
        UUID sellerPayoutId,
        UUID orderSettlementId,
        Long amount,
        SellerBalanceHistoryStatus status,
        OffsetDateTime createdAt
) {
}
