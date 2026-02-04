package store._0982.commerce.domain.order;

import java.util.UUID;

public interface CanceledOrderRepository {

    boolean existsByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(UUID orderId);

    void save(CanceledOrder canceledOrder);
}
