package store._0982.point.application.dto;

import store._0982.point.domain.entity.PointBalance;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PointInfo(
        UUID memberId,
        long paidPoint,
        long bonusPoint,
        OffsetDateTime lastUsedAt
) {
    public static PointInfo from(PointBalance pointBalance) {
        return new PointInfo(
                pointBalance.getMemberId(),
                pointBalance.getPaidPoint(),
                pointBalance.getBonusPoint(),
                pointBalance.getLastUsedAt()
        );
    }
}
