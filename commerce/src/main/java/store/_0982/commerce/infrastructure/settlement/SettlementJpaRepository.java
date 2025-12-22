package store._0982.commerce.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.settlement.Settlement;

import java.util.UUID;

public interface SettlementJpaRepository extends JpaRepository<Settlement, UUID> {
}
