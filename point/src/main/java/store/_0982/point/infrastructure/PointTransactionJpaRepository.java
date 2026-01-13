package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.constant.PointPaymentStatus;
import store._0982.point.domain.entity.PointTransaction;

import java.util.UUID;

public interface PointTransactionJpaRepository extends JpaRepository<PointTransaction, UUID> {
    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, PointPaymentStatus status);
}
