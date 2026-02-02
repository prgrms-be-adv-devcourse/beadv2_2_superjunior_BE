package store._0982.batch.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.common.domain.settlement.Settlement;

import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<Settlement, UUID> {
}
