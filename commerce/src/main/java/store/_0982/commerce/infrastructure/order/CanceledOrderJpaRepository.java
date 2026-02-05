package store._0982.commerce.infrastructure.order;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.order.CancelStatus;
import store._0982.commerce.domain.order.CanceledOrder;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CanceledOrderJpaRepository extends JpaRepository<CanceledOrder, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(UUID orderId);

    List<CanceledOrder> findAllByStatusInAndCanceledAtBefore(Collection<CancelStatus> statuses, OffsetDateTime canceledAtBefore);

    Optional<CanceledOrder> findByOrderId(UUID orderId);
}
