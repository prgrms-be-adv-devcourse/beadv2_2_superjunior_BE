package store._0982.batch.domain.settlement;

import store._0982.common.domain.settlement.SellerPayoutFailure;

import java.util.List;
import java.util.UUID;

public interface SellerPayoutFailureRepository {

    SellerPayoutFailure save(SellerPayoutFailure sellerPayoutFailure);

    void saveAll(List<SellerPayoutFailure> failures);

    void incrementRetryCount(UUID sellerPayoutId);

    void deleteBySellerPayoutId(UUID sellerPayoutId);
}
