package store._0982.point.client.dto;

import store._0982.point.application.dto.pg.PgCancelCommand;
import store._0982.point.domain.entity.PgPayment;

public record TossPaymentCancelRequest(
        String paymentKey,
        long amount,
        String reason
) {
    public static TossPaymentCancelRequest from(PgPayment pgPayment, PgCancelCommand command) {
        return new TossPaymentCancelRequest(
                pgPayment.getPaymentKey(),
                decideCancelAmount(pgPayment, command),
                command.cancelReason());
    }

    private static long decideCancelAmount(PgPayment pgPayment, PgCancelCommand command) {
        Long cancelAmount = command.amount();
        if (cancelAmount == null) {
            return pgPayment.getAmount();
        }
        return cancelAmount;
    }
}
