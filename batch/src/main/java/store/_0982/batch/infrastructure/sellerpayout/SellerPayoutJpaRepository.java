package store._0982.batch.infrastructure.sellerpayout;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.common.domain.sellerpayout.SellerPayout;

import java.util.UUID;

public interface SellerPayoutJpaRepository extends JpaRepository<SellerPayout, UUID> {
}
