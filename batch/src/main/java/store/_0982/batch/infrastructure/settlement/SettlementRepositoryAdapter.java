package store._0982.batch.infrastructure.settlement;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import store._0982.batch.domain.settlement.Settlement;
import store._0982.batch.domain.settlement.SettlementRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Repository
public class SettlementRepositoryAdapter implements SettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;

    @Override
    public Settlement save(Settlement settlement) {
        return settlementJpaRepository.save(settlement);
    }

    @Override
    public void saveAll(List<Settlement> settlements) {
        settlementJpaRepository.saveAll(settlements);
    }

    @Override
    public Optional<Settlement> findById(UUID settlementId) {
        return settlementJpaRepository.findById(settlementId);
    }
}
