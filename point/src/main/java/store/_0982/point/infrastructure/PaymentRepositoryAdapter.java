package store._0982.point.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.repository.PaymentRepository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepository {

    private final PaymentJpaRepository paymentJpaRepository;

    @Override
    public Page<Payment> findAllByMemberId(UUID memberId, Pageable pageable) {
        return paymentJpaRepository.findAllByMemberId(memberId, pageable);
    }

    @Override
    public Optional<Payment> findByOrderId(UUID orderId) {
        return paymentJpaRepository.findByPgOrderId(orderId);
    }

    @Override
    public Optional<Payment> findByOrderIdWithLock(UUID orderId) {
        return paymentJpaRepository.readByPgOrderId(orderId);
    }

    @Override
    public Payment saveAndFlush(Payment payment) {
        return paymentJpaRepository.saveAndFlush(payment);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentJpaRepository.findById(id);
    }
}
