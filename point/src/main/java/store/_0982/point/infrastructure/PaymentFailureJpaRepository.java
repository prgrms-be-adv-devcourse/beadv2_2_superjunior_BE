package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.Payment;
import store._0982.point.domain.entity.PaymentFailure;

import java.util.Optional;
import java.util.UUID;

public interface PaymentFailureJpaRepository extends JpaRepository<PaymentFailure, UUID> {
    Optional<PaymentFailure> findByPayment(Payment payment);
}
