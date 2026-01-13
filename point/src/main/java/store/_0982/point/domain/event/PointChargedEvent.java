package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointPayment;

import java.time.OffsetDateTime;

public record PointChargedEvent(
        PointPayment history,
        OffsetDateTime occurredAt
) {
    public static PointChargedEvent from(PointPayment history) {
        return new PointChargedEvent(history, OffsetDateTime.now());
    }
}
