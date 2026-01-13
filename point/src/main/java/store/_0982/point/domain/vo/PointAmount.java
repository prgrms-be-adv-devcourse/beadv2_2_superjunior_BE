package store._0982.point.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import store._0982.common.exception.CustomException;
import store._0982.point.exception.CustomErrorCode;

@Embeddable
public record PointAmount(
        @Column(name = "paid_point", nullable = false)
        long paidPoint,

        @Column(name = "bonus_point", nullable = false)
        long bonusPoint
) {
    public static PointAmount zero() {
        return new PointAmount(0, 0);
    }

    public static PointAmount of(long paidPoint, long bonusPoint) {
        return new PointAmount(paidPoint, bonusPoint);
    }

    public long getTotal() {
        return paidPoint + bonusPoint;
    }

    public PointAmount addPaid(long amount) {
        return new PointAmount(this.paidPoint + amount, this.bonusPoint);
    }

    public PointAmount addBonus(long amount) {
        return new PointAmount(this.paidPoint, this.bonusPoint + amount);
    }

    public PointAmount use(long amount) {
        PointAmount deduction = calculateDeduction(amount);
        return new PointAmount(this.paidPoint - deduction.paidPoint, this.bonusPoint - deduction.bonusPoint);
    }

    private PointAmount calculateDeduction(long amount) {
        if (getTotal() < amount) {
            throw new CustomException(CustomErrorCode.LACK_OF_POINT);
        }

        if (this.bonusPoint >= amount) {
            return new PointAmount(0, amount);
        } else {
            long remainingDeduct = amount - this.bonusPoint;
            return new PointAmount(remainingDeduct, this.bonusPoint);
        }
    }
}
