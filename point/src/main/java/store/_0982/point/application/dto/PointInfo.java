package store._0982.point.application.dto;

import store._0982.point.domain.entity.Point;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PointInfo(
        UUID memberId,
        long pointBalance,
        OffsetDateTime lastUsedAt
) {
    public static PointInfo from(Point point){
        return new PointInfo(
                point.getMemberId(),
                point.getPointBalance(),
                point.getLastUsedAt()
        );
    }
}
