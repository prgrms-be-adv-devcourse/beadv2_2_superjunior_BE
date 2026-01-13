package store._0982.point.domain.event;

import store._0982.point.domain.entity.PgPayment;

import java.time.OffsetDateTime;

public record PaymentConfirmedEvent(
        PgPayment pgPayment,
        OffsetDateTime occurredAt
) {
    public static PaymentConfirmedEvent from(PgPayment pgPayment) {
        return new PaymentConfirmedEvent(pgPayment, OffsetDateTime.now());
    }
}
