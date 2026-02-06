package store._0982.commerce.infrastructure.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.order.CancelStatus;
import store._0982.commerce.domain.order.CanceledOrder;
import store._0982.commerce.domain.order.CanceledOrderRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class CanceledOrderRepositoryAdaptor implements CanceledOrderRepository {

    private final CanceledOrderJpaRepository canceledOrderJpaRepository;

    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return canceledOrderJpaRepository.existsByIdempotencyKey(idempotencyKey);
    }

    @Override
    public boolean existsByOrderId(UUID orderId) {
        return canceledOrderJpaRepository.existsByOrderId(orderId);
    }

    @Override
    public void save(CanceledOrder canceledOrder) {
        canceledOrderJpaRepository.save(canceledOrder);
    }

    @Override
    public List<CanceledOrder> findAllByStatusInAndCanceledAtBefore(List<CancelStatus> pendingStatuses, OffsetDateTime minutesAgo) {
        return canceledOrderJpaRepository.findAllByStatusInAndCanceledAtBefore(pendingStatuses, minutesAgo);
    }

    @Override
    public Optional<CanceledOrder> findByOrderId(UUID orderId) {
        return canceledOrderJpaRepository.findByOrderId(orderId);
    }

    @Override
    public Page<CanceledOrder> findAllByMemberId(UUID memberId, Pageable pageable) {
        return canceledOrderJpaRepository.findAllByMemberId(memberId, pageable);
    }
}
