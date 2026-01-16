package store._0982.point.application.dto.point;

import lombok.Builder;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointTransaction;

import java.util.UUID;

@Builder
public record PointTransactionInfo(
        UUID id,
        PointTransactionStatus status,
        long paidPoint,
        long bonusPoint,
        UUID orderId,
        String cancelReason
) {
    public static PointTransactionInfo from(PointTransaction transaction) {
        return PointTransactionInfo.builder()
                .id(transaction.getId())
                .status(transaction.getStatus())
                .paidPoint(transaction.getPaidAmount())
                .bonusPoint(transaction.getBonusAmount())
                .orderId(transaction.getOrderId())
                .cancelReason(transaction.getCancelReason())
                .build();
    }
}
