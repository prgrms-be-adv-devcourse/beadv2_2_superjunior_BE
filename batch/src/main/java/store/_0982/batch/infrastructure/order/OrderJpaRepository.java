package store._0982.batch.infrastructure.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.common.domain.order.Order;
import store._0982.common.domain.order.OrderStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.expiredAt <= :now")
    List<Order> findByStatusAndExpiredAtBefore(@Param("status") OrderStatus status, @Param("now") OffsetDateTime now);
}
