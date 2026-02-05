package store._0982.point.domain.event;

import store._0982.point.domain.entity.PgPayment;

import java.time.OffsetDateTime;

public record PaymentCanceledTxEvent(
        PgPayment pgPayment,
        OffsetDateTime occurredAt
) {
    public static PaymentCanceledTxEvent from(PgPayment pgPayment) {
        return new PaymentCanceledTxEvent(pgPayment, OffsetDateTime.now());
    }
}
