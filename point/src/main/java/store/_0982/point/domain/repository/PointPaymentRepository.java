package store._0982.point.domain.repository;

import store._0982.point.domain.constant.PointPaymentStatus;
import store._0982.point.domain.entity.PointTransaction;

import java.util.UUID;

public interface PointPaymentRepository {
    PointTransaction save(PointTransaction pointTransaction);

    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, PointPaymentStatus status);

    PointTransaction saveAndFlush(PointTransaction pointTransaction);
}
