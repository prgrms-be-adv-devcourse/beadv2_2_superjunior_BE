package store._0982.batch.domain.settlement;

import java.util.List;
import java.util.UUID;

public interface OrderSettlementRepository {
    void markSettled(List<UUID> orderSettlementIds);
}
