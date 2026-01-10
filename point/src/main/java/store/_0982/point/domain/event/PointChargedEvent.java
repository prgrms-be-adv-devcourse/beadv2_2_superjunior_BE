package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointHistory;

import java.time.OffsetDateTime;

public record PointChargedEvent(
        PointHistory history,
        OffsetDateTime occurredAt
) {
    public static PointChargedEvent from(PointHistory history) {
        return new PointChargedEvent(history, OffsetDateTime.now());
    }
}
