package store._0982.point.client.dto;

import store._0982.point.application.dto.PointRefundCommand;
import store._0982.point.domain.entity.Payment;

public record TossPaymentCancelRequest(
        String paymentKey,
        long amount,
        String reason
) {
    public static TossPaymentCancelRequest from(Payment payment, PointRefundCommand command) {
        return new TossPaymentCancelRequest(
                payment.getPaymentKey(),
                payment.getAmount(),
                command.cancelReason());
    }
}
