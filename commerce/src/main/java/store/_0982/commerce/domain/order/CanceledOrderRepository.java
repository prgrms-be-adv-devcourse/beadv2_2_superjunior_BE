package store._0982.commerce.domain.order;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface CanceledOrderRepository {

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(UUID orderId);

    void save(CanceledOrder canceledOrder);

    List<CanceledOrder> findAllByStatusInAndCanceledAtBefore(List<CancelStatus> pendingStatuses, OffsetDateTime minutesAgo);
}
