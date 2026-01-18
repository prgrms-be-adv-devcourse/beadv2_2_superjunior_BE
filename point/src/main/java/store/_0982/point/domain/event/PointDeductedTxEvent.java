package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointDeductedTxEvent(
        PointTransaction history,
        OffsetDateTime occurredAt
) {
    public static PointDeductedTxEvent from(PointTransaction history) {
        return new PointDeductedTxEvent(history, OffsetDateTime.now());
    }
}
