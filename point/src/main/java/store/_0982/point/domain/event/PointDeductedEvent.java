package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointDeductedEvent(
        PointTransaction history,
        OffsetDateTime occurredAt
) {
    public static PointDeductedEvent from(PointTransaction history) {
        return new PointDeductedEvent(history, OffsetDateTime.now());
    }
}
