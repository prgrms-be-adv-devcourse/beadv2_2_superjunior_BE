package store._0982.point.point.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "member_point", schema = "point_schema")
public class MemberPoint {

    @Id
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "point_balance")
    private Integer pointBalance;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    protected MemberPoint(){}

    public MemberPoint(UUID memberId, int pointBalance){
        this.memberId = memberId;
        this.pointBalance = pointBalance;
    }


    public void minus(int pointBalance, OffsetDateTime lastUsedAt) {
        this.pointBalance = pointBalance;
        this.lastUsedAt = lastUsedAt;
    }

    public void plusPoint(UUID memberId, int pointBalance) {
        this.memberId = memberId;
        this.pointBalance = pointBalance;
    }

    public void refund(int pointBalance) {
        this.pointBalance = pointBalance;
    }
}
