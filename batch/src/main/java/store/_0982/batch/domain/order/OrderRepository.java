package store._0982.batch.domain.order;

import store._0982.common.domain.order.Order;

import java.time.OffsetDateTime;
import java.util.List;

public interface OrderRepository {
    List<Order> findExpiredPendingOrders(OffsetDateTime now);

    void saveAll(List<Order> orders);
}
