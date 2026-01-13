package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointPayment;

import java.time.OffsetDateTime;

public record PointDeductedEvent(
        PointPayment history,
        OffsetDateTime occurredAt
) {
    public static PointDeductedEvent from(PointPayment history) {
        return new PointDeductedEvent(history, OffsetDateTime.now());
    }
}
