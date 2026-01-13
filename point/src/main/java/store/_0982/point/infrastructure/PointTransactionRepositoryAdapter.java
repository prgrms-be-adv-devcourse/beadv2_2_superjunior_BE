package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.PointPaymentStatus;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.repository.PointPaymentRepository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PointTransactionRepositoryAdapter implements PointPaymentRepository {
    private final PointTransactionJpaRepository historyJpaRepository;

    @Override
    public PointTransaction save(PointTransaction pointTransaction) {
        return historyJpaRepository.save(pointTransaction);
    }

    @Override
    public boolean existsByIdempotencyKey(UUID idempotencyKey) {
        return historyJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsByOrderIdAndStatus(UUID orderId, PointPaymentStatus status) {
        return historyJpaRepository.existsByOrderIdAndStatus(orderId, status);
    }

    @Override
    public PointTransaction saveAndFlush(PointTransaction pointTransaction) {
        return historyJpaRepository.saveAndFlush(pointTransaction);
    }
}
