package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointHistory;

import java.time.OffsetDateTime;

public record PointReturnedEvent(
        PointHistory history,
        OffsetDateTime occurredAt
) {
    public static PointReturnedEvent from(PointHistory history) {
        return new PointReturnedEvent(history, OffsetDateTime.now());
    }
}
