package store._0982.point.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import store._0982.point.domain.entity.Payment;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {
    Page<Payment> findAllByMemberId(UUID memberId, Pageable pageable);

    Optional<Payment> findByPgOrderId(UUID pgOrderId);

    Optional<Payment> findByPgOrderIdWithLock(UUID pgOrderId);

    Optional<Payment> findById(UUID id);

    Payment saveAndFlush(Payment payment);
}
