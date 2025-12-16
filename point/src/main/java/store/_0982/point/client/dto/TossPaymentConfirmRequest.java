package store._0982.point.client.dto;

import store._0982.point.application.dto.PointChargeConfirmCommand;

import java.util.UUID;

public record TossPaymentConfirmRequest(
        UUID orderId,
        long amount,
        String paymentKey
) {
    public static TossPaymentConfirmRequest from(PointChargeConfirmCommand command) {
        return new TossPaymentConfirmRequest(
                command.orderId(),
                command.amount(),
                command.paymentKey());
    }
}
