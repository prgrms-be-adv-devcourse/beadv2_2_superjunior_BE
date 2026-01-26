package store._0982.batch.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.settlement.OrderSettlementRepository;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class OrderSettlementRepositoryAdapter implements OrderSettlementRepository {

    private final OrderSettlementJpaRepository orderSettlementJpaRepository;

    @Override
    public void markSettled(List<UUID> orderSettlementIds) {
        orderSettlementJpaRepository.markSettled(orderSettlementIds);
    }
}
