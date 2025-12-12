package store._0982.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}
