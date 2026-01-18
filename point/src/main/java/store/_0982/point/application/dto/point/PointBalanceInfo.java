package store._0982.point.application.dto.point;

import store._0982.point.domain.entity.PointBalance;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PointBalanceInfo(
        UUID memberId,
        long paidPoint,
        long bonusPoint,
        OffsetDateTime lastUsedAt
) {
    public static PointBalanceInfo from(PointBalance pointBalance) {
        return new PointBalanceInfo(
                pointBalance.getMemberId(),
                pointBalance.getPaidBalance(),
                pointBalance.getBonusBalance(),
                pointBalance.getLastUsedAt()
        );
    }
}
