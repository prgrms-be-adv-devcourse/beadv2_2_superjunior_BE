package store._0982.point.domain.event;

import store._0982.point.domain.entity.PaymentPoint;

import java.time.OffsetDateTime;

public record PointRechargedEvent(
        PaymentPoint paymentPoint,
        OffsetDateTime occurredAt
) {
    public static PointRechargedEvent from(PaymentPoint paymentPoint) {
        return new PointRechargedEvent(paymentPoint, OffsetDateTime.now());
    }
}
