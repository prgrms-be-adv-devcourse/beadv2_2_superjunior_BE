package store._0982.commerce.domain.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);

    List<Order> saveAll(List<Order> orders);

    Optional<Order> findById(UUID orderId);

    Page<Order> findAll(Pageable pageable);

    void delete(Order order);

    Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId);

    Page<Order> findByMemberIdAndDeletedIsNull(UUID memberId, Pageable pageable);

    Page<Order> findBySellerIdAndDeletedIsNull(UUID memberId, Pageable pageable);

    List<Order> findByGroupPurchaseIdAndDeletedAtIsNull(UUID groupPurchaseId);

    List<Order> findByGroupPurchaseIdAndStatusAndDeletedAtIsNull(UUID groupPurchaseId, OrderStatus status);

    boolean existsByIdempotencyKey(String idempotenceKey);

    Optional<Order> findByIdempotenceKey(String idempotenceKey);

    List<Order> findAllByMemberId(UUID memberId);

    List<Order> findAllByStatusInAndCancelRequestAtBefore(List<OrderStatus> pendingStatuses, OffsetDateTime now);
  
    void bulkMarkGroupPurchaseFail(@Param("groupPurchaseId") UUID groupPurchaseId);

    void bulkMarkGroupPurchaseSuccess(@Param("groupPurchaseId") UUID groupPurchaseId);

}
