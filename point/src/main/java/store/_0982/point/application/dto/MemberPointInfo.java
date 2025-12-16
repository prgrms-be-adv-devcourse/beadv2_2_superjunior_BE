package store._0982.point.application.dto;

import store._0982.point.domain.entity.MemberPoint;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MemberPointInfo(
        UUID memberId,
        long pointBalance,
        OffsetDateTime lastUsedAt
) {
    public static MemberPointInfo from(MemberPoint memberPoint){
        return new MemberPointInfo(
                memberPoint.getMemberId(),
                memberPoint.getPointBalance(),
                memberPoint.getLastUsedAt()
        );
    }
}
