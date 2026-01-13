package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointChargedEvent(
        PointTransaction history,
        OffsetDateTime occurredAt
) {
    public static PointChargedEvent from(PointTransaction history) {
        return new PointChargedEvent(history, OffsetDateTime.now());
    }
}
