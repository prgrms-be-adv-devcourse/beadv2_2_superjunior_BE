package store._0982.point.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import store._0982.common.exception.CustomException;
import store._0982.point.exception.CustomErrorCode;

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

    @Column(name = "point_balance", nullable = false)
    private long pointBalance;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    public MemberPoint(UUID memberId) {
        this.memberId = memberId;
        this.pointBalance = 0;
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
