package store._0982.product.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.product.domain.settlement.SettlementFailure;

import java.util.UUID;

public interface SettlementFailureJpaRepository extends JpaRepository<SettlementFailure, UUID> {
}
