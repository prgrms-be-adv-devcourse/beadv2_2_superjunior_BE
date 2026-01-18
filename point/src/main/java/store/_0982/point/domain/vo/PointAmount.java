package store._0982.point.domain.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import store._0982.common.exception.CustomException;
import store._0982.point.exception.CustomErrorCode;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode
public class PointAmount {

    @Column(name = "paid_point", nullable = false)
    private long paidPoint;

    @Column(name = "bonus_point", nullable = false)
    private long bonusPoint;

    public static PointAmount zero() {
        return new PointAmount(0, 0);
    }

    public static PointAmount of(long paidPoint, long bonusPoint) {
        return new PointAmount(paidPoint, bonusPoint);
    }

    public static PointAmount paid(long amount) {
        return new PointAmount(amount, 0);
    }

    public static PointAmount bonus(long amount) {
        return new PointAmount(0, amount);
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

    public PointAmount transfer(long amount) {
        PointAmount transfer = calculateTransfer(amount);
        return new PointAmount(this.paidPoint - transfer.paidPoint, this.bonusPoint - transfer.bonusPoint);
    }

    public PointAmount use(long amount) {
        PointAmount deduction = calculateDeduction(amount);
        return new PointAmount(this.paidPoint - deduction.paidPoint, this.bonusPoint - deduction.bonusPoint);
    }

    public PointAmount calculateTransfer(long amount) {
        if (paidPoint < amount) {
            throw new CustomException(CustomErrorCode.LACK_OF_POINT);
        }

        return PointAmount.paid(amount);
    }

    public PointAmount calculateRefund(long amount) {
        if (getTotal() < amount) {
            throw new CustomException(CustomErrorCode.INVALID_REFUND_AMOUNT);
        }

        if (amount <= paidPoint) {
            return new PointAmount(amount, 0);
        } else {
            long refundableBonusPoint = amount - paidPoint;
            return new PointAmount(paidPoint, refundableBonusPoint);
        }
    }

    public PointAmount calculateDeduction(long amount) {
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