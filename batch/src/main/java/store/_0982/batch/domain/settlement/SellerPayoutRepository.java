package store._0982.batch.domain.settlement;

import store._0982.common.domain.settlement.SellerPayout;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerPayoutRepository {
    SellerPayout save(SellerPayout settlement);

    void saveAll(List<SellerPayout> settlements);

    Optional<SellerPayout> findById(UUID settlementId);
}
