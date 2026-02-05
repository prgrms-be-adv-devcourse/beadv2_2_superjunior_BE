package store._0982.point.domain.event;

import store._0982.point.domain.entity.PointTransaction;

import java.time.OffsetDateTime;

public record PointTransferredTxEvent(
        PointTransaction transaction,
        OffsetDateTime occurredAt
) {
    public static PointTransferredTxEvent from(PointTransaction transaction) {
        return new PointTransferredTxEvent(transaction, OffsetDateTime.now());
    }
}
