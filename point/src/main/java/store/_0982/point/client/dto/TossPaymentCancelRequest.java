package store._0982.point.client.dto;

import store._0982.point.application.dto.PgCancelCommand;
import store._0982.point.domain.entity.PgPayment;

public record TossPaymentCancelRequest(
        String paymentKey,
        long amount,
        String reason
) {
    public static TossPaymentCancelRequest from(PgPayment pgPayment, PgCancelCommand command) {
        return new TossPaymentCancelRequest(
                pgPayment.getPaymentKey(),
                pgPayment.getAmount(),
                command.cancelReason());
    }
}
