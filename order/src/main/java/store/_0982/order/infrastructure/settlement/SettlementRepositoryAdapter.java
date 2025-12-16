package store._0982.order.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.order.domain.settlement.Settlement;
import store._0982.order.domain.settlement.SettlementRepository;

@RequiredArgsConstructor
@Repository
public class SettlementRepositoryAdapter implements SettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;

    @Override
    public Settlement save(Settlement settlement) {
        return settlementJpaRepository.save(settlement);
    }

}
