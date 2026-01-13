package store._0982.point.domain.repository;

import store._0982.point.domain.constant.PointPaymentStatus;
import store._0982.point.domain.entity.PointPayment;

import java.util.UUID;

public interface PointPaymentRepository {
    PointPayment save(PointPayment pointPayment);

    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, PointPaymentStatus status);

    PointPayment saveAndFlush(PointPayment pointPayment);
}
