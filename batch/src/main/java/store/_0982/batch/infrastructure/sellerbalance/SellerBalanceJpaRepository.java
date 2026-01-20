package store._0982.batch.infrastructure.sellerbalance;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.batch.domain.sellerbalance.SellerBalance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SellerBalanceJpaRepository extends JpaRepository<SellerBalance, UUID> {
    Optional<SellerBalance> findByMemberId(UUID memberId);
    List<SellerBalance> findAllByMemberIdIn(List<UUID> memberIds);
}
