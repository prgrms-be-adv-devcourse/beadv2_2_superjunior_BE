package store._0982.point.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.point.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Page<Payment> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<Payment> findByOrderId(UUID orderId);

    Optional<Payment> findByOrderIdWithLock(UUID orderId);

    Optional<Payment> findById(UUID id);

    Payment saveAndFlush(Payment payment);
}
