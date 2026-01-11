package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.constant.PointHistoryStatus;
import store._0982.point.domain.entity.PointHistory;

import java.util.UUID;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistory, UUID> {
    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, PointHistoryStatus status);
}
