package store._0982.batch.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.settlement.OrderSettlementRepository;

@RequiredArgsConstructor
@Repository
public class OrderSettlementRepositoryAdapter implements OrderSettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;

}
