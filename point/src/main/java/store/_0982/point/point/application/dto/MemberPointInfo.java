package store._0982.point.point.application.dto;

import store._0982.point.point.domain.MemberPoint;

import java.util.UUID;

public record MemberPointInfo(
        UUID memberId,
        int pointBalance
) {
    public static MemberPointInfo from(MemberPoint memberPoint){
        return new MemberPointInfo(
                memberPoint.getMemberId(),
                memberPoint.getPointBalance()
        );
    }
}
