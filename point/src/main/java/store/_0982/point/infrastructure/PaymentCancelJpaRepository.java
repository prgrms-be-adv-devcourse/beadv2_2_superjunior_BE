package store._0982.point.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.point.domain.entity.PaymentCancel;

import java.util.UUID;

public interface PaymentCancelJpaRepository extends JpaRepository<PaymentCancel, UUID> {
}
