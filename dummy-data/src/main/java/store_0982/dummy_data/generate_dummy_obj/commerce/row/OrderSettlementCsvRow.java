package store_0982.dummy_data.generate_dummy_obj.commerce.row;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.settlement.OrderSettlementStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "orderSettlementId",
        "sellerId",
        "groupPurchaseId",
        "orderId",
        "status",
        "orderAmount",
        "platformFeeRate",
        "platformFee",
        "settlementAmount",
        "createdAt",
        "settledAt"
})
public record OrderSettlementCsvRow(
        UUID orderSettlementId,
        UUID sellerId,
        UUID groupPurchaseId,
        UUID orderId,
        OrderSettlementStatus status,
        Long orderAmount,
        Double platformFeeRate,
        Long platformFee,
        Long settlementAmount,
        OffsetDateTime createdAt,
        OffsetDateTime settledAt
) {
}
