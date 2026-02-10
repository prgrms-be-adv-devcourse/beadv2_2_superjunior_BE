package store_0982.dummy_data.generate_dummy_obj.commerce.row;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.PaymentMethod;

import java.time.OffsetDateTime;
import java.util.UUID;

@JsonPropertyOrder({
        "canceledOrderId",
        "orderId",
        "memberId",
        "sellerId",
        "originalPaidAmount",
        "cancelFeeAmount",
        "shippingFeeAmount",
        "refundAmount",
        "policyId",
        "policySnapshot",
        "status",
        "reason",
        "detailReason",
        "idempotencyKey",
        "paymentMethod",
        "canceledAt",
        "returnedAt",
        "createdAt",
        "updatedAt"
})
public record CanceledOrderCsvRow(
        UUID canceledOrderId,
        UUID orderId,
        UUID memberId,
        UUID sellerId,
        Long originalPaidAmount,
        Long cancelFeeAmount,
        Long shippingFeeAmount,
        Long refundAmount,
        String policyId,
        String policySnapshot,
        CancelStatus status,
        CancelReason reason,
        String detailReason,
        String idempotencyKey,
        PaymentMethod paymentMethod,
        OffsetDateTime canceledAt,
        OffsetDateTime returnedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
