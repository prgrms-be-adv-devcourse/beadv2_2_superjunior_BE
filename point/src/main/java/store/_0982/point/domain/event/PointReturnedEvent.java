package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointPayment;

import java.time.OffsetDateTime;

public record PointReturnedEvent(
        PointPayment history,
        OffsetDateTime occurredAt
) {
    public static PointReturnedEvent from(PointPayment history) {
        return new PointReturnedEvent(history, OffsetDateTime.now());
    }
}
