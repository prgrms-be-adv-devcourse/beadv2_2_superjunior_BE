package store._0982.point.client.dto;

import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.domain.entity.PaymentPoint;

public record TossPaymentCancelRequest(
        String paymentKey,
        int amount,
        String reason
) {
    public static TossPaymentCancelRequest from(PaymentPoint paymentPoint, PointRefundCommand command) {
        return new TossPaymentCancelRequest(
                paymentPoint.getPaymentKey(),
                paymentPoint.getAmount(),
                command.cancelReason());
    }
}
