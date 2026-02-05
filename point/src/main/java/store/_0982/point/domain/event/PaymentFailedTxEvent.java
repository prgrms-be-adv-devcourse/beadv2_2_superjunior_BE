package store._0982.point.domain.event;

import store._0982.point.domain.entity.PgPayment;

import java.time.OffsetDateTime;

public record PaymentFailedTxEvent(
        PgPayment pgPayment,
        OffsetDateTime occurredAt
) {
    public static PaymentFailedTxEvent from(PgPayment pgPayment) {
        return new PaymentFailedTxEvent(pgPayment, OffsetDateTime.now());
    }
}
