package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointReturnedEvent(
        PointTransaction history,
        OffsetDateTime occurredAt
) {
    public static PointReturnedEvent from(PointTransaction history) {
        return new PointReturnedEvent(history, OffsetDateTime.now());
    }
}
