package store._0982.batch.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import store._0982.batch.domain.settlement.OrderSettlement;

import java.util.List;
import java.util.UUID;

public interface OrderSettlementJpaRepository extends JpaRepository<OrderSettlement, UUID> {

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
          UPDATE OrderSettlement os
             SET os.settledAt = CURRENT_TIMESTAMP
           WHERE os.orderSettlementId IN :orderSettlementIds
          """)
    void markSettled(
            @Param("orderSettlementIds") List<UUID> orderSettlementIds);
}
