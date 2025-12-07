package store._0982.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import store._0982.point.common.exception.CustomErrorCode;
import store._0982.point.common.exception.CustomException;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "member_point", schema = "point_schema")
public class MemberPoint {

    @Id
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    // TODO: 밸런스를 정말 안전하게 long 타입으로 관리하는 게 좋을까?
    @Column(name = "point_balance")
    private Integer pointBalance;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    public MemberPoint(UUID memberId, Integer pointBalance) {
        this.memberId = memberId;
        this.pointBalance = pointBalance;
    }

    public void addPoints(int pointBalance) {
        this.pointBalance += pointBalance;
    }

    public void deductPoints(int pointBalance) {
        refund(pointBalance);
        lastUsedAt = OffsetDateTime.now();
    }

    public void refund(int pointBalance) {
        if (this.pointBalance < pointBalance) {
            throw new CustomException(CustomErrorCode.LACK_OF_POINT);
        }
        this.pointBalance -= pointBalance;
    }
}
