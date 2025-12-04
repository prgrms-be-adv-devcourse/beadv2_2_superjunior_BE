package store._0982.point.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.point.domain.PaymentPoint;

import java.util.UUID;

public interface PaymentPointJpaRepository extends JpaRepository<PaymentPoint, UUID> {
}
