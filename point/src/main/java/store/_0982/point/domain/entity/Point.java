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
@Table(name = "point", schema = "payment_schema")
public class Point {

    @Id
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "paid_point", nullable = false)
    private long paidPoint;

    @Column(name = "bonus_point", nullable = false)
    private long bonusPoint;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

    public Point(UUID memberId) {
        this.memberId = memberId;
        this.paidPoint = 0;
        this.bonusPoint = 0;
    }

    public void charge(long amount) {
        this.paidPoint += amount;
    }

    public void earnBonus(long bonus) {
        this.bonusPoint += bonus;
    }

    public void use(long pointBalance) {
        deduct(pointBalance);
        lastUsedAt = OffsetDateTime.now();
    }

    public void transfer(long pointBalance) {
        deduct(pointBalance);
    }

    public long getTotalBalance() {
        return paidPoint + bonusPoint;
    }

    private void deduct(long pointBalance) {
        long totalBalance = getTotalBalance();
        if (totalBalance < pointBalance) {
            throw new CustomException(CustomErrorCode.LACK_OF_POINT);
        }

        if (bonusPoint >= pointBalance) {
            bonusPoint -= pointBalance;
        } else {
            long requiredAmount = pointBalance - bonusPoint;
            bonusPoint = 0;
            paidPoint -= requiredAmount;
        }
    }
}
