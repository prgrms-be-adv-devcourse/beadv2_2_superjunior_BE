package store._0982.point.domain.event;

import store._0982.point.domain.entity.BonusEarning;

import java.time.OffsetDateTime;

public record BonusEarnedTxEvent(
        BonusEarning earning,
        OffsetDateTime occurredAt
) {
    public static BonusEarnedTxEvent from(BonusEarning earning) {
        return new BonusEarnedTxEvent(earning, OffsetDateTime.now());
    }
}
