package store._0982.order.infrastructure.settlement;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.order.domain.settlement.SellerBalance;

import java.util.Optional;
import java.util.UUID;

public interface SellerBalanceJpaRepository extends JpaRepository<SellerBalance, UUID> {
    Optional<SellerBalance> findByMemberId(UUID memberId);
}
