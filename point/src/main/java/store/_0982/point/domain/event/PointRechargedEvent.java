package store._0982.point.domain.event;

import store._0982.point.domain.entity.Payment;

import java.time.OffsetDateTime;

public record PointRechargedEvent(
        Payment payment,
        OffsetDateTime occurredAt
) {
    public static PointRechargedEvent from(Payment payment) {
        return new PointRechargedEvent(payment, OffsetDateTime.now());
    }
}
