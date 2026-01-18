package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointTransferredEvent(
        PointTransaction transaction,
        OffsetDateTime occurredAt
) {
    public static PointTransferredEvent from(PointTransaction transaction) {
        return new PointTransferredEvent(transaction, OffsetDateTime.now());
    }
}
