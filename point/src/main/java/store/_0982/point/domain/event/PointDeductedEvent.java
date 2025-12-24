package store._0982.point.domain.event;

import store._0982.point.domain.entity.MemberPointHistory;

import java.time.OffsetDateTime;

public record PointDeductedEvent(
        MemberPointHistory history,
        OffsetDateTime occurredAt
) {
    public static PointDeductedEvent from(MemberPointHistory history) {
        return new PointDeductedEvent(history, OffsetDateTime.now());
    }
}
