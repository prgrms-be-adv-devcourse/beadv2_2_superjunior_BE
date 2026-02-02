package store._0982.batch.domain.settlement;

import store._0982.common.domain.settlement.Settlement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SettlementRepository {
    Settlement save(Settlement settlement);

    void saveAll(List<Settlement> settlements);

    Optional<Settlement> findById(UUID settlementId);
}
