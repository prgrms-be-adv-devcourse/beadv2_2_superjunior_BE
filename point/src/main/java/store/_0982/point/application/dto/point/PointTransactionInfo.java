package store._0982.point.application.dto.point;

import lombok.Builder;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.constant.PointType;
import store._0982.point.domain.entity.PointTransaction;

import java.util.UUID;

@Builder
public record PointTransactionInfo(
        UUID id,
        PointTransactionStatus status,
        long amount,
        UUID orderId,
        String cancelReason
) {
    public static PointTransactionInfo from(PointTransaction transaction, PointType type) {
        long amount;

        if (type == PointType.PAID) {
            amount = transaction.getPaidAmount();
        } else if (type == PointType.BONUS) {
            amount = transaction.getBonusAmount();
        } else {
            throw new IllegalArgumentException("Wrong Point Type");
        }

        return PointTransactionInfo.builder()
                .id(transaction.getId())
                .status(transaction.getStatus())
                .amount(amount)
                .orderId(transaction.getOrderId())
                .cancelReason(transaction.getCancelReason())
                .build();
    }
}
