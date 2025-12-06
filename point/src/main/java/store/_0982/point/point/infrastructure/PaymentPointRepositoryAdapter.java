package store._0982.point.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import store._0982.point.point.domain.PaymentPoint;
import store._0982.point.point.domain.PaymentPointRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentPointRepositoryAdapter implements PaymentPointRepository {

    private final PaymentPointJpaRepository paymentPointJpaRepository;

    @Override
    public PaymentPoint save(PaymentPoint paymentPoint) {
        return paymentPointJpaRepository.save(paymentPoint);
    }

    @Override
    public Page<PaymentPoint> findAllByMemberId(UUID memberId, Pageable pageable) {
        return paymentPointJpaRepository.findAllByMemberId(memberId, pageable);
    }

    @Override
    public Optional<PaymentPoint> findByOrderId(UUID orderId) {
        return paymentPointJpaRepository.findByOrderId(orderId);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return paymentPointJpaRepository.existsByOrderId(orderId);
    }
}
