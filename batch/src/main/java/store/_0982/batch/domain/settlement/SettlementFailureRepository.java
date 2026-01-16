package store._0982.batch.domain.settlement;

import java.util.List;
import java.util.UUID;

public interface SettlementFailureRepository {

    SettlementFailure save(SettlementFailure settlementFailure);

    void saveAll(List<SettlementFailure> failures);

    void incrementRetryCount(UUID settlementId);

    void deleteBySettlementId(UUID settlementId);
}
