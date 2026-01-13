package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PgPaymentFailure;

import java.util.UUID;

public interface PgPaymentFailureJpaRepository extends JpaRepository<PgPaymentFailure, UUID> {
}
