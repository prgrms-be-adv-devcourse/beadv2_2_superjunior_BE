package store._0982.batch.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.settlement.SettlementFailure;
import store._0982.batch.domain.settlement.SettlementFailureRepository;


@RequiredArgsConstructor
@Repository
public class SettlementFailureRepositoryAdapter implements SettlementFailureRepository {

    private final SettlementFailureJpaRepository settlementFailureJpaRepository;

    @Override
    public SettlementFailure save(SettlementFailure settlementFailure) {
        return settlementFailureJpaRepository.save(settlementFailure);
    }

}
