package store._0982.commerce.domain.order;

import store._0982.common.domain.order.CancelStatus;
import store._0982.common.domain.order.CanceledOrder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CanceledOrderRepository {

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(UUID orderId);

    void save(CanceledOrder canceledOrder);

    List<CanceledOrder> findAllByStatusInAndCanceledAtBefore(List<CancelStatus> pendingStatuses, OffsetDateTime minutesAgo);

    Optional<CanceledOrder> findByOrderId(UUID orderId);
}
