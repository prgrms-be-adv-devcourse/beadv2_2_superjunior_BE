package store._0982.commerce.infrastructure.grouppurchase;

import org.springframework.data.jpa.repository.JpaRepository;
import store._0982.commerce.domain.grouppurchase.GroupPurchaseLike;

import java.util.Optional;
import java.util.UUID;

public interface GroupPurchaseLikeJpaRepository extends JpaRepository<GroupPurchaseLike, UUID> {
    boolean existsByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);

    Optional<GroupPurchaseLike> findByMemberIdAndGroupPurchaseId(UUID memberId, UUID groupPurchaseId);
}
