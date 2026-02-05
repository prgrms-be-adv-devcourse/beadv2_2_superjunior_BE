package store._0982.batch.infrastructure.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.order.OrderRepository;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;


    @Override
    public List<Order> findExpiredPendingOrders(OffsetDateTime now) {
        return orderJpaRepository.findByStatusAndExpiredAtBefore(OrderStatus.PENDING, now);
    }

    @Override
    public void saveAll(List<Order> orders) {
        orderJpaRepository.saveAll(orders);
    }
}
