package store._0982.commerce.infrastructure.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.order.CanceledOrderRepository;

@RequiredArgsConstructor
@Repository
public class CanceledOrderRepositoryAdaptor implements CanceledOrderRepository {

    private final CanceledOrderJpaRepository canceledOrderJpaRepository;

}
