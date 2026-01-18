package store._0982.batch.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.settlement.SettlementFailure;
import store._0982.batch.domain.settlement.SettlementFailureRepository;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@Repository
public class SettlementFailureRepositoryAdapter implements SettlementFailureRepository {

    private final SettlementFailureJpaRepository settlementFailureJpaRepository;

    @Override
    public SettlementFailure save(SettlementFailure settlementFailure) {
        return settlementFailureJpaRepository.save(settlementFailure);
    }

    @Override
    public void saveAll(List<SettlementFailure> failures) {
        settlementFailureJpaRepository.saveAll(failures);
    }

    @Override
    public void incrementRetryCount(UUID settlementId) {
        settlementFailureJpaRepository.incrementRetryCount(settlementId);
    }

    @Override
    public void deleteBySettlementId(UUID settlementId) {
        settlementFailureJpaRepository.deleteBySettlementId(settlementId);
    }
}
