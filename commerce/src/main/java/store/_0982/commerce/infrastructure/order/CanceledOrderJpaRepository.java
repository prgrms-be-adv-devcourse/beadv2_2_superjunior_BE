package store._0982.commerce.infrastructure.order;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.order.CanceledOrder;

import java.util.UUID;

public interface CanceledOrderJpaRepository extends JpaRepository<CanceledOrder, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(UUID orderId);
}
