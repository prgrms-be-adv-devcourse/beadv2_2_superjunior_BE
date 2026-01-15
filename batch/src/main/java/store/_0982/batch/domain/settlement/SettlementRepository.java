package store._0982.batch.domain.settlement;

import java.util.List;

public interface SettlementRepository {
    Settlement save(Settlement settlement);

    void saveAll(List<Settlement> settlements);
}
