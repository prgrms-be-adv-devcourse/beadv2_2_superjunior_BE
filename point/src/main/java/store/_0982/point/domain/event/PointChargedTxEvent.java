package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointChargedTxEvent(
        PointTransaction history,
        OffsetDateTime occurredAt
) {
    public static PointChargedTxEvent from(PointTransaction history) {
        return new PointChargedTxEvent(history, OffsetDateTime.now());
    }
}
