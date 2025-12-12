package store._0982.order.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.order.domain.Order;
import store._0982.order.domain.OrderRepository;

import store._0982.order.domain.OrderStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryAdaptor implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }

    @Override
    public List<Order> saveAll(List<Order> orders) {
        return orderJpaRepository.saveAll(orders);
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return orderJpaRepository.findById(orderId);
    }

    @Override
    public Page<Order> findAll(Pageable pageable) {
        return orderJpaRepository.findAll(pageable);
    }

    @Override
    public void delete(Order order) {
        orderJpaRepository.delete(order);
    }

    @Override
    public Optional<Order> findByOrderIdAndDeletedAtIsNull(UUID orderId) {
        return orderJpaRepository.findByOrderIdAndDeletedAtIsNull(orderId);
    }

    @Override
    public Page<Order> findByMemberIdAndDeletedIsNull(UUID memberId, Pageable pageable) {
        return orderJpaRepository.findByMemberIdAndDeletedAtIsNull(memberId, pageable);
    }

    @Override
    public Page<Order> findBySellerIdAndDeletedIsNull(UUID sellerId, Pageable pageable) {
        return orderJpaRepository.findBySellerIdAndDeletedAtIsNull(sellerId, pageable);
    }
}
