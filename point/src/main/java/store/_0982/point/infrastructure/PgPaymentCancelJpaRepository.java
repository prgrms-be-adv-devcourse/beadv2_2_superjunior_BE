package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PgPaymentCancel;

import java.util.UUID;

public interface PgPaymentCancelJpaRepository extends JpaRepository<PgPaymentCancel, UUID> {
}
