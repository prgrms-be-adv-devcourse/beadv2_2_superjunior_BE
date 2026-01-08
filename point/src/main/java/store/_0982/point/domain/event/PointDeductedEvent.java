package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointHistory;

import java.time.OffsetDateTime;

public record PointDeductedEvent(
        PointHistory history,
        OffsetDateTime occurredAt
) {
    public static PointDeductedEvent from(PointHistory history) {
        return new PointDeductedEvent(history, OffsetDateTime.now());
    }
}
