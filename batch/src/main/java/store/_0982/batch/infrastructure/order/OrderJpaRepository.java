package store._0982.batch.infrastructure.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.batch.batch.sellerbalance.writer.GroupPurchaseAmountRow;
import store._0982.batch.domain.order.Order;
import store._0982.batch.domain.order.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderJpaRepository extends JpaRepository<Order, UUID> {

    Page<Order> findByMemberIdAndDeletedAtIsNull(UUID memberId, Pageable pageable);

    Page<Order> findBySellerIdAndDeletedAtIsNull(UUID sellerId, Pageable pageable);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    List<Order> findByGroupPurchaseIdAndDeletedAtIsNull(UUID groupPurchaseId);

    List<Order> findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(UUID groupPurchaseId, OrderStatus status);

    List<Order> findByGroupPurchaseIdInAndStatus(List<UUID> groupPurchaseIds, OrderStatus status);

    boolean existsByIdempotencyKey(String idempotenceKey);

    @Query(
            """
            select o.groupPurchaseId as groupPurchaseId,
                   sum(o.price * o.quantity) as totalAmount
            from Order o
            where o.groupPurchaseId in :groupPurchaseIds
            and o.status = :status
            group by o.groupPurchaseId
            """
    )
    List<GroupPurchaseAmountRow> sumTotalAmountByGroupPurchaseIdsAndStatus(
            @Param("groupPurchaseIds") List<UUID> groupPurchaseIds,
            @Param("status") OrderStatus orderStatus);
}
