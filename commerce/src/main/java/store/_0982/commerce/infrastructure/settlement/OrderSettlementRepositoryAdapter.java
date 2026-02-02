package store._0982.commerce.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.settlement.OrderSettlement;
import store._0982.commerce.domain.settlement.OrderSettlementRepository;

@RequiredArgsConstructor
@Repository
public class OrderSettlementRepositoryAdapter implements OrderSettlementRepository {

    private final OrderSettlementJpaRepository orderSettlementJpaRepository;

    @Override
    public void save(OrderSettlement orderSettlement) {
        orderSettlementJpaRepository.save(orderSettlement);
    }
}
