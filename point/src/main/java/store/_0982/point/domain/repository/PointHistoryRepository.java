package store._0982.point.domain.repository;

import store._0982.point.domain.constant.PointHistoryStatus;
import store._0982.point.domain.entity.PointHistory;

import java.util.UUID;

public interface PointHistoryRepository {
    PointHistory save(PointHistory pointHistory);

    boolean existsByIdempotencyKey(UUID idempotencyKey);

    boolean existsByOrderIdAndStatus(UUID orderId, PointHistoryStatus status);

    PointHistory saveAndFlush(PointHistory pointHistory);
}
