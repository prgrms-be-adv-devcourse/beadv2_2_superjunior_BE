package store._0982.batch.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.batch.domain.settlement.SettlementFailure;

import java.util.UUID;

public interface SettlementFailureJpaRepository extends JpaRepository<SettlementFailure, UUID> {

    @Modifying
    @Query("UPDATE SettlementFailure sf SET sf.retryCount = sf.retryCount + 1 WHERE sf.settlementId = :settlementId")
    void incrementRetryCount(@Param("settlementId") UUID settlementId);

    void deleteBySettlementId(UUID settlementId);
}
