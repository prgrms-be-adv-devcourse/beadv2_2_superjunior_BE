package store._0982.commerce.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.commerce.domain.settlement.SettlementFailure;
import store._0982.commerce.domain.settlement.SettlementFailureRepository;

@RequiredArgsConstructor
@Repository
public class SettlementFailureRepositoryAdapter implements SettlementFailureRepository {

    private final SettlementFailureJpaRepository settlementFailureJpaRepository;

    @Override
    public SettlementFailure save(SettlementFailure settlementFailure) {
        return settlementFailureJpaRepository.save(settlementFailure);
    }

}
