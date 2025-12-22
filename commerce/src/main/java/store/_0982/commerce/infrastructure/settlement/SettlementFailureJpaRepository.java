package store._0982.commerce.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.settlement.SettlementFailure;

import java.util.UUID;

public interface SettlementFailureJpaRepository extends JpaRepository<SettlementFailure, UUID> {
}
