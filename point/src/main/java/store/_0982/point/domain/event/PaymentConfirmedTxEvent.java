package store._0982.point.domain.event;

import store._0982.point.domain.entity.PgPayment;

import java.time.OffsetDateTime;

public record PaymentConfirmedTxEvent(
        PgPayment pgPayment,
        OffsetDateTime occurredAt
) {
    public static PaymentConfirmedTxEvent from(PgPayment pgPayment) {
        return new PaymentConfirmedTxEvent(pgPayment, OffsetDateTime.now());
    }
}
