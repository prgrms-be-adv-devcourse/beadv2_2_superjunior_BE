package store._0982.order.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.order.domain.Order;

import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
}
