package store._0982.point.client.dto;

import store._0982.point.application.dto.PgConfirmCommand;

import java.util.UUID;

public record TossPaymentConfirmRequest(
        UUID orderId,
        long amount,
        String paymentKey
) {
    public static TossPaymentConfirmRequest from(PgConfirmCommand command) {
        return new TossPaymentConfirmRequest(
                command.orderId(),
                command.amount(),
                command.paymentKey());
    }
}
