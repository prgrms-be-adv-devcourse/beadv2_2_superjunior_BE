package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.constant.PointPaymentStatus;
import store._0982.point.domain.entity.PointPayment;
import store._0982.point.domain.repository.PointPaymentRepository;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PointPaymentRepositoryAdapter implements PointPaymentRepository {
    private final PointPaymentJpaRepository historyJpaRepository;

    @Override
    public PointPayment save(PointPayment pointPayment) {
        return historyJpaRepository.save(pointPayment);
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
    public PointPayment saveAndFlush(PointPayment pointPayment) {
        return historyJpaRepository.saveAndFlush(pointPayment);
    }
}
