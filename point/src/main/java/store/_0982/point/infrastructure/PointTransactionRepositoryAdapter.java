package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.PointTransactionStatus;
import store._0982.point.domain.entity.PointTransaction;
import store._0982.point.domain.repository.PointTransactionRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PointTransactionRepositoryAdapter implements PointTransactionRepository {

    private final PointTransactionJpaRepository pointTransactionJpaRepository;

    @Override
    public PointTransaction save(PointTransaction pointTransaction) {
        return pointTransactionJpaRepository.save(pointTransaction);
    }

    @Override
    public boolean existsByIdempotencyKey(UUID idempotencyKey) {
        return pointTransactionJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsByOrderIdAndStatus(UUID orderId, PointTransactionStatus status) {
        return pointTransactionJpaRepository.existsByOrderIdAndStatus(orderId, status);
    }

    @Override
    public PointTransaction saveAndFlush(PointTransaction pointTransaction) {
        return pointTransactionJpaRepository.saveAndFlush(pointTransaction);
    }

    @Override
    public Optional<PointTransaction> findByOrderIdAndStatus(UUID orderId, PointTransactionStatus status) {
        return pointTransactionJpaRepository.findByOrderIdAndStatus(orderId, status);
    }
}
