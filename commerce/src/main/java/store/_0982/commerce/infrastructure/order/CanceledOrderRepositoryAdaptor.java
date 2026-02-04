package store._0982.commerce.infrastructure.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.order.CanceledOrderRepository;

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
}
