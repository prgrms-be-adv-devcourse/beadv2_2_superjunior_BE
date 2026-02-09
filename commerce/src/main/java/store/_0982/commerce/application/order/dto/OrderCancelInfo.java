package store._0982.commerce.application.order.dto;

import store._0982.common.domain.order.CancelReason;
import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.CanceledOrder;

import java.time.OffsetDateTime;
import java.util.UUID;

public record OrderCancelInfo(
        UUID orderId,
        CancelStatus status,
        long originalPaidAmount,
        long cancelFeeAmount,
        long shippingFeeAmount,
        long refundAmount,
        CancelReason reason,
        String detailReason,
        OffsetDateTime createdAt
) {
    public static OrderCancelInfo toOrderCancelInfo(CanceledOrder canceledOrder) {
        return new OrderCancelInfo(
                canceledOrder.getOrderId(),
                canceledOrder.getStatus(),
                canceledOrder.getOriginalPaidAmount(),
                canceledOrder.getCancelFeeAmount(),
                canceledOrder.getShippingFeeAmount(),
                canceledOrder.getRefundAmount(),
                canceledOrder.getReason(),
                canceledOrder.getDetailReason(),
                canceledOrder.getCreatedAt()
        );
    }
}
