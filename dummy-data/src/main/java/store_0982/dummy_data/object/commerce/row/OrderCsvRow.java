package store_0982.dummy_data.object.commerce.row;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import store._0982.common.domain.order.OrderStatus;
import store._0982.common.domain.order.PaymentMethod;

@JsonPropertyOrder({
        "orderId",
        "orderNumber",
        "quantity",
        "price",
        "paidPrice",
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
        "canceledAt",
        "createdAt",
        "updatedAt",
        "deletedAt"
})
public record OrderCsvRow(
        UUID orderId,
        String orderNumber,
        int quantity,
        Long price,
        Long paidPrice,
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
        OffsetDateTime canceledAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime deletedAt
) {
}
