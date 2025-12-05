package store._0982.point.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.point.domain.PaymentPointFailure;

import java.util.UUID;

public interface PaymentPointFailureJpaRepository extends JpaRepository<PaymentPointFailure, UUID> {
}
