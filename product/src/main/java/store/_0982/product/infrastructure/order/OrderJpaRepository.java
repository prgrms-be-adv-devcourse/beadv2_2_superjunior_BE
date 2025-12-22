package store._0982.product.infrastructure.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.product.domain.order.Order;
import store._0982.product.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByMemberIdAndDeletedAtIsNull(UUID memberId, Pageable pageable);

    Page<Order> findBySellerIdAndDeletedAtIsNull(UUID sellerId, Pageable pageable);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    List<Order> findByGroupPurchaseIdAndDeletedAtIsNull(UUID groupPurchaseId);

    List<Order> findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(UUID groupPurchaseId, OrderStatus status);
}
