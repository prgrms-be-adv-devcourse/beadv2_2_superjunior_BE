package store._0982.point.domain.event;

import store._0982.point.domain.entity.MemberPointHistory;

import java.time.OffsetDateTime;

public record PointReturnedEvent(
        MemberPointHistory history,
        OffsetDateTime occurredAt
) {
    public static PointReturnedEvent from(MemberPointHistory history) {
        return new PointReturnedEvent(history, OffsetDateTime.now());
    }
}
