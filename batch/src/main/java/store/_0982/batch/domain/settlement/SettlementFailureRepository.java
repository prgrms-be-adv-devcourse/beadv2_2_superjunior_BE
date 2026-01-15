package store._0982.batch.domain.settlement;


import java.util.List;

public interface SettlementFailureRepository {

    SettlementFailure save(SettlementFailure settlementFailure);

    void saveAll(List<SettlementFailure> failures);
}
