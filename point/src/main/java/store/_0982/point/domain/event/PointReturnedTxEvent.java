package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointReturnedTxEvent(
        PointTransaction history,
        OffsetDateTime occurredAt
) {
    public static PointReturnedTxEvent from(PointTransaction history) {
        return new PointReturnedTxEvent(history, OffsetDateTime.now());
    }
}
