package store._0982.commerce.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.settlement.Settlement;
import store._0982.commerce.domain.settlement.SettlementRepository;

@RequiredArgsConstructor
@Repository
public class SettlementRepositoryAdapter implements SettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;

    @Override
    public Settlement save(Settlement settlement) {
        return settlementJpaRepository.save(settlement);
    }

}
