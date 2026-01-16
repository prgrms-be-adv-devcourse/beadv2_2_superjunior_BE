package store._0982.point.application.dto.pg;

import store._0982.point.domain.entity.PgPayment;
import store._0982.point.domain.constant.PgPaymentStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PgCreateInfo(
        UUID paymentPointId,
        UUID memberId,
        UUID orderId,
        long amount,
        PgPaymentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime requestedAt
) {
    public static PgCreateInfo from(PgPayment requested) {
        return new PgCreateInfo(
                requested.getId(),
                requested.getMemberId(),
                requested.getOrderId(),
                requested.getAmount(),
                requested.getStatus(),
                requested.getCreatedAt(),
                requested.getRequestedAt()
        );
    }
}
