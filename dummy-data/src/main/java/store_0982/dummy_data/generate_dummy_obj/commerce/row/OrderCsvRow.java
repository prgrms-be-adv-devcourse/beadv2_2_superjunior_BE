package store_0982.dummy_data.generate_dummy_obj.commerce.row;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.order.OrderStatus;
import store._0982.commerce.domain.order.PaymentMethod;

@JsonPropertyOrder({
        "orderId",
        "quantity",
        "price",
        "status",
        "memberId",
        "address",
        "addressDetail",
        "postalCode",
        "receiverName",
        "sellerId",
        "groupPurchaseId",
        "idempotencyKey",
        "paymentMethod",
        "expiredAt",
        "paidAt",
        "createdAt",
        "updatedAt",
        "deletedAt",
        "returnedAt",
        "cancelRequestedAt",
        "cancelledAt"
})
public record OrderCsvRow(
        UUID orderId,
        int quantity,
        Long price,
        OrderStatus status,
        UUID memberId,
        String address,
        String addressDetail,
        String postalCode,
        String receiverName,
        UUID sellerId,
        UUID groupPurchaseId,
        String idempotencyKey,
        PaymentMethod paymentMethod,
        OffsetDateTime expiredAt,
        OffsetDateTime paidAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt,
        OffsetDateTime returnedAt,
        OffsetDateTime cancelRequestedAt,
        OffsetDateTime cancelledAt
) {
}
