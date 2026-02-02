package store._0982.batch.infrastructure.sellerbalance;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.common.domain.sellerbalance.SellerBalanceHistory;

import java.util.UUID;

public interface SellerBalanceHistoryJpaRepository extends JpaRepository<SellerBalanceHistory, UUID> {
}
