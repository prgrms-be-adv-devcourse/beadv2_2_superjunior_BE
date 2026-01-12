package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.PointHistoryStatus;
import store._0982.point.domain.entity.PointHistory;
import store._0982.point.domain.repository.PointHistoryRepository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryAdapter implements PointHistoryRepository {
    private final PointHistoryJpaRepository historyJpaRepository;

    @Override
    public PointHistory save(PointHistory pointHistory) {
        return historyJpaRepository.save(pointHistory);
    }

    @Override
    public boolean existsByIdempotencyKey(UUID idempotencyKey) {
        return historyJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsByOrderIdAndStatus(UUID orderId, PointHistoryStatus status) {
        return historyJpaRepository.existsByOrderIdAndStatus(orderId, status);
    }

    @Override
    public PointHistory saveAndFlush(PointHistory pointHistory) {
        return historyJpaRepository.saveAndFlush(pointHistory);
    }
}
