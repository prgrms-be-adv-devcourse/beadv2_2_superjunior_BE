package store._0982.order.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.order.domain.settlement.SellerBalanceHistory;

import java.util.UUID;

public interface SellerBalanceHistoryJpaRepository extends JpaRepository<SellerBalanceHistory, UUID> {
}
