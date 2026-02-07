package store._0982.batch.infrastructure.sellerpayout;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.common.domain.sellerpayout.SellerPayoutFailure;

import java.util.UUID;

public interface SellerPayoutFailureJpaRepository extends JpaRepository<SellerPayoutFailure, UUID> {

    @Modifying
    @Query("UPDATE SellerPayoutFailure sf SET sf.retryCount = sf.retryCount + 1 WHERE sf.sellerPayoutId = :sellerPayoutId")
    void incrementRetryCount(@Param("sellerPayoutId") UUID sellerPayoutId);

    void deleteBySellerPayoutId(UUID sellerPayoutId);
}
