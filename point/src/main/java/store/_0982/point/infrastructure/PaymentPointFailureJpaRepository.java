package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PaymentPoint;
import store._0982.point.domain.entity.PaymentPointFailure;

import java.util.Optional;
import java.util.UUID;

public interface PaymentPointFailureJpaRepository extends JpaRepository<PaymentPointFailure, UUID> {
    Optional<PaymentPointFailure> findByPaymentPoint(PaymentPoint paymentPoint);
}
