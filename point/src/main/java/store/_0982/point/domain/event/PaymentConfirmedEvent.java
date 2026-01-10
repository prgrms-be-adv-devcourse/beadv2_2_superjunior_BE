package store._0982.point.domain.event;

import store._0982.point.domain.entity.Payment;

import java.time.OffsetDateTime;

public record PaymentConfirmedEvent(
        Payment payment,
        OffsetDateTime occurredAt
) {
    public static PaymentConfirmedEvent from(Payment payment) {
        return new PaymentConfirmedEvent(payment, OffsetDateTime.now());
    }
}
