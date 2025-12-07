package store._0982.product.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.product.domain.GroupPurchase;

import java.util.UUID;

public interface GroupPurchaseJpaRepository extends JpaRepository<GroupPurchase, UUID> {

    Page<GroupPurchase> findAllBySellerId(UUID sellerId, Pageable pageable);
}
